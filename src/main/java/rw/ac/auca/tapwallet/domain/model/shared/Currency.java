package rw.ac.auca.tapwallet.domain.model.shared;

public enum Currency {

    RWF("Rwandan Franc"),
    USD("US Dollar"),
    EUR("Euro");

    private final String label;

    Currency(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
