package org.kosa.shoppingmaillmanager.obswebsocket;

import io.obswebsocket.community.client.message.request.record.StartRecordRequest;
import io.obswebsocket.community.client.message.request.record.StopRecordRequest;
import io.obswebsocket.community.client.message.request.stream.StartStreamRequest;
import io.obswebsocket.community.client.message.request.stream.StopStreamRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * OBS 방송 시작 및 종료를 제어하는 서비스 클래스
 * WebSocket 연결 상태를 확인하고 OBS에 명령을 전송함
 */
@Slf4j
@Service // Spring에서 이 클래스를 서비스 레이어로 자동 등록
public class OBSControlService {

    private final OBSWebSocketManager obsWebSocketManager;

    // 생성자 주입으로 WebSocketManager를 받아옴
    public OBSControlService(OBSWebSocketManager obsWebSocketManager) {
        this.obsWebSocketManager = obsWebSocketManager;
    }

    /**
     * OBS에 방송 시작 요청을 보냄
     */
    public void startStreaming() {
        // 연결 상태 확인
        if (obsWebSocketManager.isConnected()) {
        	
            // 방송 시작 요청 객체를 생성해서 WebSocket으로 전송
            obsWebSocketManager.getController()
                    .sendRequest(
                        StartStreamRequest.builder().build(), // 전송할 요청 객체
                        response -> {
                            // 응답 도착 시 호출되는 콜백
                            log.info("✅ 방송 시작 요청 응답: " + response);
                        }
                    );
            
            // 녹화 시작 요청 객체를 생성해서 WebSocket으로 전송
            obsWebSocketManager.getController().sendRequest(
                StartRecordRequest.builder().build(),
                response -> log.info("🎥 녹화 시작 응답: {}", response)
            );
        } else {
            // 연결되어 있지 않으면 예외 발생
            throw new IllegalStateException("OBS에 연결되어 있지 않습니다.");
        }
    }

    /**
     * OBS에 방송 종료 요청을 보냄
     */
    public void stopStreaming() {
        // 연결 상태 확인
        if (obsWebSocketManager.isConnected()) {
            // 방송 종료 요청 객체를 생성해서 WebSocket으로 전송
            obsWebSocketManager.getController()
                    .sendRequest(
                        StopStreamRequest.builder().build(), // 전송할 요청 객체
                        response -> {
                            // 응답 도착 시 호출되는 콜백
                            log.info("✅ 방송 종료 요청 응답: " + response);
                        }
                    );
            
         // 녹화 종료 요청 객체를 생성해서 WebSocket으로 전송
            obsWebSocketManager.getController().sendRequest(
                StopRecordRequest.builder().build(),
                response -> log.info("🎥 녹화 종료 응답: {}", response)
            );
        } else {
            // 연결되어 있지 않으면 예외 발생
            throw new IllegalStateException("OBS에 연결되어 있지 않습니다.");
        }
    }
}