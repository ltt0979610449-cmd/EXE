package swd.coiviet.service.ai;

import org.springframework.stereotype.Service;
import swd.coiviet.enums.CultureCategory;
import swd.coiviet.enums.LearnModuleStatus;
import swd.coiviet.enums.PublicationStatus;
import swd.coiviet.enums.Status;
import swd.coiviet.model.Artisan;
import swd.coiviet.model.BlogPost;
import swd.coiviet.model.CultureItem;
import swd.coiviet.model.LearnModule;
import swd.coiviet.model.Province;
import swd.coiviet.model.Tour;
import swd.coiviet.model.Video;
import swd.coiviet.repository.ArtisanRepository;
import swd.coiviet.repository.BlogPostRepository;
import swd.coiviet.repository.CultureItemRepository;
import swd.coiviet.repository.LearnModuleRepository;
import swd.coiviet.repository.ProvinceRepository;
import swd.coiviet.repository.TourRepository;
import swd.coiviet.repository.VideoRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * RAG (Retrieval Augmented Generation) service.
 * Extracts context from Tour, CultureItem, Artisan, BlogPost, Video, LearnModule based on user message.
 */
@Service
public class AiRagService {

    private static final int MAX_ITEMS_PER_CATEGORY = 15;
    /** Keyword (lowercase) -> province name for DB search */
    private static final Map<String, String> PROVINCE_KEYWORDS = new LinkedHashMap<>();
    /** Keyword (lowercase) -> CultureCategory */
    private static final Map<String, CultureCategory> CULTURE_CATEGORY_KEYWORDS = new LinkedHashMap<>();

    static {
        PROVINCE_KEYWORDS.put("gia lai", "Gia Lai");
        PROVINCE_KEYWORDS.put("pleiku", "Gia Lai");
        PROVINCE_KEYWORDS.put("kon tum", "Kon Tum");
        PROVINCE_KEYWORDS.put("kontum", "Kon Tum");
        PROVINCE_KEYWORDS.put("đắk lắk", "Đắk Lắk");
        PROVINCE_KEYWORDS.put("dak lak", "Đắk Lắk");
        PROVINCE_KEYWORDS.put("buôn ma thuột", "Đắk Lắk");
        PROVINCE_KEYWORDS.put("buon ma thuot", "Đắk Lắk");
        PROVINCE_KEYWORDS.put("đắk nông", "Đắk Nông");
        PROVINCE_KEYWORDS.put("dak nong", "Đắk Nông");
        PROVINCE_KEYWORDS.put("lâm đồng", "Lâm Đồng");
        PROVINCE_KEYWORDS.put("lam dong", "Lâm Đồng");
        PROVINCE_KEYWORDS.put("đà lạt", "Lâm Đồng");
        PROVINCE_KEYWORDS.put("da lat", "Lâm Đồng");
        PROVINCE_KEYWORDS.put("bình phước", "Bình Phước");
        PROVINCE_KEYWORDS.put("binh phuoc", "Bình Phước");

        CULTURE_CATEGORY_KEYWORDS.put("lễ hội", CultureCategory.FESTIVAL);
        CULTURE_CATEGORY_KEYWORDS.put("le hoi", CultureCategory.FESTIVAL);
        CULTURE_CATEGORY_KEYWORDS.put("ẩm thực", CultureCategory.FOOD);
        CULTURE_CATEGORY_KEYWORDS.put("am thuc", CultureCategory.FOOD);
        CULTURE_CATEGORY_KEYWORDS.put("món ăn", CultureCategory.FOOD);
        CULTURE_CATEGORY_KEYWORDS.put("food", CultureCategory.FOOD);
        CULTURE_CATEGORY_KEYWORDS.put("trang phục", CultureCategory.COSTUME);
        CULTURE_CATEGORY_KEYWORDS.put("costume", CultureCategory.COSTUME);
        CULTURE_CATEGORY_KEYWORDS.put("nhạc cụ", CultureCategory.INSTRUMENT);
        CULTURE_CATEGORY_KEYWORDS.put("nhac cu", CultureCategory.INSTRUMENT);
        CULTURE_CATEGORY_KEYWORDS.put("instrument", CultureCategory.INSTRUMENT);
        CULTURE_CATEGORY_KEYWORDS.put("múa", CultureCategory.DANCE);
        CULTURE_CATEGORY_KEYWORDS.put("mua", CultureCategory.DANCE);
        CULTURE_CATEGORY_KEYWORDS.put("dance", CultureCategory.DANCE);
        CULTURE_CATEGORY_KEYWORDS.put("truyền thuyết", CultureCategory.LEGEND);
        CULTURE_CATEGORY_KEYWORDS.put("truyen thuyet", CultureCategory.LEGEND);
        CULTURE_CATEGORY_KEYWORDS.put("legend", CultureCategory.LEGEND);
        CULTURE_CATEGORY_KEYWORDS.put("thủ công", CultureCategory.CRAFT);
        CULTURE_CATEGORY_KEYWORDS.put("thu cong", CultureCategory.CRAFT);
        CULTURE_CATEGORY_KEYWORDS.put("craft", CultureCategory.CRAFT);
        CULTURE_CATEGORY_KEYWORDS.put("gốm", CultureCategory.CRAFT);
        CULTURE_CATEGORY_KEYWORDS.put("gom", CultureCategory.CRAFT);
        CULTURE_CATEGORY_KEYWORDS.put("dệt", CultureCategory.CRAFT);
        CULTURE_CATEGORY_KEYWORDS.put("det", CultureCategory.CRAFT);
        CULTURE_CATEGORY_KEYWORDS.put("đan", CultureCategory.CRAFT);
        CULTURE_CATEGORY_KEYWORDS.put("dan", CultureCategory.CRAFT);
    }

