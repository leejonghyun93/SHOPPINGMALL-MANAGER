package org.kosa.shoppingmaillmanager.user;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserDAO {
	
	public int insertMember(User user);
	public int insertHost(User user);
	public User getUser(String user_id);
	public void setLoginTime(String userId);
	public void increaseFailCount(String userId);
	public Integer getFailCount(String userId);
	public void lockUser(String userId);
	public void resetFailCount(String userId);
	public User findByNameAndEmail(@Param("name") String name, @Param("email") String email);
	public User findByUserIdAndEmail(@Param("user_id") String user_id, @Param("email") String email);
	public int updatePassword(@Param("user_id") String userId, @Param("password") String encodedPw);
	public List<UserListDTO> getUserList(Map<String, Object> map);
	public int countUserList(Map<String, Object> map);
	public int updateUser(User user);
	public int secessionUser(String user_id);
	public void updateHost(User user);
}
