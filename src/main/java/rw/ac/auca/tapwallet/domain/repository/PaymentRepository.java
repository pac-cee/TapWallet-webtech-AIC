package rw.ac.auca.tapwallet.domain.repository;

import rw.ac.auca.tapwallet.domain.model.payment.Payment;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findById(Long id);

    List<Payment> findAll();

    /** Payments made from the wallet owned by this user — the customer's own history. */
    List<Payment> findByPayerId(Long payerUserId);

    /** Payments received by the shop this user operates — the merchant's own history. */
    List<Payment> findByMerchantOperatorId(Long operatorUserId);

    List<Payment> findByMerchantId(Long merchantId);

    List<Payment> findByWalletId(Long walletId);

    void delete(Payment payment);
}
