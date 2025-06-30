package org.kosa.shoppingmaillmanager.obswebsocket;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * 프론트엔드(Vue 등)에서 호출할 수 있는 REST API를 제공하는 컨트롤러
 */
@RestController
@RequestMapping("/api/obs") // 이 컨트롤러의 기본 URL 경로
public class OBSController {

    private final OBSControlService obsControlService;

    // 생성자 주입으로 서비스 연결
    public OBSController(OBSControlService obsControlService) {
        this.obsControlService = obsControlService;
    }

    /**
     * 방송 시작 API
     * POST 요청: /api/obs/start
     */
    @PostMapping("/start")
    public String start() {
        obsControlService.startStreaming(); // 서비스에 시작 명령 위임
        return "방송 시작됨";
    }

    /**
     * 방송 종료 API
     * POST 요청: /api/obs/stop
     */
    @PostMapping("/stop")
    public String stop() {
        obsControlService.stopStreaming(); // 서비스에 종료 명령 위임
        return "방송 종료됨";
    }
}