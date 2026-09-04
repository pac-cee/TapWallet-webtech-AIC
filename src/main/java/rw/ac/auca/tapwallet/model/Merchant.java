package rw.ac.auca.tapwallet.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * The Class Merchant.
 *
 * @author Pacifique Bakundukize
 * @version 1.0
 */
@Entity
@Table(name = "merchant")
public class Merchant extends Audit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "merchant_id")
    private Long id;

    @NotBlank(message = "Business name is required")
    @Size(min = 3, max = 80, message = "Business name must be between 3 and 80 characters")
    @Column(name = "business_name", nullable = false)
    private String businessName;

    @NotBlank(message = "Merchant code is required")
    @Size(min = 4, max = 20, message = "Merchant code must be between 4 and 20 characters")
    @Column(name = "merchant_code", nullable = false, unique = true)
    private String merchantCode;

    @NotNull(message = "Operator is required")
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User operator;

    @Column(name = "status", nullable = false)
    private String status = "ACTIVE";

    public Merchant() {
    }

    public Merchant(String businessName, String merchantCode, User operator, String status) {
        this.businessName = businessName;
        this.merchantCode = merchantCode;
        this.operator = operator;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getMerchantCode() {
        return merchantCode;
    }

    public void setMerchantCode(String merchantCode) {
        this.merchantCode = merchantCode;
    }

    public User getOperator() {
        return operator;
    }

    public void setOperator(User operator) {
        this.operator = operator;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
