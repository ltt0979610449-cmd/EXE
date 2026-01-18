package swd.coiviet.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swd.coiviet.model.User;
import swd.coiviet.repository.UserRepository;
import swd.coiviet.service.UserService;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository repo;

    public UserServiceImpl(UserRepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional
    public User save(User user) { return repo.save(user); }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) { return repo.findById(id); }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) { return repo.findByEmail(email); }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) { return repo.findByUsername(username); }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() { return repo.findAll(); }

    @Override
    @Transactional
    public void deleteById(Long id) { repo.deleteById(id); }
}
