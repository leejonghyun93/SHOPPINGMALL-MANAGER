package org.kosa.shoppingmaillmanager.member;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, String> {
    // USER_ID 기준으로 조회 (PK이므로 기본 제공됨)
    // 필요한 경우 닉네임으로도 조회 가능
    Member findByUserId(String userId);
}