package swd.coiviet.service.impl;

import org.springframework.stereotype.Service;
import swd.coiviet.model.Voucher;
import swd.coiviet.repository.VoucherRepository;
import swd.coiviet.service.VoucherService;

import java.util.Optional;

@Service
public class VoucherServiceImpl implements VoucherService {
    private final VoucherRepository repo;

    public VoucherServiceImpl(VoucherRepository repo) { this.repo = repo; }

    @Override
    public Voucher save(Voucher v) { return repo.save(v); }

    @Override
    public Optional<Voucher> findById(Long id) { return repo.findById(id); }

    @Override
    public Optional<Voucher> findByCode(String code) { return repo.findByCode(code); }

    @Override
    public void deleteById(Long id) { repo.deleteById(id); }
}
