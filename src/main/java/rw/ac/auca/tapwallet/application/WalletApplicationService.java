package rw.ac.auca.tapwallet.application;

import rw.ac.auca.tapwallet.domain.exception.DomainException;
import rw.ac.auca.tapwallet.domain.model.shared.Money;
import rw.ac.auca.tapwallet.domain.model.wallet.Wallet;
import rw.ac.auca.tapwallet.domain.repository.WalletRepository;
import rw.ac.auca.tapwallet.infrastructure.persistence.UnitOfWork;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Wallet use cases. Every customer-facing method is keyed on the signed-in user's
 * id, never on an id supplied by the browser, so one customer can never reach
 * another customer's wallet.
 */
public class WalletApplicationService {

    private final WalletRepository wallets;

    public WalletApplicationService(WalletRepository wallets) {
        this.wallets = wallets;
    }

    public Optional<Wallet> findOwnWallet(Long ownerUserId) {
        return UnitOfWork.execute(() -> wallets.findByOwnerId(ownerUserId));
    }

    public void topUpOwnWallet(Long ownerUserId, BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new DomainException("Enter an amount greater than zero.");
        }
        UnitOfWork.run(() -> {
            Wallet wallet = wallets.findByOwnerId(ownerUserId)
                    .orElseThrow(() -> new DomainException("You do not have a wallet yet."));
            wallet.credit(Money.of(amount, wallet.getBalance().getCurrency()));
            wallets.save(wallet);
        });
    }

    public List<Wallet> findAll() {
        return UnitOfWork.execute(wallets::findAll);
    }
}
