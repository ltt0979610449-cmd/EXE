package swd.coiviet.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import swd.coiviet.dto.response.ArtisanDetailResponse;
import swd.coiviet.dto.response.ArtisanSummaryResponse;
import swd.coiviet.dto.response.CultureItemSummaryResponse;
import swd.coiviet.dto.response.TourSummaryResponse;
import swd.coiviet.enums.PublicationStatus;
import swd.coiviet.enums.Status;
import swd.coiviet.model.Artisan;
import swd.coiviet.model.CultureItem;
import swd.coiviet.model.Tour;
import swd.coiviet.exception.AppException;
import swd.coiviet.exception.ErrorCode;
import swd.coiviet.repository.ArtisanRepository;
import swd.coiviet.repository.CultureItemRepository;
import swd.coiviet.repository.TourRepository;
import swd.coiviet.service.ArtisanService;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ArtisanServiceImpl implements ArtisanService {
    private final ArtisanRepository repo;
    private final TourRepository tourRepository;
    private final CultureItemRepository cultureItemRepository;
    private final ObjectMapper objectMapper;

    public ArtisanServiceImpl(ArtisanRepository repo, TourRepository tourRepository,
                             CultureItemRepository cultureItemRepository, ObjectMapper objectMapper) {
        this.repo = repo;
        this.tourRepository = tourRepository;
        this.cultureItemRepository = cultureItemRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public Artisan save(Artisan a) { return repo.save(a); }

    @Override
    public Optional<Artisan> findById(Long id) { return repo.findById(id); }

    @Override
    public List<Artisan> findAll() { return repo.findAll(); }

    @Override
    public List<Artisan> findByProvinceId(Long provinceId) { return repo.findByProvinceId(provinceId); }

    @Override
    public Optional<Artisan> findByUserId(Long userId) { return repo.findByUserId(userId); }

    @Override
    public void deleteById(Long id) { repo.deleteById(id); }

    @Override
    public ArtisanDetailResponse getDetailById(Long id) {
        Artisan artisan = repo.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Nghệ nhân không tồn tại"));

        // Parse images
        List<String> images;
        if (artisan.getImages() != null && !artisan.getImages().isEmpty()) {
            images = List.of(artisan.getImages().split(","));
            images = images.stream().map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
        } else {
            images = Collections.emptyList();
        }

        // Parse narrativeContent
        List<ArtisanDetailResponse.NarrativeBlock> narrativeContent;
        if (artisan.getNarrativeContent() != null && !artisan.getNarrativeContent().isEmpty()) {
            try {
                narrativeContent = objectMapper.readValue(
                        artisan.getNarrativeContent(),
                        new TypeReference<List<ArtisanDetailResponse.NarrativeBlock>>() {}
                );
            } catch (Exception e) {
                narrativeContent = Collections.emptyList();
            }
        } else {
            narrativeContent = Collections.emptyList();
        }

        // Compute age
        Integer age = null;
        LocalDate dob = artisan.getDateOfBirth();
        if (dob == null && artisan.getUser() != null) {
            dob = artisan.getUser().getDateOfBirth();
        }
        if (dob != null) {
            age = Period.between(dob, LocalDate.now()).getYears();
        }

        // Related tours
        List<TourSummaryResponse> relatedTours = new ArrayList<>();
        List<Tour> tours = tourRepository.findByArtisanId(id);
        for (Tour t : tours) {
            if (t.getStatus() == Status.ACTIVE) {
                relatedTours.add(TourSummaryResponse.builder()
                        .id(t.getId())
                        .title(t.getTitle())
                        .slug(t.getSlug())
                        .thumbnailUrl(t.getThumbnailUrl())
                        .location(t.getProvince() != null ? t.getProvince().getName() : null)
                        .description(t.getDescription())
                        .price(t.getPrice())
                        .build());
            }
        }

        // Related culture items (same province)
        List<CultureItemSummaryResponse> relatedCultureItems = new ArrayList<>();
        if (artisan.getProvince() != null) {
            List<CultureItem> cultureItems = cultureItemRepository.findByProvinceIdAndStatus(
                    artisan.getProvince().getId(), PublicationStatus.PUBLISHED);
            for (CultureItem c : cultureItems) {
                relatedCultureItems.add(CultureItemSummaryResponse.builder()
                        .id(c.getId())
                        .title(c.getTitle())
                        .thumbnailUrl(c.getThumbnailUrl())
                        .description(c.getDescription())
                        .build());
            }
        }

        // Other artisans (same province, exclude current)
        List<ArtisanSummaryResponse> otherArtisans = new ArrayList<>();
        if (artisan.getProvince() != null) {
            List<Artisan> provinceArtisans = repo.findByProvinceId(artisan.getProvince().getId());
            for (Artisan a : provinceArtisans) {
                if (!a.getId().equals(id) && Boolean.TRUE.equals(a.getIsActive())) {
                    otherArtisans.add(ArtisanSummaryResponse.builder()
                            .id(a.getId())
                            .fullName(a.getFullName())
                            .profileImageUrl(a.getProfileImageUrl())
                            .build());
                }
            }
        }

        return ArtisanDetailResponse.builder()
                .id(artisan.getId())
                .fullName(artisan.getFullName())
                .specialization(artisan.getSpecialization())
                .bio(artisan.getBio())
                .profileImageUrl(artisan.getProfileImageUrl())
                .heroSubtitle(artisan.getHeroSubtitle())
                .ethnicity(artisan.getEthnicity())
                .age(age)
                .location(artisan.getProvince() != null ? artisan.getProvince().getName() : null)
                .images(images)
                .panoramaImageUrl(artisan.getPanoramaImageUrl())
                .narrativeContent(narrativeContent)
                .relatedTours(relatedTours)
                .relatedCultureItems(relatedCultureItems)
                .otherArtisans(otherArtisans)
                .build();
    }
}
