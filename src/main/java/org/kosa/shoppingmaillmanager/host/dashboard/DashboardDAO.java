package org.kosa.shoppingmaillmanager.host.dashboard;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.kosa.shoppingmaillmanager.host.product.dto.OrderStatusCountDto;

@Mapper
public interface DashboardDAO {
    OrderStatusCountDto getOrderStatusCounts(@Param("hostId") String hostId);
}