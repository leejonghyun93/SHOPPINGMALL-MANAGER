package org.kosa.shoppingmaillmanager.host.product;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface HostDAO {

    @Select("SELECT host_id FROM tb_host WHERE user_id = #{userId}")
    String findHostIdByUserId(String userId);

}
