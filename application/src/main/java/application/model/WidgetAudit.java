package application.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;
import java.time.OffsetDateTime;

/**
 * Read-only audit log entity. @Immutable ensures Hibernate will never
 * issue UPDATE or DELETE statements against this table, protecting audit integrity.
 */
@Entity
@Immutable
@Table(name = "widgets_audit")
public class WidgetAudit {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "audit_id")
  private Long auditId;

  @Column(nullable = false)
  private String operation;

  @Column(name = "widget_id")
  private Long widgetId;

  @Column
  private String name;

  @Column(name = "created_at")
  private OffsetDateTime createdAt;

  @Column
  private Integer quantity;

  @Column
  private java.math.BigDecimal price;

  @Column(name = "changed_at", nullable = false)
  private OffsetDateTime changedAt;

  @Column(name = "changed_by")
  private String changedBy;

  protected WidgetAudit() {
  }

  public Long getAuditId() {
    return auditId;
  }

  public String getOperation() {
    return operation;
  }

  public Long getWidgetId() {
    return widgetId;
  }

  public String getName() {
    return name;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public Integer getQuantity() {
    return quantity;
  }

  public java.math.BigDecimal getPrice() {
    return price;
  }

  public OffsetDateTime getChangedAt() {
    return changedAt;
  }

  public String getChangedBy() {
    return changedBy;
  }
}

