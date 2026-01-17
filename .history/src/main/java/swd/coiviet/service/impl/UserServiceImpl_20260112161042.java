package swd.coiviet.service.impl;

import org.springframework.stereotype.Service;
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
    public User save(User user) { return repo.save(user); }

    @Override
    public Optional<User> findById(Long id) { return repo.findById(id); }

    @Override
    public Optional<User> findByEmail(String email) { return repo.findByEmail(email); }

    @Override
    public List<User> findAll() { return repo.findAll(); }

    @Override
    public void deleteById(Long id) { repo.deleteById(id); }
}
