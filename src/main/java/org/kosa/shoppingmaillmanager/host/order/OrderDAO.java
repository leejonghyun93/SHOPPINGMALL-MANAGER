package org.kosa.shoppingmaillmanager.host.order;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderDAO {
	public List<OrderListDTO> list(Map<String, Object> map);
	public int getTotalCount(Map<String, Object> map);
	public OrderDetailDTO getOrder(String order_id);
	public int updateRecipient(OrderDetailDTO dto);
	public int updateOrderStatusToCancelled(String order_id);
	public void updateOrderStatusMutilCancelled(List<String> order_ids);
	public List<String> getPagedOrderIds(Map<String, Object> map);
	public List<OrderItemDTO> getOrderItemsByOrderId(String order_id);
	public int updateOrderItemStatusToCancelled(String order_id);
	public List<OrderByUserDTO> getOrderByUser(String user_id);
}
