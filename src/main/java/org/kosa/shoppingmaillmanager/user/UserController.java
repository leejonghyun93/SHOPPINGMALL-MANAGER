package org.kosa.shoppingmaillmanager.user;

import java.util.HashMap;
import java.util.Map;

import org.kosa.shoppingmaillmanager.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
public class UserController {
	
	private final UserService userService;
	private final JwtUtil jwtUtil;
	private final BCryptPasswordEncoder passwordEncoder;
	
	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody User user) {
	    try {
	        User dbUser = userService.login(user.getUser_id(), user.getPassword());
	        String token = jwtUtil.generateToken(dbUser.getUser_id());

	        // DTO 변환
	        LoginUserDTO dto = new LoginUserDTO(dbUser);

	        return ResponseEntity.ok(Map.of(
	            "token", token,
	            "user", dto
	        ));

	    } catch (IllegalArgumentException e) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                .body(Map.of("error", e.getMessage()));
	    } catch (RuntimeException e) {
	        return ResponseEntity.status(HttpStatus.FORBIDDEN)
	                .body(Map.of("error", e.getMessage()));
	    }
	}
	
	@GetMapping("/login/findId")
	public ResponseEntity<?> findId(@RequestParam String name,
		    @RequestParam String email) {

	    User user = userService.findByNameAndEmail(name, email);
	    if (user != null) {
	        return ResponseEntity.ok(Map.of("user_Id", user.getUser_id()));
	    } else {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("일치하는 정보 없음");
	    }
	}
	
	@GetMapping("/login/findPassword")
	public ResponseEntity<?> findPassword(@RequestParam String user_id,
		    @RequestParam String email) {

	    User user = userService.findByUserIdAndEmail(user_id, email);
	    if (user != null) {
	        return ResponseEntity.ok(Map.of("user_id", user.getUser_id()));
	    } else {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("일치하는 정보 없음");
	    }
	}
	
	@PutMapping("/login/changePassword")
	public ResponseEntity<?> changePassword(@RequestBody Map<String, String> payload) {
	    String userId = payload.get("user_id");
	    String newPassword = payload.get("newPassword");

	    String encodedPw = passwordEncoder.encode(newPassword);
	    userService.updatePassword(userId, encodedPw);
	    return ResponseEntity.ok().build();
	}
	
	@PostMapping("/host/register")
	@ResponseBody
	public Map<String, Object> register(@RequestBody User user){
		Map<String, Object> result = new HashMap<String, Object>();
		if(!userService.isValid(user)) {
			result.put("errorMessage", "입력값 검증 오류 발생");
			result.put("status", "error");
		} else {
			userService.registerHost(user);
			result.put("status", "ok");
		}
		return result;
	}
	
	@PostMapping("/host/isExistUserId")
	public Map<String, Object> isExistUserId(@RequestBody User user){
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("existUserId", userService.getUser(user.getUser_id()) != null);
		return map;
	}
	
}
