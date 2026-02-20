package application.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "widgets")
public class Widget {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @Column(nullable = false)
  private Integer quantity;

  @Column(nullable = false)
  private java.math.BigDecimal price;

  protected Widget() {
  }

  public Widget(String name, OffsetDateTime createdAt) {
    this.name = name;
    this.createdAt = createdAt;
  }

  public Widget(String name, OffsetDateTime createdAt, Integer quantity, java.math.BigDecimal price) {
    this.name = name;
    this.createdAt = createdAt;
    this.quantity = quantity;
    this.price = price;
  }

  public Long getId() {
    return id;
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

  /**
   * Update mutable widget details. Allows JPA to issue true UPDATE statements.
   * This is essential for triggering the UPDATE branch of the audit trigger.
   *
   * @param name new widget name
   * @param quantity new quantity
   * @param price new price
   */
  public void updateDetails(String name, Integer quantity, java.math.BigDecimal price) {
    this.name = name;
    this.quantity = quantity;
    this.price = price;
  }
}

