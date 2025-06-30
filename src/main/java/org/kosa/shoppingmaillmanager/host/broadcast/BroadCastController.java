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

import org.kosa.shoppingmaillmanager.obswebsocket.OBSControlService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
	private final OBSControlService obsControlService;
	
	 // 실제 서버에 썸네일을 저장할 디렉터리 (로컬 경로)
    private static final String UPLOAD_DIR = "C:/upload/";
	
    // 상품 등록
	@PostMapping("/register")
	public ResponseEntity<?> register(
	        @RequestPart("broadcast") BroadCast broadCast,        // 프론트에서 전송된 방송 정보 (JSON 형식)
	        @RequestPart("productList") String productListJson) { // 상품 목록 JSON 문자열
	    try {
	    	// 방송자 id를 jwt 토큰에서 user_id를 추출 
	    	broadCast.setBroadcaster_id((String) SecurityContextHolder.getContext().getAuthentication().getPrincipal()); 
	        
	    	// JSON 문자열로 넘어온 productList를 자바 객체(List<BroadCastProduct>)로 변환
	        try {
	            ObjectMapper mapper = new ObjectMapper(); // Jackson의 JSON 파서 생성
	            List<BroadCastProduct> productList = mapper.readValue(
	                    productListJson, 
	                    new TypeReference<>() {} // 제네릭 타입 유지를 위한 구조 (List<BroadCastProduct>)
	            );
	            broadCast.setProductList(productList); // 파싱된 리스트를 BroadCast 객체에 주입
	        } catch (IOException e) {
	            // JSON 파싱 실패 시 예외 처리
	            e.printStackTrace(); 
	         // 추후에 바깥 catch에서 걸림
	            throw new RuntimeException("상품 리스트 JSON 파싱 실패", e); 
	        }

	        // 방송 등록 서비스 호출 → DB 저장 및 스트림키 생성 등 처리
	        BroadCastRegisterResult result = broadCastService.register(broadCast);

	        // 등록된 방송 정보 가져오기
	        BroadCast saved = result.getSaved();         // 저장된 BroadCast 엔티티
	        String stream_key = saved.getStream_key();  // 생성된 방송 키 (고유값)

	        // RTMP / HLS URL 생성
	        String rtmpUrl = "rtmp://localhost/stream/"; // 방송 입력 주소 (OBS용)
	        String hlsUrl = "http://localhost:8090/live/" + stream_key + "_720p2628kbs/index.m3u8"; // 시청 URL
	        saved.setStream_url(hlsUrl); // 방송 객체에 스트림 URL 저장

	        // 클라이언트에게 응답으로 전달할 데이터 구성
	        Map<String, Object> response = new HashMap<>();
	        response.put("broadcast", saved);      // 등록된 방송 객체
	        response.put("stream_key", stream_key);
	        response.put("rtmp_url", rtmpUrl);
	        response.put("stream_url", hlsUrl);

	        return ResponseEntity.ok(response); // 상태 200 OK로 응답

	    } catch (IllegalArgumentException e) {
	        // 사용자가 잘못된 데이터를 입력했을 때
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                .body(Map.of("error", e.getMessage()));
	    } catch (RuntimeException e) {
	        // 서비스 처리 중 예외 발생 시
	        return ResponseEntity.status(HttpStatus.FORBIDDEN)
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
    @GetMapping("/init")
    public ResponseEntity<Map<String, Object>> initBroadcastInfo() {
        String streamKey = UUID.randomUUID().toString();
        String rtmpUrl = "rtmp://localhost/stream/";
        String hlsUrl = "http://localhost:8090/live/" + streamKey + "_720p2628kbs/index.m3u8";

        Map<String, Object> result = new HashMap<>();
        result.put("stream_key", streamKey);
        result.put("rtmp_url", rtmpUrl);
        result.put("stream_url", hlsUrl);

        return ResponseEntity.ok(result);
    }
	
//    @GetMapping("/{broadcast_id}")
//    public ResponseEntity<?> getBroadcastInfo(@PathVariable int broadcast_id) {
//        BroadCast broadcast = broadCastService.findById(broadcast_id);
//        if (broadcast == null) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("방송을 찾을 수 없습니다.");
//        }
//        return ResponseEntity.ok(broadcast);
//    }
    
    // 방송 정보 불러오기
    @GetMapping("/{broadcast_id}")
    public ResponseEntity<?> getBroadcastDetail(@PathVariable("broadcast_id") int broadcastId) {
        BroadCast broadcast = broadCastService.getBroadcastDetails(broadcastId);
        return ResponseEntity.ok(broadcast);
    }
 
    
    // 방송 시작 API
    @PostMapping("/start")
    public ResponseEntity<?> startBroadcast(@RequestBody Map<String, Object> req) {
        
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
        	obsControlService.startStreaming();
        	
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
        	obsControlService.stopStreaming();
        	
        	// 방송 상태를 START로 변경
            broadcast.setBroadcast_status("stop");
            
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
    
}
