package org.kosa.shoppingmaillmanager.host.broadcast;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.kosa.shoppingmaillmanager.page.PageResponseVO;
import org.kosa.shoppingmaillmanager.security.AESUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.obswebsocket.community.client.OBSRemoteController;
import io.obswebsocket.community.client.message.request.record.StartRecordRequest;
import io.obswebsocket.community.client.message.request.record.StopRecordRequest;
import io.obswebsocket.community.client.message.request.stream.StartStreamRequest;
import io.obswebsocket.community.client.message.request.stream.StopStreamRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BroadCastService {

	private final BroadCastDAO broadCastDAO;
	
	private final ViewerRedisService redisService;
	
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
	            String hls_url = "http://" + broadCast.getNginx_host() + ":8090/live/" + rawKey + "_720p2628kbs/index.m3u8";
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
     * 방송 시작 요청 처리
     * - broadcasterId를 기반으로 연결 정보를 조회
     * - 해당 IP/PORT/PASSWORD로 OBS WebSocket 연결 생성
     * - 연결 성공 시 방송 및 녹화 시작 명령을 전송
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
	    OBSRemoteController client = OBSRemoteController.builder()
	        .host(broadCast.getObs_host())      // 방송자의 OBS가 실행 중인 PC IP
	        .port(broadCast.getObs_port())      // OBS WebSocket 포트 (보통 4455)
	        .password(password)                 // OBS WebSocket 연결 비밀번호
	        .lifecycle()                        // 연결 생명주기 콜백 등록 시작
	            .onIdentified(ctx -> {
	                // 5. IDENTIFIED 이벤트 발생 시 (비밀번호 인증 성공)
	                OBSRemoteController c = ref.get(); // 안전하게 참조
	                if (c == null) {
	                    log.error("❌ client is null in onIdentified");
	                    return;
	                }

	                log.info("🟢 IDENTIFIED - 방송자 [{}]", broadcast_id);

	                // 6. 방송 시작 요청
	                c.sendRequest(
	                    StartStreamRequest.builder().build(),
	                    res -> log.info("✅ 방송 시작 응답: {}", res)
	                );

	                // 7. 녹화 시작 요청
	                c.sendRequest(
	                    StartRecordRequest.builder().build(),
	                    res -> log.info("🎥 녹화 시작 응답: {}", res)
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
	
    /**
     * 방송 종료 요청 처리
     * - broadcasterId를 기반으로 연결 정보를 조회
     * - 해당 IP/PORT/PASSWORD로 OBS WebSocket 연결 생성
     * - 연결 성공 시 방송 및 녹화 종료 명령을 전송
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
 	    OBSRemoteController client = OBSRemoteController.builder()
 	        .host(broadCast.getObs_host())      // 방송자의 OBS가 실행 중인 PC IP
 	        .port(broadCast.getObs_port())      // OBS WebSocket 포트 (보통 4455)
 	        .password(password)                 // OBS WebSocket 연결 비밀번호
 	        .lifecycle()                        // 연결 생명주기 콜백 등록 시작
 	            .onIdentified(ctx -> {
 	                // 5. IDENTIFIED 이벤트 발생 시 (비밀번호 인증 성공)
 	                OBSRemoteController c = ref.get(); // 안전하게 참조
 	                if (c == null) {
 	                    log.error("❌ client is null in onIdentified");
 	                    return;
 	                }

 	                log.info("🟢 IDENTIFIED - 방송자 [{}]", broadcast_id);

 	                // 6. 방송 시작 요청
 	                c.sendRequest(
 	                    StopStreamRequest.builder().build(),
 	                    res -> log.info("🛑 방송 종료 응답: {}", res)
 	                );

 	                // 7. 녹화 시작 요청
 	                c.sendRequest(
 	                    StopRecordRequest.builder().build(),
 	                    res -> log.info("⏹ 녹화 종료 응답: {}", res)
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
}
