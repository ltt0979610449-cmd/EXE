package swd.coiviet.service.impl;

import org.springframework.stereotype.Service;
import swd.coiviet.model.BlogPost;
import swd.coiviet.repository.BlogPostRepository;
import swd.coiviet.service.BlogPostService;

import java.util.List;
import java.util.Optional;

@Service
public class BlogPostServiceImpl implements BlogPostService {
    private final BlogPostRepository repo;

    public BlogPostServiceImpl(BlogPostRepository repo) { this.repo = repo; }

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
}
