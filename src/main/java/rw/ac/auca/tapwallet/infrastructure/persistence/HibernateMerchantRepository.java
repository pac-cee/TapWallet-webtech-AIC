package rw.ac.auca.tapwallet.infrastructure.persistence;

import rw.ac.auca.tapwallet.domain.model.merchant.Merchant;
import rw.ac.auca.tapwallet.domain.model.shared.AccountStatus;
import rw.ac.auca.tapwallet.domain.repository.MerchantRepository;

import java.util.List;
import java.util.Optional;

public class HibernateMerchantRepository implements MerchantRepository {

    @Override
    public Merchant save(Merchant merchant) {
        UnitOfWork.currentSession().saveOrUpdate(merchant);
        return merchant;
    }

    @Override
    public Optional<Merchant> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(UnitOfWork.currentSession().get(Merchant.class, id));
    }

    @Override
    public Optional<Merchant> findByCode(String code) {
        if (code == null) {
            return Optional.empty();
        }
        List<Merchant> matches = UnitOfWork.currentSession()
                .createQuery("SELECT m FROM Merchant m WHERE m.code.value = :code", Merchant.class)
                .setParameter("code", code.trim().toUpperCase())
                .setMaxResults(1)
                .list();
        return matches.isEmpty() ? Optional.empty() : Optional.of(matches.get(0));
    }

    @Override
    public Optional<Merchant> findByOperatorId(Long operatorId) {
        if (operatorId == null) {
            return Optional.empty();
        }
        List<Merchant> matches = UnitOfWork.currentSession()
                .createQuery("SELECT m FROM Merchant m WHERE m.operator.id = :operatorId", Merchant.class)
                .setParameter("operatorId", operatorId)
                .setMaxResults(1)
                .list();
        return matches.isEmpty() ? Optional.empty() : Optional.of(matches.get(0));
    }

    @Override
    public List<Merchant> findAll() {
        return UnitOfWork.currentSession()
                .createQuery("SELECT m FROM Merchant m ORDER BY m.businessName", Merchant.class)
                .list();
    }

    @Override
    public List<Merchant> findActive() {
        return UnitOfWork.currentSession()
                .createQuery("SELECT m FROM Merchant m WHERE m.status = :status ORDER BY m.businessName", Merchant.class)
                .setParameter("status", AccountStatus.ACTIVE)
                .list();
    }

    @Override
    public void delete(Merchant merchant) {
        UnitOfWork.currentSession().delete(merchant);
    }
}
