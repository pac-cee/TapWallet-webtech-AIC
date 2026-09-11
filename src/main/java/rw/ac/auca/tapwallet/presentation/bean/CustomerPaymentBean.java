package rw.ac.auca.tapwallet.presentation.bean;

import rw.ac.auca.tapwallet.domain.exception.DomainException;
import rw.ac.auca.tapwallet.domain.model.merchant.Merchant;
import rw.ac.auca.tapwallet.domain.model.payment.Payment;
import rw.ac.auca.tapwallet.infrastructure.config.ServiceRegistry;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * The signed-in customer paying a merchant, and reading back their own history.
 */
@ManagedBean(name = "customerPayment")
@ViewScoped
public class CustomerPaymentBean implements Serializable {

    @ManagedProperty(value = "#{userSession}")
    private UserSessionBean userSession;

    private Long merchantId;
    private BigDecimal amount;

    public List<Merchant> getMerchantChoices() {
        return ServiceRegistry.merchants().findActive();
    }

    /** Scoped to the signed-in customer — never to an id from the browser. */
    public List<Payment> getMyPayments() {
        return ServiceRegistry.payments().findOwnPayments(userSession.getUserId());
    }

    public String pay() {
        try {
            Payment payment = ServiceRegistry.payments().payMerchant(userSession.getUserId(), merchantId, amount);
            merchantId = null;
            amount = null;
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Payment sent",
                            "Reference " + payment.getReference() + " for " + payment.getAmount() + "."));
            return null;
        } catch (DomainException failure) {
            addError(failure.getMessage());
            return null;
        } catch (RuntimeException failure) {
            addError("Could not send the payment. Please try again.");
            return null;
        }
    }

    private void addError(String detail) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Payment failed", detail));
    }

    public void setUserSession(UserSessionBean userSession) {
        this.userSession = userSession;
    }

    public Long getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
