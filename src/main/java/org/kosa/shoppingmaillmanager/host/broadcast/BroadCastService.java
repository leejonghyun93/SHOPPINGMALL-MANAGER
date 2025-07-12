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
import java.util.function.Consumer;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
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

	@Transactional
	public BroadCast getBroadcastDetails(int broadcast_id) {
		BroadCast broadcast = broadCastDAO.findBroadcastById(broadcast_id);
		if (broadcast == null) {
			throw new IllegalArgumentException("존재하지 않는 방송입니다.");
		}

		List<BroadCastProduct> products = broadCastDAO.findProductsByBroadcastId(broadcast_id);
		List<BroadCastViewer> viewers = broadCastDAO.findViewersByBroadcastId(broadcast_id);
		broadCastDAO.updateBroadcastCategoryByTopProductCategory(broadcast_id);

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
	
	
	/**
	 * 방송자의 OBS에 WebSocket으로 연결하고, IDENTIFIED 시 전달된 행동(onIdentifiedAction)을 실행하는 메서드.
	 *
	 * @param broadcast_id 방송 고유 ID
	 * @param onIdentifiedAction 연결 성공 시 실행할 동작 (예: 방송 시작, 종료, 녹화 등)
	 * @throws Exception 예외 발생 시 호출자에게 전달
	 */
	public void withOBSClient(int broadcast_id, Consumer<OBSRemoteController> onIdentifiedAction) throws Exception {
	    // 1. 방송 ID를 이용해 DB에서 방송 상세 정보 조회
	    BroadCast broadCast = broadCastDAO.findById(broadcast_id);

	    // 2. 암호화되어 저장된 OBS 비밀번호 복호화
	    String password = AESUtil.decrypt(broadCast.getObs_password());
	    System.out.println("🔑 복호화된 OBS 비밀번호 = " + password);

	    // 3. 람다 내부에서 OBSRemoteController 인스턴스를 안전하게 참조하기 위한 래퍼런스 객체 생성
	    AtomicReference<OBSRemoteController> ref = new AtomicReference<>();

	    // 4. OBS WebSocket 클라이언트 생성 (빌더 패턴 사용)
	    OBSRemoteController client = OBSRemoteController.builder()
	        .host(broadCast.getObs_host())              // 방송자의 OBS가 실행 중인 PC의 IP 주소
	        .port(broadCast.getObs_port())              // WebSocket 연결 포트 (기본값: 4455)
	        .password(password)                         // 복호화된 OBS 연결 비밀번호
	        .lifecycle()                                // WebSocket 연결 생명주기 콜백 설정 시작

	        // 5. 서버에서 HELLO 패킷 수신 시 로그 출력
	        .onHello(ctx -> log.info("👋 HELLO 수신 - IDENTIFY 준비됨"))

	        // 6. IDENTIFIED (인증 성공) 이벤트 수신 시 실행될 콜백 정의
	        .onIdentified(ctx -> {
	            OBSRemoteController c = ref.get();      // 람다 외부에서 안전하게 참조
	            if (c == null) {
	                log.error("❌ client is null in onIdentified");
	                return;
	            }

	            log.info("🟢 IDENTIFIED - 방송자 [{}]", broadcast_id);

	            // ✅ 연결 성공 시 전달받은 동작 실행 (방송 시작/중지/녹화 등)
	            onIdentifiedAction.accept(c);
	        })

	        // 7. WebSocket 연결이 끊어졌을 때 로그 출력
	        .onDisconnect(() -> log.info("❌ 방송자 [{}] OBS 연결 해제", broadcast_id))

	        // 8. 라이프사이클 설정 종료 후 클라이언트 빌드
	        .and()
	        .build();

	    // 9. 생성된 OBSRemoteController 인스턴스를 AtomicReference에 저장 (람다에서 참조 가능하도록)
	    ref.set(client);

	    // 10. 비동기 방식으로 OBS WebSocket 서버에 연결 시도
	    client.connect();
	}
	

	/**
	 * 방송 시작 요청 처리 - broadcasterId를 기반으로 연결 정보를 조회 - 해당 IP/PORT/PASSWORD로 OBS
	 * WebSocket 연결 생성 - 연결 성공 시 방송 및 녹화 시작 명령을 전송
	 * 
	 * @throws Exception
	 */
	// 방송 시작 메서드 (broadcast_id는 방송 고유 번호)
	public void startStreaming(int broadcast_id) throws Exception {
	    withOBSClient(broadcast_id, client -> {
	        client.sendRequest(StartStreamRequest.builder().build(), 
	            res -> log.info("✅ 방송 시작 응답: {}", res));
	    });
	}

	/**
	 * 방송 종료 요청 처리 - broadcasterId를 기반으로 연결 정보를 조회 - 해당 IP/PORT/PASSWORD로 OBS
	 * WebSocket 연결 생성 - 연결 성공 시 방송 및 녹화 종료 명령을 전송
	 * 
	 * @throws Exception
	 */

	// 방송 종료 메서드 (broadcast_id는 방송 고유 번호)
	public void stopStreaming(int broadcast_id) throws Exception {
	    withOBSClient(broadcast_id, client -> {
	        client.sendRequest(StopStreamRequest.builder().build(), 
	            res -> log.info("🛑 방송 종료 응답: {}", res));
	    });
	}

	/**
	 * 방송 시작 요청 처리 - broadcasterId를 기반으로 연결 정보를 조회 - 해당 IP/PORT/PASSWORD로 OBS
	 * WebSocket 연결 생성 - 연결 성공 시 녹화 시작 명령을 전송
	 * 
	 * @throws Exception
	 */
	// 방송 녹화 시작 메서드 (broadcast_id는 방송 고유 번호)
	public void startRecording(int broadcast_id) throws Exception {
	    withOBSClient(broadcast_id, client -> {
	        client.sendRequest(StartRecordRequest.builder().build(),
	            res -> log.info("🎥 녹화 시작 응답: {}", res));
	    });
	}


	/**
	 * 방송 종료 요청 처리 - broadcasterId를 기반으로 연결 정보를 조회 - 해당 IP/PORT/PASSWORD로 OBS
	 * WebSocket 연결 생성 - 연결 성공 시 녹화 종료 명령을 전송
	 * 
	 * @throws Exception
	 */
	
	// 방송 녹화 종료 메서드 (broadcast_id는 방송 고유 번호)
		public void stopRecording(int broadcast_id) throws Exception {
		    withOBSClient(broadcast_id, client -> {
		        client.sendRequest(StopRecordRequest.builder().build(), // 1.. OBS에 '녹화 중지' 명령 요청 생성
						response -> { // 2. WebSocket을 통해 응답이 비동기로 들어옴 (Consumer<RequestResponse<?>>)
							
							log.info("⏹ 녹화 종료 응답: {}", response);
		        
							var messageData = response.getMessageData(); // 3. 응답에서 messageData 객체 추출
							if (messageData == null) {
								// 4. 응답 본문이 없을 경우 로그 찍고 리턴
								log.warn("⚠️ 응답에 데이터가 없습니다.");
								return;
							}

							Object responseData = messageData.getResponseData(); // 5. 실제 응답 데이터 추출 (Map 형태로 들어옴)

							log.info("🔎 응답 데이터 타입: {}", responseData.getClass().getName());
							log.info("📦 응답 데이터 내용: {}", responseData);

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
		    });
		}

	public void updateStreamUrl(BroadCast b) {
		broadCastDAO.updateStreamUrl(b);
	}
	
	
	
	/**
	 * OBS 녹화가 종료된 후, 저장된 영상 파일을 Spring 서버로 업로드하는 메서드
	 *
	 * @param filePath 녹화된 영상의 전체 파일 경로 (예: C:/upload/recordings/broadcast_1234_2025-07-12.mp4)
	 * @param broadcastId 어떤 방송의 영상인지 식별하기 위한 ID
	 * @throws IOException 파일 업로드 중 오류 발생 시 예외 처리
	 * @throws java.io.IOException 
	 * @throws InterruptedException 
	 */
	private void uploadToSpringServer(String filePath, int broadcastId) throws IOException, java.io.IOException, InterruptedException {
	    // 1. 업로드할 영상 파일 객체 생성
	    File file = new File(filePath);

	    // 2. 파일이 실제로 존재하는지 체크 (최대 5초까지 기다림)
	    int waitMs = 0;
	    while (!file.exists() && waitMs < 5000) {
	        Thread.sleep(200);
	        waitMs += 200;
	    }
	    if (!file.exists()) {
	        log.warn("❌ 업로드 실패: 파일이 존재하지 않습니다 → {}", filePath);
	        return;
	    }

	    // 3. 업로드를 요청할 Spring 서버의 API 주소
	    // TODO: 실제 Spring 서버의 IP 또는 도메인 주소로 수정해야 함
	    String serverIp = getLocalIp();
	    String uploadUrl = "http://" + serverIp + ":8080/video/upload";

	    // 4. HTTP 클라이언트 생성 (자동으로 close 처리)
	    try (CloseableHttpClient client = HttpClients.createDefault()) {
	        // 5. POST 방식의 요청 생성
	        HttpPost post = new HttpPost(uploadUrl);

	        // 6. multipart/form-data 본문 구성
	        //    - file: 실제 영상 파일
	        //    - broadcast_id: 방송 식별용 텍스트 값
	        HttpEntity entity = MultipartEntityBuilder.create()
	                .addBinaryBody("file", file, ContentType.DEFAULT_BINARY, file.getName())
	                .addTextBody("broadcast_id", String.valueOf(broadcastId), ContentType.TEXT_PLAIN)
	                .build();

	        // 7. 구성한 엔티티를 요청에 설정
	        post.setEntity(entity);

	        // 8. 서버 응답 처리 핸들러 정의
	        HttpClientResponseHandler<Void> responseHandler = (ClassicHttpResponse response) -> {
	            int status = response.getCode(); // 응답 상태 코드 (예: 200, 400, 500)
	            String responseBody = EntityUtils.toString(response.getEntity()); // 응답 본문

	            // 9. 업로드 성공 여부 로깅
	            if (status == 200) {
	                log.info("✅ 업로드 성공! 응답: {}", responseBody);
	            } else {
	                log.warn("❌ 업로드 실패 (HTTP {}): {}", status, responseBody);
	            }

	            return null; // 반환값 없음
	        };

	        // 10. 실제 요청 전송 + 응답 처리
	        client.execute(post, responseHandler);

	    } catch (IOException e) {
	        // 11. 네트워크 또는 I/O 예외 발생 시 로그 출력 + 예외 던지기
	        log.error("❌ 파일 업로드 중 예외 발생", e);
	        throw e;
	    }
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

}
