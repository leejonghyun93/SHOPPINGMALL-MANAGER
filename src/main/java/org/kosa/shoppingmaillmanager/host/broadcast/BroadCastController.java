package org.kosa.shoppingmaillmanager.host.broadcast;

import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
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
    
    
    /**
     * 클라이언트 요청에서 broadcast_id 추출 후,
     * 현재 로그인한 사용자의 방송인지 검증하는 메서드
     *
     * @param req 클라이언트로부터 받은 요청 바디(Map 형태)
     * @return BroadCast 객체 (유효하지 않으면 null)
     */
    public BroadCast validateAndGetBroadcast(Map<String, Object> req) {
        try {
            // 1. broadcast_id 파싱
            int broadcast_id = Integer.parseInt(req.get("broadcast_id").toString());

            // 2. 인증 정보 가져오기
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            // 3. 인증되지 않았거나, 사용자 정보가 없을 경우 null 반환
            if (auth == null || !auth.isAuthenticated()) return null;

            // 4. 현재 로그인한 사용자 ID 추출 (JWT 인증 기준)
            String user_id = (String) auth.getPrincipal();

            // 5. 방송 ID로 방송 정보 조회
            BroadCast broadcast = broadCastService.findById(broadcast_id);

            // 6. 방송이 없거나, 현재 사용자가 방송 등록자가 아닌 경우 null 반환
            if (broadcast == null || !broadcast.getBroadcaster_id().equals(user_id)) {
                return null;
            }

            // 7. 모든 조건 통과 → 방송 정보 반환
            return broadcast;

        } catch (Exception e) {
            // 파싱 오류 등 예외 발생 시 null 반환
            return null;
        }
    }
 
    
    /**
     * 방송 시작 API
     *
     * @param req 방송 ID를 포함한 요청 데이터 (JSON Body)
     * @return 방송 시작 성공/실패 응답
     */
    @PostMapping("/start")
    public ResponseEntity<?> startBroadcast(@RequestBody Map<String, Object> req) {

        // 1. 방송 유효성 및 권한 체크
        BroadCast broadcast = validateAndGetBroadcast(req);

        // 2. 유효하지 않으면 403 응답
        if (broadcast == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "status", "error",
                "message", "방송이 존재하지 않거나 권한이 없습니다."
            ));
        }

        try {
            // 3. 방송 ID로 방송 시작 (OBS로 전송하는 로직)
            broadCastService.startStreaming(broadcast.getBroadcast_id());

            log.info("🚀 OBS 스트리밍 시작 요청 전송");
        	
        	// 방송 상태를 START로 변경
            broadcast.setBroadcast_status("start");
            
            // 변경된 방송 정보를 DB에 저장
            broadCastService.updateStatus(broadcast);
            
            // 4. 성공 응답 반환
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "stream_url", broadcast.getStream_url()
            ));

        } catch (Exception e) {
            // 5. 내부 오류 발생 시 500 응답
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }
    
    
    
    /**
     * 방송 중지 API
     *
     * @param req 방송 ID를 포함한 요청 데이터 (JSON Body)
     * @return 방송 중지 성공/실패 응답
     */
    @PostMapping("/stop")
    public ResponseEntity<?> stopBroadcast(@RequestBody Map<String, Object> req) {

        // 1. 방송 유효성 및 권한 체크
        BroadCast broadcast = validateAndGetBroadcast(req);

        // 2. 유효하지 않으면 403 응답
        if (broadcast == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "status", "error",
                "message", "방송이 존재하지 않거나 권한이 없습니다."
            ));
        }

        try {
            // 3. 방송 ID로 방송 중지 (OBS로 전송하는 로직)
            broadCastService.stopStreaming(broadcast.getBroadcast_id());

            log.info("🚀 OBS 스트리밍 중지 요청 전송");
        	
        	// 방송 상태를 START로 변경
            broadcast.setBroadcast_status("stop");
            
            // 변경된 방송 정보를 DB에 저장
            broadCastService.updateStatus(broadcast);
            
            // 4. 성공 응답 반환
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "stream_url", broadcast.getStream_url()
            ));

        } catch (Exception e) {
            // 5. 내부 오류 발생 시 500 응답
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }
    
    
    /**
     * 녹화 및 송출 시작 API
     *
     * @param req 방송 ID를 포함한 요청 데이터 (JSON Body)
     * @return 녹화 시작 성공/실패 응답
     */
    @PostMapping("/live")
    public ResponseEntity<?> recordStartBroadcast(@RequestBody Map<String, Object> req) {

        // 1. 방송 유효성 및 권한 체크
        BroadCast broadcast = validateAndGetBroadcast(req);

        // 2. 유효하지 않으면 403 응답
        if (broadcast == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "status", "error",
                "message", "방송이 존재하지 않거나 권한이 없습니다."
            ));
        }

        try {
            // 3. 방송 ID로 녹화 시작 (OBS로 전송하는 로직)
            broadCastService.startRecording(broadcast.getBroadcast_id());

            // 4. 성공 응답 반환
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "stream_url", broadcast.getStream_url()
            ));

        } catch (Exception e) {
            // 5. 내부 오류 발생 시 500 응답
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }
    
    
    
    /**
     * 녹화 및 송출 중지 API
     *
     * @param req 방송 ID를 포함한 요청 데이터 (JSON Body)
     * @return 녹화 중지 성공/실패 응답
     */
    @PostMapping("/ended")
    public ResponseEntity<?> recordStopBroadcast(@RequestBody Map<String, Object> req) {

        // 1. 방송 유효성 및 권한 체크
        BroadCast broadcast = validateAndGetBroadcast(req);

        // 2. 유효하지 않으면 403 응답
        if (broadcast == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "status", "error",
                "message", "방송이 존재하지 않거나 권한이 없습니다."
            ));
        }

        try {
            // 3. 방송 ID로 녹화 중지 (OBS로 전송하는 로직)
            broadCastService.stopRecording(broadcast.getBroadcast_id());

            // 4. 성공 응답 반환
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "stream_url", broadcast.getStream_url()
            ));

        } catch (Exception e) {
            // 5. 내부 오류 발생 시 500 응답
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
    	
    	dto.setBroadcaster_id((String) SecurityContextHolder.getContext().getAuthentication().getPrincipal()); 
	
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
    
    // 방송자의 녹화 파일(mp4 등)을 업로드받아 서버에 저장하고, 재생 가능한 URL을 DB에 저장하는 API
    @PostMapping("/video/upload")
    public ResponseEntity<?> upload(@RequestParam MultipartFile file,      // 클라이언트에서 업로드한 파일
                                    @RequestParam int broadcast_id) {     			    // 어떤 방송의 영상인지 식별할 ID

        // Spring 서버 내에 녹화 파일을 저장할 디렉토리 경로 (이 경로는 서버 로컬 디스크 기준)
        String saveDir = "C:/videos/";

        // 저장할 파일명: broadcast ID와 원본 파일명을 조합하여 고유하게 생성 (ex. broadcast_5_myvideo.mp4)
        String filename = "broadcast_" + broadcast_id + "_" + file.getOriginalFilename();

        try {
        	
        	// 저장 폴더가 없으면 생성
            File dir = new File(saveDir);
            if (!dir.exists()) {
                boolean created = dir.mkdirs(); // 폴더 생성 (상위 경로까지 포함)
                if (!created) {
                    return ResponseEntity.status(500).body("❌ 저장 디렉토리 생성 실패");
                }
            }
        	
            // 파일 저장: 업로드된 파일을 지정된 경로에 실제로 저장
            // 저장 위치: Spring Boot가 실행 중인 PC의 로컬 디스크 (예: C:/upload/videos/...)
            file.transferTo(new File(saveDir + filename));

            // 현재 Spring 서버의 IP 주소를 자동으로 추출 (ex. 192.168.0.101)
            String serverIp = broadCastService.getLocalIp(); // 아래에 정의된 getLocalIp() 메서드 참고

            // 사용자 브라우저에서 접근 가능한 영상 URL 생성
            // ex. http://192.168.0.101:8080/video/broadcast_5_myvideo.mp4
            String videoUrl = "http://" + serverIp + ":8080/video/" + filename;

            // DB 업데이트: 해당 방송 ID에 대해 생성된 videoUrl을 저장
            // → 이후 Vue에서 이 URL을 불러와서 <video>로 재생할 수 있음
            broadCastService.updateVideoUrl(broadcast_id, videoUrl);

            // 응답: 생성된 영상 URL을 클라이언트에 JSON으로 반환
            return ResponseEntity.ok(Map.of("video_url", videoUrl));
        } catch (IOException e) {
            // 파일 저장 중 에러 발생 시 500 에러 반환
            return ResponseEntity.status(500).body("업로드 실패");
        }
    }
    
    	// 현재 서버의 IPv4 주소를 자동으로 추출하는 메서드
 		// Spring Boot가 실행 중인 PC의 실제 네트워크 IP (ex. 192.168.0.101)를 반환함
 		private String getLocalIp() {
 			try {
 				// 현재 시스템에 존재하는 모든 네트워크 인터페이스(유선랜, 와이파이, 가상 어댑터 등)를 순회
 			   for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
 			             
 			   // 해당 인터페이스에 연결된 모든 IP 주소를 순회 (IPv4, IPv6 포함)
 			   for (InetAddress addr : Collections.list(ni.getInetAddresses())) {
 			
 			       // 조건 1: 루프백 주소는 제외 (예: 127.0.0.1 → 자기 자신용 주소는 사용 X)
 			       // 조건 2: IPv4 주소만 추출 (IPv6 주소는 제외)
 			       if (!addr.isLoopbackAddress() && addr instanceof Inet4Address) {
 			
 			                     // 조건을 만족하는 첫 번째 IPv4 주소를 반환 (예: 192.168.0.101)
 			                     return addr.getHostAddress();
 			                 }
 			             }
 			        }
 			} catch (Exception e) {
 			         // 예외 발생 시 로그 출력 (예: 인터페이스 조회 실패 등)
 			         e.printStackTrace();
 			}
 			
 			// 조건에 맞는 IP를 찾지 못하거나 예외 발생 시 fallback 값으로 "localhost" 반환
 			return "localhost";
 		}
    
    
	 
}
