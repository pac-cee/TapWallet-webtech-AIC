package rw.ac.auca.tapwallet.presentation.bean;

import rw.ac.auca.tapwallet.domain.model.merchant.Merchant;
import rw.ac.auca.tapwallet.domain.model.payment.Payment;
import rw.ac.auca.tapwallet.infrastructure.config.ServiceRegistry;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import java.io.Serializable;
import java.util.List;

/**
 * The signed-in merchant's own shop and the payments it received. Both reads are
 * keyed on the session user id, so one merchant can never see another's takings.
 */
@ManagedBean(name = "merchantPortal")
@ViewScoped
public class MerchantPortalBean implements Serializable {

    @ManagedProperty(value = "#{userSession}")
    private UserSessionBean userSession;

    public Merchant getMyShop() {
        return ServiceRegistry.merchants().findOwnShop(userSession.getUserId()).orElse(null);
    }

    public List<Payment> getPaymentsReceived() {
        return ServiceRegistry.payments().findPaymentsReceived(userSession.getUserId());
    }

    public boolean isHasShop() {
        return getMyShop() != null;
    }

    public void setUserSession(UserSessionBean userSession) {
        this.userSession = userSession;
    }
}
