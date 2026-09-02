package rw.ac.auca.tapwallet.model;

import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

/**
 * The Class Audit.
 *
 * @author Pacifique Bakundukize
 * @version 1.0
 */
@MappedSuperclass
public class Audit {
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;

    public Audit() {
    }

    public Audit(LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
