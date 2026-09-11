package rw.ac.auca.tapwallet.application;

import rw.ac.auca.tapwallet.domain.exception.DomainException;
import rw.ac.auca.tapwallet.domain.model.merchant.Merchant;
import rw.ac.auca.tapwallet.domain.model.payment.Payment;
import rw.ac.auca.tapwallet.domain.model.shared.Money;
import rw.ac.auca.tapwallet.domain.model.wallet.Wallet;
import rw.ac.auca.tapwallet.domain.repository.MerchantRepository;
import rw.ac.auca.tapwallet.domain.repository.PaymentRepository;
import rw.ac.auca.tapwallet.domain.repository.WalletRepository;
import rw.ac.auca.tapwallet.domain.service.PaymentDomainService;
import rw.ac.auca.tapwallet.infrastructure.persistence.UnitOfWork;

import java.math.BigDecimal;
import java.util.List;

/**
 * Payment use cases. The whole movement — debit the wallet, credit the merchant,
 * write the payment row — happens inside one Unit of Work, so the two balances
 * can never drift apart.
 */
public class PaymentApplicationService {

    private final PaymentRepository payments;
    private final WalletRepository wallets;
    private final MerchantRepository merchants;
    private final PaymentDomainService paymentDomainService;

    public PaymentApplicationService(PaymentRepository payments, WalletRepository wallets,
                                     MerchantRepository merchants, PaymentDomainService paymentDomainService) {
        this.payments = payments;
        this.wallets = wallets;
        this.merchants = merchants;
        this.paymentDomainService = paymentDomainService;
    }

    /** The payer is always the signed-in user — it is never taken from the request. */
    public Payment payMerchant(Long payerUserId, Long merchantId, BigDecimal amount) {
        if (merchantId == null) {
            throw new DomainException("Choose a merchant to pay.");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new DomainException("Enter an amount greater than zero.");
        }

        return UnitOfWork.execute(() -> {
            Wallet wallet = wallets.findByOwnerId(payerUserId)
                    .orElseThrow(() -> new DomainException("You do not have a wallet yet."));
            Merchant merchant = merchants.findById(merchantId)
                    .orElseThrow(() -> new DomainException("That merchant no longer exists."));

            Money money = Money.of(amount, wallet.getBalance().getCurrency());
            Payment payment = paymentDomainService.pay(wallet, merchant, money);

            wallets.save(wallet);
            merchants.save(merchant);
            return payments.save(payment);
        });
    }

    public void reverse(Long paymentId) {
        UnitOfWork.run(() -> {
            Payment payment = payments.findById(paymentId)
                    .orElseThrow(() -> new DomainException("That payment no longer exists."));
            paymentDomainService.reverse(payment);
            wallets.save(payment.getWallet());
            merchants.save(payment.getMerchant());
            payments.save(payment);
        });
    }

    public List<Payment> findAll() {
        return UnitOfWork.execute(payments::findAll);
    }

    /** A customer only ever sees payments made from their own wallet. */
    public List<Payment> findOwnPayments(Long payerUserId) {
        return UnitOfWork.execute(() -> payments.findByPayerId(payerUserId));
    }

    /** A merchant only ever sees payments received by the shop they operate. */
    public List<Payment> findPaymentsReceived(Long operatorUserId) {
        return UnitOfWork.execute(() -> payments.findByMerchantOperatorId(operatorUserId));
    }
}
