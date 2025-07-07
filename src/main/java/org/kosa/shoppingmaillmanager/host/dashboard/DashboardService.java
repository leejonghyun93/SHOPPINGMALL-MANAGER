package org.kosa.shoppingmaillmanager.host.dashboard;

import org.kosa.shoppingmaillmanager.host.product.HostDAO;
import org.kosa.shoppingmaillmanager.host.product.dto.OrderStatusCountDto;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardService {

	private final DashboardDAO dashboardDAO;
	private final HostDAO hostDAO;
	
	public OrderStatusCountDto getOrderStatusCounts(String userId) {
		String hostId = hostDAO.findHostIdByUserId(userId);
		return dashboardDAO.getOrderStatusCounts(hostId);
	}
}