    private final ProvinceRepository provinceRepository;
    private final TourRepository tourRepository;
    private final CultureItemRepository cultureItemRepository;
    private final ArtisanRepository artisanRepository;
    private final BlogPostRepository blogPostRepository;
    private final VideoRepository videoRepository;
    private final LearnModuleRepository learnModuleRepository;

    public AiRagService(ProvinceRepository provinceRepository,
                        TourRepository tourRepository,
                        CultureItemRepository cultureItemRepository,
                        ArtisanRepository artisanRepository,
                        BlogPostRepository blogPostRepository,
                        VideoRepository videoRepository,
                        LearnModuleRepository learnModuleRepository) {
        this.provinceRepository = provinceRepository;
        this.tourRepository = tourRepository;
        this.cultureItemRepository = cultureItemRepository;
        this.artisanRepository = artisanRepository;
        this.blogPostRepository = blogPostRepository;
        this.videoRepository = videoRepository;
        this.learnModuleRepository = learnModuleRepository;
    }

    public String getContext(String userMessage) {
        String normalized = userMessage.toLowerCase().trim();
        List<Province> matchedProvinces = findProvincesFromMessage(normalized);
        List<String> contextParts = new ArrayList<>();

        if (!matchedProvinces.isEmpty()) {
            for (Province province : matchedProvinces) {
                contextParts.add(buildProvinceContext(province));
            }
        } else {
            contextParts.add(buildGeneralContext(normalized));
        }

        if (contextParts.isEmpty()) {
            return "Không tìm thấy dữ liệu phù hợp. Hãy hỏi về tour, văn hóa, nghệ nhân, bài viết hoặc video.";
        }

        return String.join("\n\n", contextParts);
    }

    private List<Province> findProvincesFromMessage(String message) {
        if (message.contains("tây nguyên") || message.contains("tay nguyen") || message.contains("cao nguyên") || message.contains("cao nguyen")) {
            return provinceRepository.findAll().stream()
                    .filter(p -> Boolean.TRUE.equals(p.getIsActive()))
                    .limit(5)
                    .collect(Collectors.toList());
        }
        List<Province> result = new ArrayList<>();
        for (Map.Entry<String, String> e : PROVINCE_KEYWORDS.entrySet()) {
            if (message.contains(e.getKey())) {
                List<Province> found = provinceRepository.findByNameContainingIgnoreCase(e.getValue());
                for (Province p : found) {
                    if (result.stream().noneMatch(r -> r.getId().equals(p.getId()))) {
                        result.add(p);
                    }
                }
                if (result.size() >= 3) break;
            }
        }
        return result.stream().limit(3).collect(Collectors.toList());
    }

