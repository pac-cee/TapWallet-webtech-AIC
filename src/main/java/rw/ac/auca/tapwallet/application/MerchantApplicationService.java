package rw.ac.auca.tapwallet.application;

import rw.ac.auca.tapwallet.domain.exception.DomainException;
import rw.ac.auca.tapwallet.domain.model.merchant.Merchant;
import rw.ac.auca.tapwallet.domain.model.merchant.MerchantCode;
import rw.ac.auca.tapwallet.domain.model.shared.AccountStatus;
import rw.ac.auca.tapwallet.domain.model.shared.Currency;
import rw.ac.auca.tapwallet.domain.model.shared.Money;
import rw.ac.auca.tapwallet.domain.model.user.Role;
import rw.ac.auca.tapwallet.domain.model.user.User;
import rw.ac.auca.tapwallet.domain.repository.MerchantRepository;
import rw.ac.auca.tapwallet.domain.repository.PaymentRepository;
import rw.ac.auca.tapwallet.domain.repository.UserRepository;
import rw.ac.auca.tapwallet.infrastructure.persistence.UnitOfWork;

import java.util.List;
import java.util.Optional;

/**
 * Administrator use cases for shops, plus the merchant's own read of their shop.
 */
public class MerchantApplicationService {

    private final MerchantRepository merchants;
    private final UserRepository users;
    private final PaymentRepository payments;

    public MerchantApplicationService(MerchantRepository merchants, UserRepository users, PaymentRepository payments) {
        this.merchants = merchants;
        this.users = users;
        this.payments = payments;
    }

    public Merchant register(String businessName, String code, Long operatorId, Currency currency) {
        MerchantCode merchantCode = MerchantCode.of(code);

        return UnitOfWork.execute(() -> {
            User operator = users.findById(operatorId)
                    .orElseThrow(() -> new DomainException("That operator no longer exists."));
            if (!operator.hasRole(Role.MERCHANT)) {
                throw new DomainException("The operator must be a user with the MERCHANT role.");
            }
            if (merchants.findByCode(merchantCode.getValue()).isPresent()) {
                throw new DomainException("That merchant code is already taken.");
            }
            if (merchants.findByOperatorId(operatorId).isPresent()) {
                throw new DomainException("This user already operates a shop.");
            }
            Currency shopCurrency = currency == null ? Currency.RWF : currency;
            return merchants.save(Merchant.register(businessName, merchantCode, operator, Money.zero(shopCurrency)));
        });
    }

    public void update(Long merchantId, String businessName, String code, AccountStatus status) {
        MerchantCode merchantCode = MerchantCode.of(code);

        UnitOfWork.run(() -> {
            Merchant merchant = merchants.findById(merchantId)
                    .orElseThrow(() -> new DomainException("That merchant no longer exists."));

            Optional<Merchant> sameCode = merchants.findByCode(merchantCode.getValue());
            if (sameCode.isPresent() && !sameCode.get().getId().equals(merchantId)) {
                throw new DomainException("That merchant code is already taken.");
            }

            merchant.rename(businessName);
            merchant.changeCode(merchantCode);
            if (status == AccountStatus.FROZEN) {
                merchant.freeze();
            } else {
                merchant.activate();
            }
            merchants.save(merchant);
        });
    }

    public void delete(Long merchantId) {
        UnitOfWork.run(() -> {
            Merchant merchant = merchants.findById(merchantId)
                    .orElseThrow(() -> new DomainException("That merchant no longer exists."));
            if (!payments.findByMerchantId(merchantId).isEmpty()) {
                throw new DomainException("This merchant has payment history and cannot be deleted.");
            }
            merchants.delete(merchant);
        });
    }

    public List<Merchant> findAll() {
        return UnitOfWork.execute(merchants::findAll);
    }

    public List<Merchant> findActive() {
        return UnitOfWork.execute(merchants::findActive);
    }

    public Optional<Merchant> findById(Long id) {
        return UnitOfWork.execute(() -> merchants.findById(id));
    }

    /** A merchant may only ever read the shop they personally operate. */
    public Optional<Merchant> findOwnShop(Long operatorUserId) {
        return UnitOfWork.execute(() -> merchants.findByOperatorId(operatorUserId));
    }
}
