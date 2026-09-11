package rw.ac.auca.tapwallet.domain.model.payment;

public enum PaymentStatus {

    COMPLETED("Completed"),
    REVERSED("Reversed");

    private final String label;

    PaymentStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
