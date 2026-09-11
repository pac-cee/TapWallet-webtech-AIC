package rw.ac.auca.tapwallet.domain.model.user;

public enum Role {

    ADMIN("Administrator", "/admin/users.xhtml"),
    CUSTOMER("Customer", "/customer/wallet.xhtml"),
    MERCHANT("Merchant", "/merchant/payments.xhtml");

    private final String label;
    private final String homePage;

    Role(String label, String homePage) {
        this.label = label;
        this.homePage = homePage;
    }

    public String getLabel() {
        return label;
    }

    public String getHomePage() {
        return homePage;
    }
}
