package swd.coiviet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swd.coiviet.model.ChatConversation;

import java.util.List;
import java.util.Optional;

public interface ChatConversationRepository extends JpaRepository<ChatConversation, Long> {
    Optional<ChatConversation> findByCustomerIdAndArtisanId(Long customerId, Long artisanId);

    List<ChatConversation> findByCustomerIdOrArtisanIdOrderByUpdatedAtDesc(Long customerId, Long artisanId);
}
