package org.kosa.shoppingmaillmanager.user;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {
	
	private final UserDAO userDAO;
	private final BCryptPasswordEncoder passwordEncoder;

	public User login(String userId, String password) {

	    if (userId == null || userId.trim().isEmpty()) {
	        throw new IllegalArgumentException("아이디를 입력해주세요");
	    }

	    if (password == null || password.trim().isEmpty()) {
	        throw new IllegalArgumentException("비밀번호를 입력해주세요");
	    }

	    User dbUser = userDAO.getUser(userId);
	    if (dbUser == null) {
	    	throw new RuntimeException("아이디가 존재하지 않습니다.");
	    }
	    if (!passwordEncoder.matches(password, dbUser.getPassword())) {
	        userDAO.increaseFailCount(userId);
	        Integer failCount = userDAO.getFailCount(userId);

	        if (failCount >= 5) {
	            userDAO.lockUser(userId);
	            throw new RuntimeException("로그인 5회 실패로 계정이 잠겼습니다.");
	        } else {
	            throw new RuntimeException("비밀번호가 잘못되었습니다 (" + failCount + "/5)");
	        }
	    }

	    if ("N".equals(dbUser.getApproved_yn()) || !"HOST".equals(dbUser.getGrade_id())) {
	        throw new RuntimeException("로그인 권한이 없습니다.");
	    }

	    if ("N".equals(dbUser.getStatus())) {
	        throw new RuntimeException("계정이 잠겨 있습니다.");
	    }

	    if ("Y".equals(dbUser.getBlacklisted())) {
	        throw new RuntimeException("사이트를 이용할 수 없습니다.");
	    }

	    userDAO.resetFailCount(userId);
	    userDAO.setLoginTime(userId);

	    return dbUser;
	}
	
	
	@Transactional
	public void registerHost(User user) {
		String encryptedPassword = passwordEncoder.encode(user.getPassword());
		user.setPassword(encryptedPassword);
		
		if(user.getGrade_id() == null || user.getGrade_id().isEmpty()) {
			user.setGrade_id("HOST");
		}
		
	    userDAO.insertMember(user); // 먼저 tb_member에 넣고
	    userDAO.insertHost(user);   // 그다음 tb_host에 넣기
	}

	public boolean isValid(User user) {
		if (user.getUser_id() == null || user.getUser_id().length() == 0 || user.getUser_id().length() < 8) return false;
		if (user.getPassword() == null || user.getPassword().length() == 0 || isValidPasswd(user.getPassword()) == false) return false;
		if (user.getName() == null || user.getName().length() == 0) return false;
		if (user.getEmail() == null || user.getEmail().length() == 0) return false;
		return true;
	}

	private boolean isValidPasswd(String password) {
		String pattern = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,}$";
	    return password != null && password.matches(pattern);
	}

	public User getUser(String user_id) {
		// TODO Auto-generated method stub
		return userDAO.getUser(user_id);
	}


	public User findByNameAndEmail(String name, String email) {
		return userDAO.findByNameAndEmail(name, email);
	}


	public User findByUserIdAndEmail(String userId, String email) {
		return userDAO.findByUserIdAndEmail(userId, email);
	}


	public void updatePassword(String userId, String encodedPw) {
		userDAO.updatePassword(userId, encodedPw);
	}


}
