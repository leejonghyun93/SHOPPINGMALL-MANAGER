package org.kosa.shoppingmaillmanager.obswebsocket;

import io.obswebsocket.community.client.OBSRemoteController;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * OBS와의 WebSocket 연결을 관리하는 컴포넌트 클래스
 * - Spring 부팅 시 자동 연결
 * - Spring 종료 시 자동 해제
 */
@Slf4j
@Component // Spring Boot에서 자동으로 관리되는 Bean으로 등록됨
public class OBSWebSocketManager {

    private OBSRemoteController controller; // OBS와 통신할 주 객체 (명령 전송용)
    private boolean connected = false;      // 현재 연결 상태를 추적하기 위한 플래그

    /**
     * 애플리케이션이 시작될 때 자동으로 호출되는 초기화 메서드
     * OBS WebSocket에 연결을 시도하고, 연결 이벤트를 등록함
     */
    @PostConstruct
    public void init() {
        // OBSRemoteController를 builder 패턴으로 생성
        this.controller = OBSRemoteController.builder()
                .host("192.168.4.176") // OBS가 실행되고 있는 주소 (보통 본인 PC이므로 localhost)
                .port(4455)        // OBS WebSocket 포트 번호 (기본값은 4455)
                .password("WmNWz4vAllzTMOnl") // WebSocket 연결 시 사용할 비밀번호 (없으면 생략 가능)
                .lifecycle() // 연결/해제에 대한 이벤트 핸들러 등록 시작
                    .onConnect(ctx -> {
                        // 연결 성공 시 호출되는 람다 함수
                        log.info("✅ OBS WebSocket 연결 성공");
                        connected = true; // 연결 상태 플래그 true로 설정
                    })
                    .onDisconnect(() -> {
                        log.info("❌ OBS 연결 해제");
                        connected = false;
                    })
                    .and() // lifecycle 설정 종료
                .build(); // 설정 완료 후 controller 인스턴스 생성

        controller.connect(); // WebSocket 연결 시도 (비동기 처리됨)
    }

    /**
     * 애플리케이션이 종료되기 직전에 호출됨
     * OBS와의 WebSocket 연결을 안전하게 종료함
     */
    @PreDestroy
    public void destroy() {
        if (controller != null) {
            // 연결 여부 관계없이 disconnect 호출해도 안전함
            controller.disconnect();
        }
    }

    /**
     * 외부에서 controller 객체에 접근할 수 있도록 getter 제공
     */
    public OBSRemoteController getController() {
        return controller;
    }

    /**
     * 현재 OBS WebSocket 연결 여부를 외부에서 확인할 수 있도록 제공
     */
    public boolean isConnected() {
        return connected;
    }
}