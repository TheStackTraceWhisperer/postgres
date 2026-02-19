package application.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class WidgetAuditTest {

  @Test
  void testDefaultConstructor() {
    WidgetAudit audit = new WidgetAudit();
    assertNotNull(audit);
  }

  @Test
  void testGetAuditId() {
    WidgetAudit audit = new WidgetAudit();
    assertNull(audit.getAuditId());
  }

  @Test
  void testGetOperation() {
    WidgetAudit audit = new WidgetAudit();
    assertNull(audit.getOperation());
  }

  @Test
  void testGetWidgetId() {
    WidgetAudit audit = new WidgetAudit();
    assertNull(audit.getWidgetId());
  }

  @Test
  void testGetName() {
    WidgetAudit audit = new WidgetAudit();
    assertNull(audit.getName());
  }

  @Test
  void testGetCreatedAt() {
    WidgetAudit audit = new WidgetAudit();
    assertNull(audit.getCreatedAt());
  }

  @Test
  void testGetQuantity() {
    WidgetAudit audit = new WidgetAudit();
    assertNull(audit.getQuantity());
  }

  @Test
  void testGetPrice() {
    WidgetAudit audit = new WidgetAudit();
    assertNull(audit.getPrice());
  }

  @Test
  void testGetChangedAt() {
    WidgetAudit audit = new WidgetAudit();
    assertNull(audit.getChangedAt());
  }

  @Test
  void testGetChangedBy() {
    WidgetAudit audit = new WidgetAudit();
    assertNull(audit.getChangedBy());
  }
}

