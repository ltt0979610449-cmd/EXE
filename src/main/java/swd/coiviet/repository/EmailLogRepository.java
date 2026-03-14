package swd.coiviet.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import swd.coiviet.model.EmailLog;

import java.time.LocalDateTime;
import java.util.List;

public interface EmailLogRepository extends JpaRepository<EmailLog, Long> {
    Page<EmailLog> findByRecipientEmailContainingIgnoreCase(String recipientEmail, Pageable pageable);
    Page<EmailLog> findByTemplateType(String templateType, Pageable pageable);
    Page<EmailLog> findByStatus(EmailLog.EmailLogStatus status, Pageable pageable);

    @Query("SELECT e FROM EmailLog e WHERE " +
            "(:recipient IS NULL OR LOWER(e.recipientEmail) LIKE LOWER(CONCAT('%', :recipient, '%'))) AND " +
            "(:templateType IS NULL OR e.templateType = :templateType) AND " +
            "(:opened IS NULL OR (:opened = true AND e.openedAt IS NOT NULL) OR (:opened = false AND e.openedAt IS NULL)) AND " +
            "(:fromDate IS NULL OR e.sentAt >= :fromDate) AND " +
            "(:toDate IS NULL OR e.sentAt <= :toDate)")
    Page<EmailLog> findWithFilters(@Param("recipient") String recipient,
                                   @Param("templateType") String templateType,
                                   @Param("opened") Boolean opened,
                                   @Param("fromDate") LocalDateTime fromDate,
                                   @Param("toDate") LocalDateTime toDate,
                                   Pageable pageable);
}
