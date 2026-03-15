package swd.coiviet.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import swd.coiviet.dto.response.ArtisanDetailResponse;
import swd.coiviet.dto.response.BlogPostDetailResponse;
import swd.coiviet.exception.AppException;
import swd.coiviet.exception.ErrorCode;
import swd.coiviet.model.BlogPost;
import swd.coiviet.repository.BlogPostRepository;
import swd.coiviet.service.BlogPostService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BlogPostServiceImpl implements BlogPostService {
    private final BlogPostRepository repo;
    private final ObjectMapper objectMapper;

    public BlogPostServiceImpl(BlogPostRepository repo, ObjectMapper objectMapper) {
        this.repo = repo;
        this.objectMapper = objectMapper;
    }

    @Override
    public BlogPost save(BlogPost p) { return repo.save(p); }

    @Override
    public Optional<BlogPost> findById(Long id) { return repo.findById(id); }

    @Override
    public Optional<BlogPost> findBySlug(String slug) { return repo.findBySlug(slug); }

    @Override
    public List<BlogPost> findAll() { return repo.findAll(); }

    @Override
    public void deleteById(Long id) { repo.deleteById(id); }

    @Override
    public BlogPostDetailResponse getDetailById(Long id) {
        BlogPost post = repo.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Blog post không tồn tại"));
        return toDetailResponse(post);
    }

    @Override
    public Optional<BlogPostDetailResponse> getDetailBySlug(String slug) {
        return repo.findBySlug(slug).map(this::toDetailResponse);
    }

    private BlogPostDetailResponse toDetailResponse(BlogPost post) {
        List<String> images = parseImages(post.getImages());
        List<ArtisanDetailResponse.NarrativeBlock> narrativeContent = parseNarrativeContent(post.getNarrativeContent());

        return BlogPostDetailResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .slug(post.getSlug())
                .heroSubtitle(post.getHeroSubtitle())
                .featuredImageUrl(post.getFeaturedImageUrl())
                .panoramaImageUrl(post.getPanoramaImageUrl())
                .content(post.getContent())
                .narrativeContent(narrativeContent)
                .images(images)
                .location(post.getProvince() != null ? post.getProvince().getName() : null)
                .status(post.getStatus())
                .publishedAt(post.getPublishedAt())
                .createdAt(post.getCreatedAt())
                .build();
    }

    private List<String> parseImages(String imagesStr) {
        if (imagesStr == null || imagesStr.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(imagesStr.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private List<ArtisanDetailResponse.NarrativeBlock> parseNarrativeContent(String json) {
        if (json == null || json.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<ArtisanDetailResponse.NarrativeBlock>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