    private String buildProvinceContext(Province province) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Tỉnh: ").append(province.getName()).append(" ===\n");

        if (province.getDescription() != null && !province.getDescription().isBlank()) {
            sb.append("Mô tả: ").append(truncate(province.getDescription(), 300)).append("\n");
        }
        if (province.getBestSeason() != null && !province.getBestSeason().isBlank()) {
            sb.append("Thời điểm đẹp: ").append(truncate(province.getBestSeason(), 150)).append("\n");
        }
        if (province.getTransportation() != null && !province.getTransportation().isBlank()) {
            sb.append("Di chuyển: ").append(truncate(province.getTransportation(), 150)).append("\n");
        }
        if (province.getCulturalTips() != null && !province.getCulturalTips().isBlank()) {
            sb.append("Lưu ý văn hóa: ").append(truncate(province.getCulturalTips(), 200)).append("\n");
        }

        List<Tour> tours = tourRepository.findByProvinceId(province.getId()).stream()
                .filter(t -> t.getStatus() == Status.ACTIVE)
                .limit(MAX_ITEMS_PER_CATEGORY)
                .toList();
        if (!tours.isEmpty()) {
            sb.append("TOURS:\n");
            for (Tour t : tours) {
                sb.append("- ").append(t.getTitle())
                        .append(" | Giá: ").append(t.getPrice() != null ? t.getPrice() + " VNĐ" : "Liên hệ")
                        .append(" | Thời lượng: ").append(t.getDurationHours() != null ? t.getDurationHours() + "h" : "N/A");
                if (t.getArtisan() != null) {
                    sb.append(" | Nghệ nhân: ").append(t.getArtisan().getFullName());
                }
                sb.append("\n");
                if (t.getDescription() != null && !t.getDescription().isBlank()) {
                    sb.append("  Mô tả: ").append(truncate(t.getDescription(), 250)).append("\n");
                }
                if (t.getBestSeason() != null && !t.getBestSeason().isBlank()) {
                    sb.append("  Mùa đẹp: ").append(truncate(t.getBestSeason(), 80)).append("\n");
                }
            }
        }

        List<CultureItem> cultureItems = cultureItemRepository.findByProvinceIdAndStatus(
                province.getId(), PublicationStatus.PUBLISHED).stream()
                .limit(MAX_ITEMS_PER_CATEGORY)
                .toList();
        if (!cultureItems.isEmpty()) {
            sb.append("VĂN HÓA:\n");
            for (CultureItem c : cultureItems) {
                sb.append("- ").append(c.getTitle())
                        .append(" (").append(c.getCategory() != null ? c.getCategory() : "N/A").append(")\n");
                if (c.getDescription() != null && !c.getDescription().isBlank()) {
                    sb.append("  ").append(truncate(c.getDescription(), 180)).append("\n");
                }
            }
        }

        List<Artisan> artisans = artisanRepository.findByProvinceId(province.getId()).stream()
                .filter(a -> Boolean.TRUE.equals(a.getIsActive()))
                .limit(MAX_ITEMS_PER_CATEGORY)
                .toList();
        if (!artisans.isEmpty()) {
            sb.append("NGHỆ NHÂN:\n");
            for (Artisan a : artisans) {
                sb.append("- ").append(a.getFullName())
                        .append(" | Chuyên môn: ").append(a.getSpecialization());
                if (a.getEthnicity() != null && !a.getEthnicity().isBlank()) {
                    sb.append(" | Dân tộc: ").append(a.getEthnicity());
                }
                if (a.getBio() != null && !a.getBio().isBlank()) {
                    sb.append(" | ").append(truncate(a.getBio(), 120));
                }
                sb.append("\n");
            }
        }

        List<BlogPost> blogPosts = blogPostRepository.findByProvinceIdAndStatus(province.getId(), PublicationStatus.PUBLISHED).stream()
                .limit(5)
                .toList();
        if (!blogPosts.isEmpty()) {
            sb.append("BÀI VIẾT:\n");
            for (BlogPost b : blogPosts) {
                sb.append("- ").append(b.getTitle());
                if (b.getContent() != null && !b.getContent().isBlank()) {
                    sb.append(" | ").append(truncate(stripHtml(b.getContent()), 100));
                }
                sb.append("\n");
            }
        }

