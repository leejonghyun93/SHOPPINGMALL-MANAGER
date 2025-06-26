package org.kosa.shoppingmaillmanager.host.broadcast;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/broadcast")
public class BroadCastController {
	
	private final BroadCastService broadCastService;
	
	@PostMapping("/register")
	public ResponseEntity<?> register(@RequestBody BroadCast broadCast) {
	    try {
	        BroadCast saved = broadCastService.register(broadCast);
	        
	        String stream_key = saved.getStream_key();
	        
	        String rtmpUrl = "rtmp://localhost:1935/stream/" + stream_key;
	        String hlsUrl = "http://localhost:8090/live/" + stream_key + "_720p2628kbs/index.m3u8";
	        broadCast.setStream_url(hlsUrl);
	        
	        // 민감 정보 제거
	        saved.setStream_key(null);
	        
	        Map<String, Object> result = new HashMap<>();
	        result.put("broadcast", saved);
	        result.put("rtmp_url", rtmpUrl);
	        result.put("hls_url", hlsUrl);

	        return ResponseEntity.ok(result);

	    } catch (IllegalArgumentException e) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                .body(Map.of("error", e.getMessage()));
	    } catch (RuntimeException e) {
	        return ResponseEntity.status(HttpStatus.FORBIDDEN)
	                .body(Map.of("error", e.getMessage()));
	    }
	}
}
