package org.kosa.shoppingmaillmanager.member;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {
	
	private final MemberRepository memberRepository;
	
	public MemberDto getMemberById(String userId) {
        Member member = memberRepository.findByUserId(userId);

        if (member == null) {
            throw new IllegalArgumentException("회원 정보를 찾을 수 없습니다: " + userId);
        }

        return MemberDto.builder()
                .userId(member.getUserId())
                .nickname(member.getNickname())
                .build();
    }
}
