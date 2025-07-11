package org.kosa.shoppingmaillmanager.member;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;



public interface MemberRepository extends JpaRepository<Member, String> {
    // USER_ID 기준으로 조회 (PK이므로 기본 제공됨)
    // 필요한 경우 닉네임으로도 조회 가능
    Member findByUserId(String userId);
    
    @Query(value = "SELECT COUNT(*) > 0 FROM tb_live_broadcasts WHERE broadcaster_id = :userId AND broadcast_id = :broadcastId", nativeQuery = true)
    boolean isUserHostOfBroadcast(@Param("userId") String userId, @Param("broadcastId") String broadcastId);
}