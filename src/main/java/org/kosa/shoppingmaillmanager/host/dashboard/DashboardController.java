package org.kosa.shoppingmaillmanager.host.dashboard;

import org.kosa.shoppingmaillmanager.host.product.dto.OrderStatusCountDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dashboard")
public class DashboardController {
	
	private final DashboardService dashboardService;
	
	@GetMapping("/order-status")
	public OrderStatusCountDto getOrderStatus(HttpServletRequest request) {
	    String userId = (String) request.getAttribute("userId");

	    return dashboardService.getOrderStatusCounts(userId);
	}

}
