package org.kosa.shoppingmaillmanager.host.broadcast;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.kosa.shoppingmaillmanager.page.PageResponseVO;
import org.kosa.shoppingmaillmanager.security.AESUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/broadcast")
public class BroadCastController {
	
	private final BroadCastService broadCastService;
	private final ViewerRedisService redisService;
	
	 // 실제 서버에 썸네일을 저장할 디렉터리 (로컬 경로)
    private static final String UPLOAD_DIR = "C:/upload/";
    
    // 방송 등록
    @PostMapping("/register")
    public ResponseEntity<?> register(
        @RequestPart("broadcast") BroadCast broadCast,
        @RequestPart("productList") String productListJson) {

        try {
            // 방송자 ID 추출
            String broadcaster_id = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            broadCast.setBroadcaster_id(broadcaster_id);

            // OBS 정보 확인
            if (broadCast.getObs_host() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "OBS 연결 정보가 존재하지 않습니다."));
            }

            // 상품 JSON 파싱 (ObjectMapper 그대로 유지)
            try {
                ObjectMapper mapper = new ObjectMapper();
                List<BroadCastProduct> productList = mapper.readValue(productListJson, new TypeReference<>() {});
                broadCast.setProductList(productList);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("상품 리스트 JSON 파싱 실패", e);
            }

            // OBS 비밀번호 암호화
            String encryptedOBSPassword = AESUtil.encrypt(broadCast.getObs_password());
            broadCast.setObs_password(encryptedOBSPassword);
            
            // 등록 처리
            broadCastService.register(broadCast);

            // 프론트에 전달
            return ResponseEntity.ok(Map.of(
            	"broadcast", broadCast  // broadcast_id 포함 전체 객체
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "등록 중 오류 발생"));
        }
    }
	
	// 썸네일 업로드 요청 처리
	@PostMapping("/uploads/thumbnail")
	public ResponseEntity<?> uploadThumbnail(
			@RequestParam("file") MultipartFile file){
		
		// 파일이 비어있을 경우 error 응답 출력
		if(file.isEmpty()) {
			return ResponseEntity.badRequest().body(Map.of("error", "파일이 비어있습니다."));
		}
		
		try {
			File uploadDir = new File(UPLOAD_DIR);
			// 저장 폴더가 존재하지 않을 경우 새로 생성하기
			if(!uploadDir.exists()) {
				uploadDir.mkdirs();
			}
			
			// 파일명 임의 생성
			String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
			
			// 파일 저장 경로 생성
			Path savePath = Paths.get(UPLOAD_DIR, filename);
			
			// 파일 저장 (파일명 중복시 덮어쓰기)
			Files.copy(file.getInputStream(), savePath,
					StandardCopyOption.REPLACE_EXISTING);
			
			// 저장된 파일에 접근 가능한 URL 경로 구성
			String fileUrl = "/upload/" + filename;
			
			System.out.println("저장됨: " + savePath.toString());
			System.out.println("파일 크기: " + Files.size(savePath) + " bytes");
			
			// 클라이언트에게 파일 URL 응답
			return ResponseEntity.ok(Map.of("url", fileUrl));
			
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "파일 업로드 중 오류가 발생했습니다."));
		}
	}
	
	
	 // 상품명 키워드 검색
    @GetMapping("/product/search")
    public ResponseEntity<List<BroadCastProduct>> searchProducts(@RequestParam String keyword) {
        List<BroadCastProduct> result = broadCastService.findByKeyword(keyword);
        return ResponseEntity.ok(result);
    }
    
    // 스트림 키, 스트림 url, rtmp url 등 불러오기
