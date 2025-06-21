package org.kosa.shoppingmaillmanager.host.order;

import java.util.HashMap;
import java.util.Map;

import org.kosa.shoppingmaillmanager.page.PageResponseVO;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderService {
	private final OrderDAO orderDAO;

	public PageResponseVO<OrderListDTO> list(String searchValue, int pageNo, int size) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("start", (pageNo-1) * size);
		map.put("size", pageNo * size);
		map.put("searchValue", searchValue);
		return new PageResponseVO<OrderListDTO>(pageNo,
				orderDAO.list(map),
				orderDAO.getTotalCount(map),
				size);
	}

	public OrderDetailDTO getOrder(String order_id) {
		return orderDAO.getOrder(order_id);
	}
}
