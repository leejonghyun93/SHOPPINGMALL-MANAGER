package org.kosa.shoppingmaillmanager.host.broadcast;

import java.io.File;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.kosa.shoppingmaillmanager.page.PageResponseVO;
import org.kosa.shoppingmaillmanager.security.AESUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonObject;

import org.springframework.messaging.simp.SimpMessagingTemplate;

import io.jsonwebtoken.io.IOException;
import io.obswebsocket.community.client.OBSRemoteController;
import io.obswebsocket.community.client.message.request.record.StartRecordRequest;
import io.obswebsocket.community.client.message.request.record.StopRecordRequest;
import io.obswebsocket.community.client.message.request.stream.StartStreamRequest;
import io.obswebsocket.community.client.message.request.stream.StopStreamRequest;
import io.obswebsocket.community.client.message.response.RequestResponse;
import io.obswebsocket.community.client.message.response.record.StopRecordResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BroadCastService {

	private final BroadCastDAO broadCastDAO;

	private final ViewerRedisService redisService;

	private final SimpMessagingTemplate messagingTemplate;

	@Transactional
	public void register(BroadCast broadCast) {
		// 1. stream_key가 null이거나 비어 있으면 → 새로 생성
		if (broadCast.getStream_key() == null || broadCast.getStream_key().isBlank()) {
			// 스트림키 생성
			String rawKey = UUID.randomUUID().toString();
			try {
				String encryptedKey = AESUtil.encrypt(rawKey);
				broadCast.setStream_key(encryptedKey); // 암호화된 값 저장
				// hls_url : 방송 송출 url
				String hls_url = "http://" + broadCast.getNginx_host() + ":8090/live/" + rawKey
						+ "_720p2628kbs/index.m3u8";
				// stream_url DB에 저장
				broadCast.setStream_url(hls_url);

			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException("스트림 키 암호화 실패", e);
			}
		}

		// 2. 방송 정보 저장
		broadCastDAO.insert(broadCast);

		// 3. 상품 리스트 저장
		if (broadCast.getProductList() != null) {
			for (BroadCastProduct product : broadCast.getProductList()) {
				product.setBroadcast_id(broadCast.getBroadcast_id());
				broadCastDAO.insertProduct(product);
			}
		}
	}

	public List<BroadCastProduct> findByKeyword(String keyword) {
		return broadCastDAO.findByKeyword(keyword); // LIKE 검색용으로 감싸기
	}

	public BroadCast findById(int broadcast_id) {
		return broadCastDAO.findById(broadcast_id);
	}

	@Transactional(readOnly = true)
	public BroadCast getBroadcastDetails(int broadcast_id) {
		BroadCast broadcast = broadCastDAO.findBroadcastById(broadcast_id);
		if (broadcast == null) {
			throw new IllegalArgumentException("존재하지 않는 방송입니다.");
		}

		List<BroadCastProduct> products = broadCastDAO.findProductsByBroadcastId(broadcast_id);
		List<BroadCastViewer> viewers = broadCastDAO.findViewersByBroadcastId(broadcast_id);

		broadcast.setProductList(products);
		broadcast.setViewerList(viewers);

		return broadcast;
	}

	public void updateStatus(BroadCast broadCast) {

		broadCastDAO.updateStatus(broadCast);

		messagingTemplate.convertAndSend("/topic/broadcast/" + broadCast.getBroadcast_id() + "/status",
				Map.of("status", broadCast.getBroadcast_status()));
	}


	// 시청자 입장 메소드
	public void onViewerJoined(int broadcastId, BroadCastViewer viewer) {
		broadCastDAO.insertViewer(viewer);
		redisService.increase(broadcastId);
	}

	// 시청자 퇴장 메소드
	public void onViewerLeft(int broadcast_id, String user_id) {
		broadCastDAO.updateLeftTime(user_id, broadcast_id);
		redisService.decrease(broadcast_id);
	}

	// 방송 종료 메소드
	public void onBroadcastEnd(int broadcast_id) {
		long total = redisService.getCount(broadcast_id);
		broadCastDAO.updateTotalViewersManual(broadcast_id, total);
		redisService.remove(broadcast_id); // 캐시 제거
	}

	public PageResponseVO<BroadCastListDTO> list(BroadCastListDTO dto) {

		int start = (dto.getPageNo() - 1) * dto.getSize();

		Map<String, Object> map = new HashMap<>();
		map.put("start", start);
		map.put("size", dto.getSize());
		map.put("searchValue", dto.getSearchValue());
		map.put("broadcast_id", dto.getBroadcast_id());
		map.put("title", dto.getTitle());
		map.put("broadcaster_id", dto.getBroadcaster_id());
		map.put("created_at", dto.getCreated_at());
		map.put("current_viewers", dto.getCurrent_viewers());
		map.put("category_id", dto.getCategory_id());

		List<BroadCastListDTO> list = broadCastDAO.findBroadcastList(map);
		int total = broadCastDAO.countBroadcastList(map);

		return new PageResponseVO<>(dto.getPageNo(), list, total, dto.getSize());
	}

	@Transactional(readOnly = true)
	public BroadCast getBroadcastDetailsView(int broadcast_id) {
		BroadCast broadcast = broadCastDAO.findBroadcastById(broadcast_id);
		if (broadcast == null) {
			throw new IllegalArgumentException("존재하지 않는 방송입니다.");
		}

		broadCastDAO.findProductsByBroadcastId(broadcast_id);

		List<BroadCastProduct> products = broadCastDAO.findProductsByBroadcastId(broadcast_id);
		String category_name = broadCastDAO.findCategoryName(broadcast.getCategory_id());

		broadcast.setCategory_name(category_name);
		broadcast.setProductList(products);

		return broadcast;
	}

	@Transactional
	public void updateVideoUrl(int broadcastId, String videoUrl) {
		broadCastDAO.updateVideoUrl(broadcastId, videoUrl);
	}

	public void uploadToSpringServer(int broadcast_id, String recordedFilePath) {
		// TODO Auto-generated method stub

	}

	/**
	 * 방송 시작 요청 처리 - broadcasterId를 기반으로 연결 정보를 조회 - 해당 IP/PORT/PASSWORD로 OBS
	 * WebSocket 연결 생성 - 연결 성공 시 방송 및 녹화 시작 명령을 전송
	 * 
	 * @throws Exception
	 */
	// 방송 시작 메서드 (broadcast_id는 방송 고유 번호)
	public void startStreaming(int broadcast_id) throws Exception {
		// 1. DB에서 방송 ID로 방송 정보 조회
		BroadCast broadCast = broadCastDAO.findById(broadcast_id);

		// 2. 암호화된 OBS 비밀번호 복호화
		String password = AESUtil.decrypt(broadCast.getObs_password());
		System.out.println("🔑 복호화된 OBS 비밀번호 = " + password);

		// 3. 람다 내부에서 안전하게 참조할 수 있도록 AtomicReference 사용
		AtomicReference<OBSRemoteController> ref = new AtomicReference<>();

		// 4. OBS WebSocket 클라이언트 생성 (builder 패턴)
		OBSRemoteController client = OBSRemoteController.builder().host(broadCast.getObs_host()) // 방송자의 OBS가 실행 중인 PC
																									// IP
				.port(broadCast.getObs_port()) // OBS WebSocket 포트 (보통 4455)
				.password(password) // OBS WebSocket 연결 비밀번호
				.lifecycle() // 연결 생명주기 콜백 등록 시작
				.onIdentified(ctx -> {
					// 5. IDENTIFIED 이벤트 발생 시 (비밀번호 인증 성공)
					OBSRemoteController c = ref.get(); // 안전하게 참조
					if (c == null) {
						log.error("❌ client is null in onIdentified");
						return;
					}

					log.info("🟢 IDENTIFIED - 방송자 [{}]", broadcast_id);

					// 6. 방송 시작 요청
					c.sendRequest(StartStreamRequest.builder().build(), res -> log.info("✅ 방송 시작 응답: {}", res));

					// 7. 녹화 시작 요청
					c.sendRequest(StartRecordRequest.builder().build(), res -> log.info("🎥 녹화 시작 응답: {}", res));
				}).onHello(ctx -> log.info("👋 HELLO 수신 - IDENTIFY 준비됨") // 서버에서 HELLO 수신 시 출력됨
				).onDisconnect(() -> log.info("❌ 방송자 [{}] OBS 연결 해제", broadcast_id) // 연결 종료 시
				).and().build();

		// 8. 람다에서 참조할 수 있도록 AtomicReference에 저장
		ref.set(client);

		// 9. OBS WebSocket 연결 시도 (비동기)
		client.connect();
	}

	/**
	 * 방송 종료 요청 처리 - broadcasterId를 기반으로 연결 정보를 조회 - 해당 IP/PORT/PASSWORD로 OBS
	 * WebSocket 연결 생성 - 연결 성공 시 방송 및 녹화 종료 명령을 전송
	 * 
	 * @throws Exception
	 */

	// 방송 종료 메서드 (broadcast_id는 방송 고유 번호)

	public void stopStreaming(int broadcast_id) throws Exception {
		// 1. DB에서 방송 ID로 방송 정보 조회
		BroadCast broadCast = broadCastDAO.findById(broadcast_id);

		// 2. 암호화된 OBS 비밀번호 복호화
		String password = AESUtil.decrypt(broadCast.getObs_password());
		System.out.println("🔑 복호화된 OBS 비밀번호 = " + password);

		// 3. 람다 내부에서 안전하게 참조할 수 있도록 AtomicReference 사용
		AtomicReference<OBSRemoteController> ref = new AtomicReference<>();

		// 4. OBS WebSocket 클라이언트 생성 (builder 패턴)
		OBSRemoteController client = OBSRemoteController.builder().host(broadCast.getObs_host()) // 방송자의 OBS가 실행 중인 PC
																									// IP
				.port(broadCast.getObs_port()) // OBS WebSocket 포트 (보통 4455)
				.password(password) // OBS WebSocket 연결 비밀번호
				.lifecycle() // 연결 생명주기 콜백 등록 시작
				.onIdentified(ctx -> {
					// 5. IDENTIFIED 이벤트 발생 시 (비밀번호 인증 성공)
					OBSRemoteController c = ref.get(); // 안전하게 참조
					if (c == null) {
						log.error("❌ client is null in onIdentified");
						return;
					}

					log.info("🟢 IDENTIFIED - 방송자 [{}]", broadcast_id);

					// 6. 방송 종료 요청
					c.sendRequest(StopStreamRequest.builder().build(), res -> log.info("🛑 방송 종료 응답: {}", res));

					// 7. 녹화 종료 요청
					c.sendRequest(StopRecordRequest.builder().build(), res -> log.info("⏹ 녹화 종료 응답: {}", res));

					// 7. 녹화 종료 요청 (StopRecordRequest 전송)
					c.sendRequest(StopRecordRequest.builder().build(), // 1.. OBS에 '녹화 중지' 명령 요청 생성
							response -> { // 2. WebSocket을 통해 응답이 비동기로 들어옴 (Consumer<RequestResponse<?>>)

								var messageData = response.getMessageData(); // 3. 응답에서 messageData 객체 추출
								if (messageData == null) {
									// 4. 응답 본문이 없을 경우 로그 찍고 리턴
									log.warn("⚠️ 응답에 데이터가 없습니다.");
									return;
								}

								Object responseData = messageData.getResponseData(); // 5. 실제 응답 데이터 추출 (Map 형태로 들어옴)

								log.info("🔎 응답 데이터 타입: {}", responseData.getClass().getName());
								log.info("📦 응답 데이터 내용: {}", responseData);

// 	            		    if (responseData instanceof Map<?, ?> map) {
// 	            		        // 6. outputPath 키를 이용해 녹화된 영상의 파일 경로 추출
// 	            		        String outputPath = String.valueOf(map.get("outputPath"));
// 	            		        log.info("📁 녹화 파일 경로: {}", outputPath);
//
// 	            		        try {
// 	            		           // 7. Spring 서버로 영상 업로드 요청 (파일 전송)
// 	            		           uploadToSpringServer(outputPath, broadcast_id);
// 	            		        } catch (Exception e) {
// 	            		           // 8. 업로드 중 예외 발생 시 에러 로그 출력
// 	            		          log.error("❌ 업로드 실패", e);
// 	            		        }
// 	            		   } else {
// 	            		     // 9. 응답 데이터가 Map 형태가 아니면 경고 로그 출력
// 	            			 log.warn("⚠️ 예상치 못한 응답 형식: {}", responseData);
// 	            		   }
 	            		   
 	            		//  여기서 타입 캐스팅
 	            	        if (responseData instanceof StopRecordResponse.SpecificData data) {
 	            	            String outputPath = data.getOutputPath();
 	            	           

 	            	            try {
 	            	                uploadToSpringServer(outputPath, broadcast_id);
 	            	               log.info("📁 녹화 파일 경로: {}", outputPath);
 	            	            } catch (Exception e) {
 	            	                log.error("❌ 업로드 실패", e);
 	            	            }
 	            	        } else {
 	            	            log.warn("⚠️ 예상치 못한 응답 형식: {}", responseData);
 	            	        }
 	            		   
 	            		 }
 	            	);
 	            })
 	            .onHello(ctx -> 
 	                log.info("👋 HELLO 수신 - IDENTIFY 준비됨") // 서버에서 HELLO 수신 시 출력됨
 	            )
 	            .onDisconnect(() -> 
 	                log.info("❌ 방송자 [{}] OBS 연결 해제", broadcast_id) // 연결 종료 시
 	            )
 	            .and()
 	        .build();

 	    // 8. 람다에서 참조할 수 있도록 AtomicReference에 저장
 	    ref.set(client);

 	    // 9. OBS WebSocket 연결 시도 (비동기)
 	    client.connect();
 	}

	public void update(BroadCast b) {
		// TODO Auto-generated method stub

	}

	public void updateStreamUrl(BroadCast b) {
		broadCastDAO.updateStreamUrl(b);
	}

	// 현재 서버의 IPv4 주소를 자동으로 추출하는 메서드
	// Spring Boot가 실행 중인 PC의 실제 네트워크 IP (ex. 192.168.0.101)를 반환함
	public String getLocalIp() {
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
	
	
	// 녹화된 영상 파일을 Spring 서버에 업로드하는 메서드
	public void uploadToSpringServer(String filePath, int broadcastId) throws java.io.IOException {
	    // 업로드할 파일 객체 생성 (파일 경로로부터)
	    File file = new File(filePath);

		 // 파일이 생성되었는지 최대 10번 재시도
		    int retry = 0;
		    while (!file.exists() && retry++ < 10) {
		        log.warn("⏳ 파일 아직 존재하지 않음. 재시도 중... ({})", retry);
		        try {
		            Thread.sleep(500); // 0.5초 대기
		        } catch (InterruptedException e) {
		            Thread.currentThread().interrupt();
		            log.error("❌ 파일 대기 중 인터럽트 발생", e);
		        }
		    }

	    
	    // 파일이 존재하지 않을 경우 업로드 중단
	    if (!file.exists()) {
	        System.out.println("❌ 업로드 실패: 파일이 존재하지 않습니다.");
	        return;
	    }

	    // HTTP 요청을 보내기 위한 클라이언트 객체 생성 (자동 자원 해제)
	    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

	        // 업로드 요청을 보낼 대상 주소 (Spring 서버의 /video/upload 엔드포인트 : 컨트롤러로 요청 보냄)
	        HttpPost post = new HttpPost("http://" + getLocalIp() +":8080/video/upload");
	        log.info("업로드요청 링크 : " + post);
	        // multipart/form-data 형식으로 요청 본문 구성
	        HttpEntity entity = MultipartEntityBuilder.create()
	                // 바이너리 파일 전송 (form name: file)
	                .addBinaryBody("file", file, ContentType.DEFAULT_BINARY, file.getName())
	                // 방송 ID 전송 (form name: broadcast_id)
	                .addTextBody("broadcast_id", String.valueOf(broadcastId), ContentType.TEXT_PLAIN)
	                .build(); // 최종 MultipartEntity 완성

	        // 구성된 multipart entity를 POST 요청에 첨부
	        post.setEntity(entity);

	        // 응답을 처리할 핸들러 정의 (비동기 방식 아님, 간단한 콜백 처리)
	        HttpClientResponseHandler<Void> responseHandler = (ClassicHttpResponse response) -> {
	            int status = response.getCode(); // 응답 코드 (예: 200, 500 등)
	            System.out.println("✅ 응답 코드: " + status); // 콘솔 출력
	            return null; // 반환값 필요 없음
	        };

	        // 요청 전송 + 응답 핸들러로 결과 처리
	        httpClient.execute(post, responseHandler);

	    } catch (IOException e) {
	        // 네트워크 오류, 파일 I/O 오류 등 예외 발생 시 처리
	        System.out.println("❌ 업로드 중 예외 발생");
	        e.printStackTrace();
	    }
	}
	

}
