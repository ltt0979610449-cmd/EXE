package swd.coiviet.service;

import org.springframework.data.domain.Page;
import swd.coiviet.dto.request.CreateLeadRequest;
import swd.coiviet.dto.request.UpdateLeadRequest;
import swd.coiviet.enums.LeadStatus;
import swd.coiviet.model.Lead;

public interface LeadService {
    Lead create(CreateLeadRequest request);
    Lead update(Long id, UpdateLeadRequest request);
    Page<Lead> findAll(LeadStatus status, Long tourId, int page, int size);
    Lead findById(Long id);
}
