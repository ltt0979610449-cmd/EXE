package swd.coiviet.service.impl;

import org.springframework.stereotype.Service;
import swd.coiviet.enums.CultureCategory;
import swd.coiviet.exception.AppException;
import swd.coiviet.exception.ErrorCode;
import swd.coiviet.enums.PublicationStatus;
import swd.coiviet.model.CultureItem;
import swd.coiviet.model.Tour;
import swd.coiviet.model.TourCultureItem;
import swd.coiviet.repository.TourCultureItemRepository;
import swd.coiviet.service.CultureItemService;
import swd.coiviet.service.TourCultureItemService;
import swd.coiviet.service.TourService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TourCultureItemServiceImpl implements TourCultureItemService {
    private final TourCultureItemRepository repository;
    private final TourService tourService;
    private final CultureItemService cultureItemService;

    public TourCultureItemServiceImpl(TourCultureItemRepository repository,
                                      TourService tourService,
                                      CultureItemService cultureItemService) {
        this.repository = repository;
        this.tourService = tourService;
        this.cultureItemService = cultureItemService;
    }

    @Override
    public TourCultureItem save(TourCultureItem item) {
        return repository.save(item);
    }

    @Override
    public List<TourCultureItem> findByTourId(Long tourId) {
        return repository.findByTourIdOrderByDisplayOrderAsc(tourId);
    }

    @Override
    public List<CultureItem> findCultureItemsByTourId(Long tourId) {
        List<CultureItem> linked = repository.findByTourIdOrderByDisplayOrderAsc(tourId).stream()
                .map(TourCultureItem::getCultureItem)
                .filter(ci -> ci.getStatus() == PublicationStatus.PUBLISHED)
                .collect(Collectors.toList());
        if (!linked.isEmpty()) {
            return linked;
        }
        return fallbackByProvince(tourId, null);
    }

    @Override
    public List<CultureItem> findCultureItemsByTourIdAndCategory(Long tourId, CultureCategory category) {
        if (category == null) {
            return findCultureItemsByTourId(tourId);
        }
        List<CultureItem> linked = repository.findByTourIdOrderByDisplayOrderAsc(tourId).stream()
                .map(TourCultureItem::getCultureItem)
                .filter(ci -> ci.getStatus() == PublicationStatus.PUBLISHED && ci.getCategory() == category)
                .collect(Collectors.toList());
        if (!linked.isEmpty()) {
            return linked;
        }
        return fallbackByProvince(tourId, category);
    }

    private List<CultureItem> fallbackByProvince(Long tourId, CultureCategory category) {
        Tour tour = tourService.findById(tourId).orElse(null);
        if (tour == null || tour.getProvince() == null) {
            return List.of();
        }
        Long provinceId = tour.getProvince().getId();
        if (category != null) {
            return cultureItemService.findByProvinceIdAndCategory(provinceId, category).stream()
                    .filter(ci -> ci.getStatus() == PublicationStatus.PUBLISHED)
                    .collect(Collectors.toList());
        }
        return cultureItemService.findAllByProvinceId(provinceId).stream()
                .filter(ci -> ci.getStatus() == PublicationStatus.PUBLISHED)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByTourIdAndCultureItemId(Long tourId, Long cultureItemId) {
        repository.findByTourIdOrderByDisplayOrderAsc(tourId).stream()
                .filter(tci -> tci.getCultureItem().getId().equals(cultureItemId))
                .findFirst()
                .ifPresent(repository::delete);
    }

    @Override
    public void setCultureItemsForTour(Long tourId, List<Long> cultureItemIds) {
        if (cultureItemIds == null || cultureItemIds.isEmpty()) {
            repository.deleteByTourId(tourId);
            return;
        }
        Tour tour = tourService.findById(tourId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Tour không tồn tại"));
        repository.deleteByTourId(tourId);
        List<TourCultureItem> toSave = new ArrayList<>();
        for (int i = 0; i < cultureItemIds.size(); i++) {
            Long ciId = cultureItemIds.get(i);
            CultureItem ci = cultureItemService.findById(ciId)
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "CultureItem không tồn tại: " + ciId));
            toSave.add(TourCultureItem.builder()
                    .tour(tour)
                    .cultureItem(ci)
                    .displayOrder(i)
                    .build());
        }
        repository.saveAll(toSave);
    }
}
