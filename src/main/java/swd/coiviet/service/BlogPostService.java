package swd.coiviet.service;

import swd.coiviet.model.BlogPost;

import java.util.List;
import java.util.Optional;

public interface BlogPostService {
    BlogPost save(BlogPost p);
    Optional<BlogPost> findById(Long id);
    Optional<BlogPost> findBySlug(String slug);
    List<BlogPost> findAll();
    void deleteById(Long id);
}
