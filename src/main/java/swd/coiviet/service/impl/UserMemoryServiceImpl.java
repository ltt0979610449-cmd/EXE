package swd.coiviet.service.impl;

import org.springframework.stereotype.Service;
import swd.coiviet.model.UserMemory;
import swd.coiviet.repository.UserMemoryRepository;
import swd.coiviet.service.UserMemoryService;
import swd.coiviet.enums.PublicationStatus;

import java.util.List;
import java.util.Optional;

@Service
public class UserMemoryServiceImpl implements UserMemoryService {
    private final UserMemoryRepository repo;

    public UserMemoryServiceImpl(UserMemoryRepository repo) {
        this.repo = repo;
    }

    @Override
    public UserMemory save(UserMemory memory) {
        return repo.save(memory);
    }

    @Override
    public Optional<UserMemory> findById(Long id) {
        return repo.findById(id);
    }

    @Override
    public List<UserMemory> findAll() {
        return repo.findAll();
    }

    @Override
    public List<UserMemory> findByUserId(Long userId) {
        return repo.findByUserId(userId);
    }

    @Override
    public List<UserMemory> findByProvinceId(Long provinceId) {
        return repo.findByProvinceId(provinceId);
    }

    @Override
    public List<UserMemory> findByStatus(PublicationStatus status) {
        return repo.findByStatus(status);
    }

    @Override
    public List<UserMemory> findByProvinceIdAndStatus(Long provinceId, PublicationStatus status) {
        return repo.findByProvinceIdAndStatus(provinceId, status);
    }

    @Override
    public List<UserMemory> findByUserIdAndStatus(Long userId, PublicationStatus status) {
        return repo.findByUserIdAndStatus(userId, status);
    }

    @Override
    public void deleteById(Long id) {
        repo.deleteById(id);
    }
}