        List<Video> videos = videoRepository.findByProvinceIdAndStatus(province.getId(), PublicationStatus.PUBLISHED).stream()
                .limit(5)
                .toList();
        if (!videos.isEmpty()) {
            sb.append("VIDEO:\n");
            for (Video v : videos) {
                sb.append("- ").append(v.getTitle());
                if (v.getCultureItem() != null) {
                    sb.append(" | Văn hóa: ").append(v.getCultureItem().getTitle());
                }
                sb.append("\n");
            }
        }

        List<LearnModule> modules = learnModuleRepository.findByProvinceIdAndStatusOrderByOrderIndexAsc(province.getId(), LearnModuleStatus.PUBLISHED).stream()
                .limit(5)
                .toList();
        if (!modules.isEmpty()) {
            sb.append("HỌC TẬP:\n");
            for (LearnModule m : modules) {
                sb.append("- ").append(m.getTitle());
                if (m.getQuickNotesJson() != null && !m.getQuickNotesJson().isBlank()) {
                    sb.append(" | ").append(truncate(m.getQuickNotesJson(), 80));
                }
                if (m.getCulturalEtiquetteTitle() != null && !m.getCulturalEtiquetteTitle().isBlank()) {
                    sb.append(" | Ứng xử: ").append(truncate(m.getCulturalEtiquetteTitle(), 60));
                }
                sb.append("\n");
            }
        }

