package rw.ac.auca.tapwallet.presentation.bean;

import rw.ac.auca.tapwallet.domain.exception.DomainException;
import rw.ac.auca.tapwallet.domain.model.wallet.Wallet;
import rw.ac.auca.tapwallet.infrastructure.config.ServiceRegistry;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * The signed-in customer's own wallet. The owner id always comes from the session,
 * never from the request, so a customer cannot read or top up anyone else's wallet.
 */
@ManagedBean(name = "customerWallet")
@ViewScoped
public class CustomerWalletBean implements Serializable {

    @ManagedProperty(value = "#{userSession}")
    private UserSessionBean userSession;

    private BigDecimal topUpAmount;

    public Wallet getWallet() {
        return ServiceRegistry.wallets().findOwnWallet(userSession.getUserId()).orElse(null);
    }

    public String topUp() {
        try {
            ServiceRegistry.wallets().topUpOwnWallet(userSession.getUserId(), topUpAmount);
            topUpAmount = null;
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Wallet topped up", "Your new balance is shown below."));
            return null;
        } catch (DomainException failure) {
            addError(failure.getMessage());
            return null;
        } catch (RuntimeException failure) {
            addError("Could not top up your wallet. Please try again.");
            return null;
        }
    }

    private void addError(String detail) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Top up failed", detail));
    }

    public void setUserSession(UserSessionBean userSession) {
        this.userSession = userSession;
    }

    public BigDecimal getTopUpAmount() {
        return topUpAmount;
    }

    public void setTopUpAmount(BigDecimal topUpAmount) {
        this.topUpAmount = topUpAmount;
    }
}
