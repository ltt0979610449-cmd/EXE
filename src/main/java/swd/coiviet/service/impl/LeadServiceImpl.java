package swd.coiviet.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import swd.coiviet.dto.request.CreateLeadRequest;
import swd.coiviet.dto.request.UpdateLeadRequest;
import swd.coiviet.enums.LeadSource;
import swd.coiviet.enums.LeadStatus;
import swd.coiviet.exception.AppException;
import swd.coiviet.exception.ErrorCode;
import swd.coiviet.model.Lead;
import swd.coiviet.model.Tour;
import swd.coiviet.repository.LeadRepository;
import swd.coiviet.service.LeadService;
import swd.coiviet.service.TourService;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class LeadServiceImpl implements LeadService {
    private final LeadRepository leadRepository;
    private final TourService tourService;

    public LeadServiceImpl(LeadRepository leadRepository, TourService tourService) {
        this.leadRepository = leadRepository;
        this.tourService = tourService;
    }

    @Override
    public Lead create(CreateLeadRequest request) {
        Tour tour = null;
        if (request.getTourId() != null) {
            tour = tourService.findById(request.getTourId()).orElse(null);
        }

        Lead lead = Lead.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .tour(tour)
                .message(request.getMessage())
                .source(request.getSource() != null ? request.getSource() : LeadSource.WEBSITE)
                .status(LeadStatus.NEW)
                .createdAt(LocalDateTime.now())
                .build();
        return leadRepository.save(lead);
    }

    @Override
    public Lead update(Long id, UpdateLeadRequest request) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Lead không tồn tại"));
        if (request.getStatus() != null) {
            lead.setStatus(request.getStatus());
        }
        if (request.getAdminNote() != null) {
            lead.setAdminNote(request.getAdminNote());
        }
        lead.setUpdatedAt(LocalDateTime.now());
        return leadRepository.save(lead);
    }

    @Override
    public Page<Lead> findAll(LeadStatus status, Long tourId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return leadRepository.findWithFilters(status, tourId, pageable);
    }

    @Override
    public Lead findById(Long id) {
        return leadRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Lead không tồn tại"));
    }
}
