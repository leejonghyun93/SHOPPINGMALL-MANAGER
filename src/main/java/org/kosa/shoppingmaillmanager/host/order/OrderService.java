package org.kosa.shoppingmaillmanager.host.order;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kosa.shoppingmaillmanager.page.PageResponseVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderService {
	private final OrderDAO orderDAO;

	public PageResponseVO<OrderListDTO> list(
			Integer host_id, String searchColumn, String searchValue, int pageNo, int size, String startDate, String endDate, 
			List<String> order_status, List<String> payment_method, String recipient_name, String recipient_phone, 
			String order_address_detail, String user_name, String user_phone, String user_email, String sortOption) {
		
		int start = (pageNo - 1) * size;
		
		Map<String, Object> map = new HashMap<>();
		map.put("host_id", host_id);
		map.put("start", start);
		map.put("size", size);
		map.put("searchColumn", searchColumn);
		map.put("searchValue", searchValue);
		map.put("startDate", startDate);
		map.put("endDate", endDate);
		map.put("order_status", order_status);
		map.put("payment_method", payment_method);
		map.put("recipient_name", recipient_name);
		map.put("recipient_phone", recipient_phone);
		map.put("order_address_detail",order_address_detail);
		map.put("user_name", user_name);
		map.put("user_phone", user_phone);
		map.put("user_email", user_email);
		map.put("sortOption", sortOption);
		
		List<String> orderIdList = orderDAO.getPagedOrderIds(map);
		
		List<OrderListDTO> orders = Collections.emptyList();
		
		if(!orderIdList.isEmpty()) {
			map.put("orderIdList", orderIdList);
			orders = orderDAO.list(map);
		}
		
		Integer count = orderDAO.getTotalCount(map);
		int totalCount = (count != null) ? count : 0;
		
		return new PageResponseVO<>(pageNo, orders, totalCount, size);
	}

	public OrderDetailDTO getOrder(String order_id) {
		return orderDAO.getOrder(order_id);
	}

	public boolean updateRecipient(OrderDetailDTO dto) {
		return orderDAO.updateRecipient(dto) > 0;
		
	}
	@Transactional
	public boolean cancelOrder(String order_id) {
		int result1 = orderDAO.updateOrderStatusToCancelled(order_id);
		int result2 = orderDAO.updateOrderItemStatusToCancelled(order_id);
        return result1 > 0 && result2 > 0;
	}

	public void cancelOrders(List<String> order_ids) {
		orderDAO.updateOrderStatusMutilCancelled(order_ids);
	}

	public List<OrderByUserDTO> getOrderByUser(String user_id) {
		return orderDAO.getOrderByUser(user_id);
	}
}

