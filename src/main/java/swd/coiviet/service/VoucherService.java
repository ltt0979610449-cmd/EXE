package swd.coiviet.service;

import swd.coiviet.model.Voucher;

import java.util.Optional;

public interface VoucherService {
    Voucher save(Voucher v);
    Optional<Voucher> findById(Long id);
    Optional<Voucher> findByCode(String code);
    java.util.List<Voucher> findAll();
    void deleteById(Long id);
}
