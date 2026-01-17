package swd.coiviet.service;

import swd.coiviet.model.UserMemory;
import swd.coiviet.enums.PublicationStatus;

import java.util.List;
import java.util.Optional;

public interface UserMemoryService {
    UserMemory save(UserMemory memory);
    Optional<UserMemory> findById(Long id);
    List<UserMemory> findAll();
    List<UserMemory> findByUserId(Long userId);
    List<UserMemory> findByProvinceId(Long provinceId);
    List<UserMemory> findByStatus(PublicationStatus status);
    List<UserMemory> findByProvinceIdAndStatus(Long provinceId, PublicationStatus status);
    List<UserMemory> findByUserIdAndStatus(Long userId, PublicationStatus status);
    void deleteById(Long id);
}
