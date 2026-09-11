package rw.ac.auca.tapwallet.domain.model.shared;

public enum AccountStatus {

    ACTIVE("Active"),
    FROZEN("Frozen");

    private final String label;

    AccountStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public boolean isActive() {
        return this == ACTIVE;
    }
}
