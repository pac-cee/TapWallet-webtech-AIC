package rw.ac.auca.tapwallet.infrastructure.persistence;

import rw.ac.auca.tapwallet.domain.model.wallet.Wallet;
import rw.ac.auca.tapwallet.domain.repository.WalletRepository;

import java.util.List;
import java.util.Optional;

public class HibernateWalletRepository implements WalletRepository {

    @Override
    public Wallet save(Wallet wallet) {
        UnitOfWork.currentSession().saveOrUpdate(wallet);
        return wallet;
    }

    @Override
    public Optional<Wallet> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(UnitOfWork.currentSession().get(Wallet.class, id));
    }

    @Override
    public Optional<Wallet> findByOwnerId(Long ownerId) {
        if (ownerId == null) {
            return Optional.empty();
        }
        List<Wallet> matches = UnitOfWork.currentSession()
                .createQuery("SELECT w FROM Wallet w WHERE w.owner.id = :ownerId", Wallet.class)
                .setParameter("ownerId", ownerId)
                .setMaxResults(1)
                .list();
        return matches.isEmpty() ? Optional.empty() : Optional.of(matches.get(0));
    }

    @Override
    public List<Wallet> findAll() {
        return UnitOfWork.currentSession()
                .createQuery("SELECT w FROM Wallet w ORDER BY w.id", Wallet.class)
                .list();
    }

    @Override
    public void delete(Wallet wallet) {
        UnitOfWork.currentSession().delete(wallet);
    }
}
