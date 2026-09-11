package rw.ac.auca.tapwallet.presentation.bean;

import rw.ac.auca.tapwallet.domain.exception.DomainException;
import rw.ac.auca.tapwallet.domain.model.payment.Payment;
import rw.ac.auca.tapwallet.infrastructure.config.ServiceRegistry;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.List;

/**
 * Administrator oversight of every payment on the platform, including reversal.
 */
@ManagedBean(name = "adminPayment")
@ViewScoped
public class AdminPaymentBean implements Serializable {

    public List<Payment> getPayments() {
        return ServiceRegistry.payments().findAll();
    }

    public String reverse(Long paymentId) {
        try {
            ServiceRegistry.payments().reverse(paymentId);
            return "/admin/payments.xhtml?faces-redirect=true";
        } catch (DomainException failure) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Could not reverse payment", failure.getMessage()));
            return null;
        } catch (RuntimeException failure) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Could not reverse payment", "Please try again."));
            return null;
        }
    }
}
