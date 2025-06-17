package org.kosa.shoppingmaillmanager.user;

import java.util.Map;

import org.kosa.shoppingmaillmanager.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {
	
	private final UserDAO userDAO;
	private final BCryptPasswordEncoder passwordEncoder;
	private final JwtUtil jwtUtil;
	
	
//	public ResponseEntity<?> login(@RequestBody User user) {
//		
//	    String userId = user.getUser_id();
//	    String password = user.getPassword();
//
//	    if (userId == null || userId.trim().isEmpty()) {
//	    	return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//	                .body(Map.of("error", "아이디를 입력해주세요"));
//	    }
//	    
//	    if (password == null || password.trim().isEmpty()) {
//	    	return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//	                .body(Map.of("error", "비밀번호를 입력해주세요"));
//	    }
//	    
//	    // DB에서 해당 userId로 유저 조회
//	    User dbUser = userDAO.getUser(userId);
//	    
//	    // 유저가 없거나 비밀번호가 틀릴 경우 → 401 Unauthorized
//	    if (dbUser == null ||  !passwordEncoder.matches(password, dbUser.getPassword())) {
//	    	userDAO.increaseFailCount(userId); // 실패 횟수 +1
//	        int failCount = userDAO.getFailCount(userId); // 현재 실패 횟수
//
//	        if (failCount >= 5) {
//	            userDAO.lockUser(userId); // 계정 잠금 처리
//	            return ResponseEntity.status(HttpStatus.FORBIDDEN)
//	                    .body(Map.of("error", "로그인 5회 실패로 계정이 잠겼습니다."));
//	        } else {
//	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//	                    .body(Map.of("error", "아이디 또는 비밀번호가 잘못되었습니다 (" + failCount + "/5)"));
//	        }
//	    }
//
//	    // 승인되지 않은 사용자 → 403 Forbidden
//	    if ("N".equals(dbUser.getApproved_yn())) {
//	        return ResponseEntity.status(HttpStatus.FORBIDDEN)
//	                .body(Map.of("error", "로그인 권한이 없습니다."));
//	    }
//
//	    // 계정이 잠긴 사용자 → 403 Forbidden
//	    if ("N".equals(dbUser.getStatus())) {
//	        return ResponseEntity.status(HttpStatus.FORBIDDEN)
//	                .body(Map.of("error", "계정이 잠겨 있습니다."));
//	    }
//	    
//	    // 블랙리스트에 등록된 사용자 → 403 Forbidden
//	    if ("Y".equals(dbUser.getBlacklisted())) {
//	        return ResponseEntity.status(HttpStatus.FORBIDDEN)
//	                .body(Map.of("error", "사이트를 이용할 수 없습니다."));
//	    }
//
//	    // 로그인 성공 시 → JWT 토큰 생성
//	    String token = jwtUtil.generateToken(userId);
//	    userDAO.resetFailCount(userId);
//	    userDAO.setLoginTime(userId);
//	    
//	    dbUser.setPassword(null);
//	    
//	    // JWT와 사용자 정보 반환
//	    return ResponseEntity.ok(Map.of(
//	            "token", token,
//	            "user", dbUser
//	    ));
//	}

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

	    if ("N".equals(dbUser.getApproved_yn())) {
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


}
