package rw.ac.auca.tapwallet.domain.repository;

import rw.ac.auca.tapwallet.domain.model.wallet.Wallet;

import java.util.List;
import java.util.Optional;

public interface WalletRepository {

    Wallet save(Wallet wallet);

    Optional<Wallet> findById(Long id);

    Optional<Wallet> findByOwnerId(Long ownerId);

    List<Wallet> findAll();

    void delete(Wallet wallet);
}
