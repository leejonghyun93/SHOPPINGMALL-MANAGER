package org.kosa.shoppingmaillmanager.host.order;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderDAO {
	public List<OrderListDTO> list(Map<String, Object> map);
	public int getTotalCount(Map<String, Object> map);
	public OrderDetailDTO getOrder(String order_id);
}
