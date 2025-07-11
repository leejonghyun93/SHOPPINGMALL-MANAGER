package org.kosa.shoppingmaillmanager.host.dashboard;

import java.util.List;
import java.util.Map;

import org.kosa.shoppingmaillmanager.host.product.HostDAO;
import org.kosa.shoppingmaillmanager.host.product.dto.MonthlySalesSummaryDto;
import org.kosa.shoppingmaillmanager.host.product.dto.OrderStatusCountDto;
import org.kosa.shoppingmaillmanager.host.product.dto.SalesOrderItemDto;
import org.kosa.shoppingmaillmanager.host.product.dto.SalesSummaryDto;
import org.kosa.shoppingmaillmanager.host.product.dto.TopProductDto;
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

	// 최근 30일 일별 매출
	public List<SalesSummaryDto> getDailySalesSummary(String userId) {
		String hostId = hostDAO.findHostIdByUserId(userId);
		return dashboardDAO.getDailySalesSummary(hostId);
	}

	// 최근 12개월 월별 매출
	public List<SalesSummaryDto> getMonthlySalesSummary(String userId) {
		String hostId = hostDAO.findHostIdByUserId(userId);
		return dashboardDAO.getMonthlySalesSummary(hostId);
	}

	public MonthlySalesSummaryDto getSalesSummaryCard(String userId, String month) {
		String hostId = hostDAO.findHostIdByUserId(userId);
		return dashboardDAO.getSalesSummaryCard(hostId, month);
	}

	public List<TopProductDto> getTop5ProductsByQuantity(String userId) {
		String hostId = hostDAO.findHostIdByUserId(userId);
		return dashboardDAO.getTop5ByQuantity(hostId);
	}

	public List<TopProductDto> getTop5ProductsBySales(String userId) {
		String hostId = hostDAO.findHostIdByUserId(userId);
		return dashboardDAO.getTop5BySales(hostId);
	}

	public Map<String, Integer> getPaymentMethodCounts(String userId) {
		return dashboardDAO.countPaymentMethods(userId);
	}

	public List<SalesOrderItemDto> getSalesOrderItems(String userId, String startDate, String endDate,
			String productKeyword, String paymentMethodKeyword) {
		String hostId = hostDAO.findHostIdByUserId(userId);
		return dashboardDAO.findSalesOrderItems(hostId, startDate, endDate, productKeyword, paymentMethodKeyword);
	}
}
