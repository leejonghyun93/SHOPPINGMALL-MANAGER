package org.kosa.shoppingmaillmanager.user;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kosa.shoppingmaillmanager.page.PageResponseVO;
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
	    if (dbUser == null || "Y".equals(dbUser.getSecession_yn())) {
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

	    if (!"HOST".equals(dbUser.getGrade_id()) &&
	    	!"ADMIN".equals(dbUser.getGrade_id())) {
	        throw new RuntimeException("로그인 권한이 없습니다.");
	    }
	    
	    if ("HOST".equals(dbUser.getGrade_id())) {
	    	if ("N".equals(dbUser.getApproved_yn())) {
	    		throw new RuntimeException("로그인 권한이 없습니다.");
	    	}
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


	public PageResponseVO<UserListDTO> userList(UserListDTO dto) {
		System.out.println(dto.getFilterType());
		dto.applyFilterType(); // 필터 타입에 따라 조건 자동 설정
		
		int start = (dto.getPageNo() - 1) * dto.getSize();
		
		Map<String, Object> map = new HashMap<>();
		map.put("start", start);
		map.put("size", dto.getSize());
		map.put("searchColumn" ,dto.getSearchColumn());
		map.put("searchValue", dto.getSearchValue());
		map.put("sortOption", dto.getSortOption());
		map.put("user_id", dto.getUser_id());
		map.put("name", dto.getName());
		map.put("nickname", dto.getNickname());
		map.put("email", dto.getEmail());
		map.put("phone", dto.getPhone());
		map.put("address", dto.getAddress());
		map.put("gender", dto.getGender());
		map.put("created_date", dto.getCreated_date());
		map.put("blacklisted", dto.getBlacklisted());
		map.put("status", dto.getStatus());
		map.put("grade_id", dto.getGrade_id());
		map.put("secession_yn", dto.getSecession_yn());
		map.put("secession_date", dto.getSecession_date());
		
		// 역할별 필터링 처리
	    if (dto.isExcludeAdminAndHost()) {
	        map.put("excludeGrades", List.of("ADMIN", "HOST"));
	    } else if (dto.getGrade_id() != null) {
	        map.put("grade_id", dto.getGrade_id());
	    }
	    
	    map.put("blacklisted", dto.getBlacklisted());
	    map.put("status", dto.getStatus());
	    map.put("filterType", dto.getFilterType());
		
		List<UserListDTO> list = userDAO.getUserList(map);
	    int total = userDAO.countUserList(map);
	    System.out.println("🧾 조건 map: " + map);
	    return new PageResponseVO<>(dto.getPageNo(), list, total, dto.getSize());
	}

	@Transactional
	public boolean updateUser(User user) {
		try {
			userDAO.updateUser(user);
			userDAO.updateHost(user);
			return true;
		} catch (Exception e) {
			log.info("수정 실패");
			return false;
		}
		
	}


	public boolean secessionUser(String user_id, String secession_yn) {
		return userDAO.secessionUser(user_id, secession_yn) > 0;
	}

	public int setBlacklistStatus(List<String> userIds, String blacklisted) {
	    return userDAO.updateBlacklistStatus(userIds, blacklisted);
	}


	public int setUnlockStatus(List<String> userIds, String status) {
		return userDAO.updateUnlockStatus(userIds, status);
	}
	
	
}
