package org.kosa.shoppingmaillmanager.user;

import org.apache.ibatis.annotations.Mapper;

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

}
