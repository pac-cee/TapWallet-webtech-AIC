package rw.ac.auca.tapwallet.infrastructure.persistence;

import rw.ac.auca.tapwallet.domain.model.payment.Payment;
import rw.ac.auca.tapwallet.domain.repository.PaymentRepository;

import java.util.List;
import java.util.Optional;

public class HibernatePaymentRepository implements PaymentRepository {

    @Override
    public Payment save(Payment payment) {
        UnitOfWork.currentSession().saveOrUpdate(payment);
        return payment;
    }

    @Override
    public Optional<Payment> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(UnitOfWork.currentSession().get(Payment.class, id));
    }

    @Override
    public List<Payment> findAll() {
        return UnitOfWork.currentSession()
                .createQuery("SELECT p FROM Payment p ORDER BY p.id DESC", Payment.class)
                .list();
    }

    @Override
    public List<Payment> findByPayerId(Long payerUserId) {
        if (payerUserId == null) {
            return java.util.Collections.emptyList();
        }
        return UnitOfWork.currentSession()
                .createQuery("SELECT p FROM Payment p WHERE p.wallet.owner.id = :payerId ORDER BY p.id DESC", Payment.class)
                .setParameter("payerId", payerUserId)
                .list();
    }

    @Override
    public List<Payment> findByMerchantOperatorId(Long operatorUserId) {
        if (operatorUserId == null) {
            return java.util.Collections.emptyList();
        }
        return UnitOfWork.currentSession()
                .createQuery("SELECT p FROM Payment p WHERE p.merchant.operator.id = :operatorId ORDER BY p.id DESC", Payment.class)
                .setParameter("operatorId", operatorUserId)
                .list();
    }

    @Override
    public List<Payment> findByMerchantId(Long merchantId) {
        if (merchantId == null) {
            return java.util.Collections.emptyList();
        }
        return UnitOfWork.currentSession()
                .createQuery("SELECT p FROM Payment p WHERE p.merchant.id = :merchantId ORDER BY p.id DESC", Payment.class)
                .setParameter("merchantId", merchantId)
                .list();
    }

    @Override
    public List<Payment> findByWalletId(Long walletId) {
        if (walletId == null) {
            return java.util.Collections.emptyList();
        }
        return UnitOfWork.currentSession()
                .createQuery("SELECT p FROM Payment p WHERE p.wallet.id = :walletId ORDER BY p.id DESC", Payment.class)
                .setParameter("walletId", walletId)
                .list();
    }

    @Override
    public void delete(Payment payment) {
        UnitOfWork.currentSession().delete(payment);
    }
}