        return sb.toString();
    }

    private String buildGeneralContext(String message) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== DỮ LIỆU TỔNG QUAN ===\n");

        CultureCategory matchedCategory = null;
        for (Map.Entry<String, CultureCategory> e : CULTURE_CATEGORY_KEYWORDS.entrySet()) {
            if (message.contains(e.getKey())) {
                matchedCategory = e.getValue();
                break;
            }
        }

        if (matchedCategory != null) {
            List<CultureItem> byCategory = cultureItemRepository.findByCategory(matchedCategory).stream()
                    .filter(c -> c.getStatus() == PublicationStatus.PUBLISHED)
                    .limit(MAX_ITEMS_PER_CATEGORY)
                    .toList();
            if (!byCategory.isEmpty()) {
                sb.append("VĂN HÓA (").append(matchedCategory).append("):\n");
                for (CultureItem c : byCategory) {
                    sb.append("- ").append(c.getTitle())
                            .append(" | ").append(c.getProvince() != null ? c.getProvince().getName() : "N/A");
                    if (c.getDescription() != null && !c.getDescription().isBlank()) {
                        sb.append(" | ").append(truncate(c.getDescription(), 100));
                    }
                    sb.append("\n");
                }
            }
        }

        boolean searchArtisan = message.contains("nghệ nhân") || message.contains("nghe nhan") || message.contains("artisan");
        String artisanKeyword = extractArtisanKeyword(message);
        if (searchArtisan || artisanKeyword != null) {
            List<Artisan> artisans;
            if (artisanKeyword != null) {
                artisans = artisanRepository.findBySpecializationContainingIgnoreCase(artisanKeyword).stream()
                        .filter(a -> Boolean.TRUE.equals(a.getIsActive()))
                        .limit(MAX_ITEMS_PER_CATEGORY)
                        .toList();
            } else {
                artisans = artisanRepository.findAll().stream()
                        .filter(a -> Boolean.TRUE.equals(a.getIsActive()))
                        .limit(MAX_ITEMS_PER_CATEGORY)
                        .toList();
            }
            if (!artisans.isEmpty()) {
                sb.append("NGHỆ NHÂN:\n");
                for (Artisan a : artisans) {
                    sb.append("- ").append(a.getFullName())
                            .append(" | ").append(a.getSpecialization())
                            .append(" | Tỉnh: ").append(a.getProvince() != null ? a.getProvince().getName() : "N/A")
                            .append("\n");
                }
            }
        }

        String tourKeyword = extractSearchKeyword(message, "tour", "du lịch", "du lich");
        List<Tour> tours;
        if (tourKeyword != null && !tourKeyword.isBlank()) {
            tours = tourRepository.findByTitleContainingOrDescriptionContainingIgnoreCase(tourKeyword).stream()
                    .filter(t -> t.getStatus() == Status.ACTIVE)
                    .limit(MAX_ITEMS_PER_CATEGORY)
                    .toList();
        } else {
            tours = tourRepository.findAll().stream()
                    .filter(t -> t.getStatus() == Status.ACTIVE)
                    .limit(MAX_ITEMS_PER_CATEGORY)
                    .toList();
        }
        if (!tours.isEmpty()) {
            sb.append("TOURS:\n");
            for (Tour t : tours) {
                sb.append("- ").append(t.getTitle())
                        .append(" | ").append(t.getProvince() != null ? t.getProvince().getName() : "N/A")
                        .append(" | Giá: ").append(t.getPrice() != null ? t.getPrice() + " VNĐ" : "Liên hệ")
                        .append("\n");
            }
        }

        String cultureKeyword = extractSearchKeyword(message, "văn hóa", "van hoa", "văn hoá");
        List<CultureItem> cultureItems;
        if (cultureKeyword != null && !cultureKeyword.isBlank() && matchedCategory == null) {
            cultureItems = cultureItemRepository.findByTitleContainingIgnoreCase(cultureKeyword).stream()
                    .filter(c -> c.getStatus() == PublicationStatus.PUBLISHED)
                    .limit(MAX_ITEMS_PER_CATEGORY)
                    .toList();
        } else if (matchedCategory == null) {
            cultureItems = cultureItemRepository.findByStatus(PublicationStatus.PUBLISHED).stream()
                    .limit(MAX_ITEMS_PER_CATEGORY)
                    .toList();
        } else {
            cultureItems = List.of();
        }
        if (!cultureItems.isEmpty()) {
            sb.append("VĂN HÓA:\n");
            for (CultureItem c : cultureItems) {
                sb.append("- ").append(c.getTitle())
                        .append(" | ").append(c.getProvince() != null ? c.getProvince().getName() : "N/A")
                        .append(" | ").append(c.getCategory() != null ? c.getCategory() : "")
                        .append("\n");
            }
        }

        List<BlogPost> blogPosts = blogPostRepository.findAll().stream()
                .filter(b -> b.getStatus() == PublicationStatus.PUBLISHED)
                .limit(5)
                .toList();
        if (!blogPosts.isEmpty()) {
            sb.append("BÀI VIẾT:\n");
            for (BlogPost b : blogPosts) {
                sb.append("- ").append(b.getTitle())
                        .append(" | ").append(b.getProvince() != null ? b.getProvince().getName() : "N/A")
                        .append("\n");
            }
        }

        List<LearnModule> modules = learnModuleRepository.findByStatusOrderByOrderIndexAsc(LearnModuleStatus.PUBLISHED).stream()
                .limit(5)
                .toList();
        if (!modules.isEmpty()) {
            sb.append("HỌC TẬP:\n");
            for (LearnModule m : modules) {
                sb.append("- ").append(m.getTitle())
                        .append(" | ").append(m.getProvince() != null ? m.getProvince().getName() : "N/A")
                        .append("\n");
            }
        }

        return sb.toString();
    }

    private String extractArtisanKeyword(String message) {
        for (String kw : List.of("gốm", "gom", "dệt", "det", "đan", "dan", "làm gỗ", "lam go", "gỗ", "go", "thêu", "theu")) {
            if (message.contains(kw)) return kw;
        }
        return null;
    }

    private String extractSearchKeyword(String message, String... triggers) {
        for (String t : triggers) {
            if (message.contains(t)) {
                String rest = message.replace(t, "").trim();
                if (rest.length() > 2 && rest.length() < 50) return rest;
                return null;
            }
        }
        return null;
    }

    private String stripHtml(String html) {
        if (html == null) return "";
        return html.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
    }

    private String truncate(String s, int maxLen) {
        if (s == null) return "";
        s = s.replaceAll("\\s+", " ").trim();
        return s.length() <= maxLen ? s : s.substring(0, maxLen) + "...";
    }
}
