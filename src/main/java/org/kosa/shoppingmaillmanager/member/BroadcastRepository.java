package org.kosa.shoppingmaillmanager.member;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BroadcastRepository extends JpaRepository<Broadcast, String> {

	boolean existsByBroadcastIdAndBroadcaster_UserId(String broadcastId, String userId);

}