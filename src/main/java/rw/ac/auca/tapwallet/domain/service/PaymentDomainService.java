package rw.ac.auca.tapwallet.domain.service;

import rw.ac.auca.tapwallet.domain.exception.InactiveAccountException;
import rw.ac.auca.tapwallet.domain.exception.SelfPaymentException;
import rw.ac.auca.tapwallet.domain.model.merchant.Merchant;
import rw.ac.auca.tapwallet.domain.model.payment.Payment;
import rw.ac.auca.tapwallet.domain.model.shared.Money;
import rw.ac.auca.tapwallet.domain.model.wallet.Wallet;

/**
 * Domain service. Paying spans two aggregates (Wallet and Merchant), so the rule
 * belongs here rather than inside either one of them.
 */
public class PaymentDomainService {

    public Payment pay(Wallet wallet, Merchant merchant, Money amount) {
        if (wallet.getOwner() != null && wallet.getOwner().isSameAs(merchant.getOperator())) {
            throw new SelfPaymentException("You cannot pay your own shop.");
        }
        if (!merchant.isActive()) {
            throw new InactiveAccountException("This merchant is not accepting payments right now.");
        }

        wallet.debit(amount);
        merchant.receive(amount);

        return Payment.record(wallet, merchant, amount);
    }

    public void reverse(Payment payment) {
        payment.markReversed();
        payment.getMerchant().refund(payment.getAmount());
        payment.getWallet().credit(payment.getAmount());
    }
}
