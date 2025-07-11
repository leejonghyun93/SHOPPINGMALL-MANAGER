package org.kosa.shoppingmaillmanager.member;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/members")
public class MemberController {
	
	private final MemberService memberService;
	
	@GetMapping("/me/{broadcastId}")
	public ResponseEntity<MemberDto> getMyInfoWithBroadcast(
	        @PathVariable String broadcastId,
	        HttpServletRequest request) {		

	    String userId = (String) request.getAttribute("userId");
	    if (userId == null) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	        
	    }
	    
	    MemberDto dto = memberService.getMemberWithBroadcast(userId, broadcastId);
	    
	    return ResponseEntity.ok(dto);
	}
}
