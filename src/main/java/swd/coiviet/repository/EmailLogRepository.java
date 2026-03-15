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
            "(:recipient IS NULL OR LOWER(e.recipientEmail) LIKE LOWER(CONCAT('%', CAST(:recipient AS string), '%'))) AND " +
            "(:templateType IS NULL OR e.templateType = :templateType) AND " +
            "(:openedFilter = 0 OR (:openedFilter = 1 AND e.openedAt IS NOT NULL) OR (:openedFilter = 2 AND e.openedAt IS NULL)) AND " +
            "e.sentAt >= :fromDate AND e.sentAt <= :toDate")
    Page<EmailLog> findWithFilters(@Param("recipient") String recipient,
                                   @Param("templateType") String templateType,
                                   @Param("openedFilter") int openedFilter,
                                   @Param("fromDate") LocalDateTime fromDate,
                                   @Param("toDate") LocalDateTime toDate,
                                   Pageable pageable);
}