//    @GetMapping("/init")
//    public ResponseEntity<Map<String, Object>> initBroadcastInfo() {
//        String streamKey = UUID.randomUUID().toString();
//        String rtmpUrl = "rtmp://192.168.4.206/stream/";
//        String hlsUrl = "http://192.168.4.206:8090/live/" + streamKey + "_720p2628kbs/index.m3u8";
//
//        Map<String, Object> result = new HashMap<>();
//        result.put("stream_key", streamKey);
//        result.put("rtmp_url", rtmpUrl);
//        result.put("stream_url", hlsUrl);
//
//        return ResponseEntity.ok(result);
//    }
    
    // 방송 정보 불러오기
    @GetMapping("/{broadcast_id}")
    public ResponseEntity<?> getBroadcastDetail(@PathVariable("broadcast_id") int broadcast_id) throws Exception {
    	// 방송 정보 불러오기
    	BroadCast b = broadCastService.getBroadcastDetails(broadcast_id);

        // stream_key는 등록 시 이미 생성되어 있으므로 여기선 복호화만 하면 됨
        String streamKey = AESUtil.decrypt(b.getStream_key());
        
        // rtmp 주소 설정
        String rtmpUrl = "rtmp://" + b.getNginx_host() + "/stream/";

        // hls_url : 방송 송출 url
        String hls_url = b.getStream_url();
        
        Map<String, Object> result = new HashMap<>();
        result.put("broadcast", b);
        result.put("stream_key", streamKey);
        result.put("rtmp_url", rtmpUrl);
        result.put("stream_url", hls_url);        
        
        return ResponseEntity.ok(result);
    }
 
    
    // 방송 시작 API
    @PostMapping("/start")
    public ResponseEntity<?> startBroadcast(@RequestBody Map<String, Object> req) {
        
    	// 클라이언트에서 넘어온 방송 ID 추출
    	 int broadcast_id = Integer.parseInt(req.get("broadcast_id").toString());
    	
    	// 인증 객체 수동으로 가져오기
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    	
    	// 인증 정보 없거나 인증 안 된 경우
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "status", "error",
                "message", "인증되지 않은 사용자입니다."
            ));
        }
    	
    	// 현재 로그인한 사용자의 ID 추출 (JWT 인증 기준)
        String user_id = (String) auth.getPrincipal();

        // 방송 ID로 방송 정보를 DB에서 조회
        BroadCast broadcast = broadCastService.findById(broadcast_id);

        System.out.println("user_id : " + user_id);
        // 방송이 존재하지 않거나, 해당 방송의 등록자가 현재 로그인한 사용자와 다를 경우 → 권한 없음
        if (broadcast == null || !broadcast.getBroadcaster_id().equals(user_id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "status", "error",
                "message", "권한 없음 또는 방송 없음"
            ));
        }

        try {
        	// OBS에 방송 시작 명령 전송
        	broadCastService.startStreaming(broadcast_id);
        	log.info("🚀 OBS 스트리밍 시작 요청 전송");
        	
        	// 방송 상태를 START로 변경
            broadcast.setBroadcast_status("start");
            
            // 변경된 방송 정보를 DB에 저장
            broadCastService.updateStatus(broadcast);

            // 성공 응답 반환 (Stream_url 포함)
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "stream_url", broadcast.getStream_url()
            ));
        } catch (Exception e) {
        	// 예외 발생 시 에러 응답 변환
        	return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }
    
    
    
    // 방송 중지 API
    @PostMapping("/stop")
    public ResponseEntity<?> stopBroadcast(@RequestBody Map<String, Object> req) {
        
    	// 클라이언트에서 넘어온 방송 ID 추출
    	int broadcast_id = Integer.valueOf(req.get("broadcast_id").toString());
    	
    	// 인증 객체 수동으로 가져오기
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    	
    	// 인증 정보 없거나 인증 안 된 경우
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "status", "error",
                "message", "인증되지 않은 사용자입니다."
            ));
        }
    	
    	// 현재 로그인한 사용자의 ID 추출 (JWT 인증 기준)
        String user_id = (String) auth.getPrincipal();

        // 방송 ID로 방송 정보를 DB에서 조회
        BroadCast broadcast = broadCastService.findById(broadcast_id);

        System.out.println("user_id : " + user_id);
        // 방송이 존재하지 않거나, 해당 방송의 등록자가 현재 로그인한 사용자와 다를 경우 → 권한 없음
        if (broadcast == null || !broadcast.getBroadcaster_id().equals(user_id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "status", "error",
                "message", "권한 없음 또는 방송 없음"
            ));
        }

        try {
        	// OBS에 방송 시작 명령 전송
        	broadCastService.stopStreaming(broadcast_id);
        	
        	// 방송 상태를 STOP로 변경
            broadcast.setBroadcast_status("stop");
            
            // 변경된 방송 정보를 DB에 저장
            broadCastService.updateStatus(broadcast);

//            String recordedFilePath = obsControlService.stopRecordingAndGetFilePath(); // OBS → 녹화 종료 + 파일 경로 받음
//            broadCastService.uploadToSpringServer(broadcast_id, recordedFilePath); // Spring upload API로 업로드 요청 보내기
            
            // 성공 응답 반환 (Stream_url 포함)
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "stream_url", broadcast.getStream_url()
            ));
        } catch (Exception e) {
        	// 예외 발생 시 에러 응답 변환
        	return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }
    
    @GetMapping("/{broadcast_id}/viewer-count")
    public ResponseEntity<Long> getViewerCount(@PathVariable int broadcast_id) {
    	long count = redisService.getCount(broadcast_id);
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/list")
    public ResponseEntity<PageResponseVO<BroadCastListDTO>> broadcastList(
    		@ModelAttribute BroadCastListDTO dto){
    	
    	PageResponseVO<BroadCastListDTO> pageResponse = broadCastService.list(dto);
    	return ResponseEntity.ok(pageResponse);
    			
    }
    
    // 방송 정보 불러오기
    @GetMapping("/detail/{broadcast_id}")
    public ResponseEntity<?> getBroadcastDetailView(@PathVariable("broadcast_id") int broadcastId) {
        BroadCast broadcast = broadCastService.getBroadcastDetailsView(broadcastId);
        return ResponseEntity.ok(broadcast);
    }
    
    // 방송 상태 변경
    @PutMapping("/status")
    public ResponseEntity<?> updateStatus(@RequestBody BroadCast broadCast) {
        broadCastService.updateStatus(broadCast);
        return ResponseEntity.ok().body(Map.of("result", "success"));
    } 
    
    @PostMapping("/upload")
    public ResponseEntity<?> uploadRecording(@RequestParam MultipartFile file,
                @RequestParam("broadcast_id") int broadcastId) {
        String saveDir = "C:/upload/recordings/"; // 받아온 영상을 저장할 경로
        File dir = new File(saveDir); // 만들어진 경로를 파일 형태로 변환
        if (!dir.exists()) dir.mkdirs(); // 해당 경로가 없을 경우 직접 생성

        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename(); // 파일명 임의 생성
        String videoUrl = "/upload/recordings/" + filename; // db에 저장할 동영상 주소

        try {
            file.transferTo(new File(saveDir + filename)); // 저장경로 + 파일명 합쳐서 파일로 변환
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("파일 저장 실패");
        }

        //  여기서 DB에 video_url 저장
        broadCastService.updateVideoUrl(broadcastId, videoUrl);

        return ResponseEntity.ok(videoUrl);
    }
    
}
