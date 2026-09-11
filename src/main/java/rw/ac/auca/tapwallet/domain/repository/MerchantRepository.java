package rw.ac.auca.tapwallet.domain.repository;

import rw.ac.auca.tapwallet.domain.model.merchant.Merchant;

import java.util.List;
import java.util.Optional;

public interface MerchantRepository {

    Merchant save(Merchant merchant);

    Optional<Merchant> findById(Long id);

    Optional<Merchant> findByCode(String code);

    Optional<Merchant> findByOperatorId(Long operatorId);

    List<Merchant> findAll();

    List<Merchant> findActive();

    void delete(Merchant merchant);
}
