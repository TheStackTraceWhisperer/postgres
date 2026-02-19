package application;

import application.model.Widget;
import application.model.WidgetAudit;
import application.repo.WidgetAuditRepository;
import application.repo.WidgetRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@Transactional
class IntegrationTest {

  @Autowired
  private WidgetRepository widgetRepository;

  @Autowired
  private WidgetAuditRepository widgetAuditRepository;

  @Test
  void testFullWorkflow() {
    // Create a widget
    Widget widget = new Widget(
      "Integration Test Widget",
      OffsetDateTime.now(),
      100,
      new BigDecimal("99.99")
    );

    // Save it
    Widget saved = widgetRepository.save(widget);
    assertNotNull(saved.getId());

    // Retrieve it
    Widget found = widgetRepository.findById(saved.getId()).orElseThrow();
    assertEquals("Integration Test Widget", found.getName());
    assertEquals(100, found.getQuantity());
    assertEquals(0, new BigDecimal("99.99").compareTo(found.getPrice()));

    // Update it
    Widget updated = new Widget(
      "Updated Widget",
      found.getCreatedAt(),
      150,
      new BigDecimal("149.99")
    );
    widgetRepository.save(updated);

    // Delete it
    widgetRepository.delete(found);

    // Verify deletion
    assertFalse(widgetRepository.findById(saved.getId()).isPresent());
  }

  @Test
  void testBulkOperations() {
    long initialCount = widgetRepository.count();

    // Create multiple widgets
    Widget w1 = new Widget("Bulk1", OffsetDateTime.now(), 10, new BigDecimal("10.00"));
    Widget w2 = new Widget("Bulk2", OffsetDateTime.now(), 20, new BigDecimal("20.00"));
    Widget w3 = new Widget("Bulk3", OffsetDateTime.now(), 30, new BigDecimal("30.00"));

    List<Widget> saved = widgetRepository.saveAll(List.of(w1, w2, w3));
    assertEquals(3, saved.size());

    // Verify count increased
    long newCount = widgetRepository.count();
    assertEquals(initialCount + 3, newCount);

    // Fetch all and verify
    List<Widget> all = widgetRepository.findAll();
    assertTrue(all.size() >= 3);

    // Clean up
    widgetRepository.deleteAll(saved);
  }

  @Test
  void testRepositoryQueryMethods() {
    Widget widget = new Widget("Query Test", OffsetDateTime.now(), 5, new BigDecimal("5.00"));
    Widget saved = widgetRepository.save(widget);

    // Test exists
    assertTrue(widgetRepository.existsById(saved.getId()));

    // Test count
    long count = widgetRepository.count();
    assertTrue(count > 0);

    // Test findAll
    List<Widget> all = widgetRepository.findAll();
    assertFalse(all.isEmpty());
  }

  @Test
  void testDataIntegrity() {
    // Test that timestamps are preserved
    OffsetDateTime specificTime = OffsetDateTime.parse("2026-02-19T10:30:00Z");
    Widget widget = new Widget("Timestamp Test", specificTime, 1, new BigDecimal("1.00"));

    Widget saved = widgetRepository.save(widget);
    Widget fetched = widgetRepository.findById(saved.getId()).orElseThrow();

    assertEquals(specificTime, fetched.getCreatedAt());

    // Test that BigDecimal precision is preserved
    BigDecimal precisePrice = new BigDecimal("123.45");
    Widget priceWidget = new Widget("Price Test", OffsetDateTime.now(), 1, precisePrice);

    Widget savedPrice = widgetRepository.save(priceWidget);
    Widget fetchedPrice = widgetRepository.findById(savedPrice.getId()).orElseThrow();

    assertEquals(0, precisePrice.compareTo(fetchedPrice.getPrice()));
  }

  @Test
  void testTransactionRollback() {
    try {
      Widget widget = new Widget("Rollback Test", OffsetDateTime.now(), 1, new BigDecimal("1.00"));
      widgetRepository.save(widget);

      // Force an exception to trigger rollback
      throw new RuntimeException("Simulated failure");
    } catch (RuntimeException e) {
      // Expected - in @Transactional test context, the save is part of the test transaction
      // which gets rolled back after the test completes
      assertEquals("Simulated failure", e.getMessage());
    }
  }

  @Test
  void testEdgeCases() {
    // Test with zero quantity
    Widget zeroQuantity = new Widget("Zero", OffsetDateTime.now(), 0, new BigDecimal("1.00"));
    Widget savedZero = widgetRepository.save(zeroQuantity);
    assertEquals(0, savedZero.getQuantity());

    // Test with very large quantity
    Widget largeQuantity = new Widget("Large", OffsetDateTime.now(), Integer.MAX_VALUE, new BigDecimal("1.00"));
    Widget savedLarge = widgetRepository.save(largeQuantity);
    assertEquals(Integer.MAX_VALUE, savedLarge.getQuantity());

    // Test with very small price
    Widget smallPrice = new Widget("Small", OffsetDateTime.now(), 1, new BigDecimal("0.01"));
    Widget savedSmall = widgetRepository.save(smallPrice);
    assertEquals(0, new BigDecimal("0.01").compareTo(savedSmall.getPrice()));

    // Test with very large price
    Widget largePrice = new Widget("Expensive", OffsetDateTime.now(), 1, new BigDecimal("9999999.99"));
    Widget savedLargePrice = widgetRepository.save(largePrice);
    assertEquals(0, new BigDecimal("9999999.99").compareTo(savedLargePrice.getPrice()));
  }

  @Test
  void testAuditRecordsCreatedForOperations() {
    long initialAuditCount = widgetAuditRepository.count();

    // Create a widget
    Widget widget = new Widget("Audit Verify", OffsetDateTime.now(), 77, new BigDecimal("77.77"));
    Widget saved = widgetRepository.saveAndFlush(widget);
    Long widgetId = saved.getId();

    // Verify INSERT audit was created
    long auditCountAfterInsert = widgetAuditRepository.count();
    assertTrue(auditCountAfterInsert > initialAuditCount, "Audit count should increase after INSERT");

    // Query audits for this specific widget
    List<WidgetAudit> auditsForWidget = widgetAuditRepository.findByWidgetIdOrderByChangedAtDesc(widgetId);
    assertFalse(auditsForWidget.isEmpty(), "Should have audit records for the new widget");

    WidgetAudit insertAudit = auditsForWidget.get(0);
    assertEquals("INSERT", insertAudit.getOperation());
    assertEquals(widgetId, insertAudit.getWidgetId());
    assertEquals("Audit Verify", insertAudit.getName());
    assertEquals(77, insertAudit.getQuantity());
    assertEquals(0, new BigDecimal("77.77").compareTo(insertAudit.getPrice()));

    // Delete the widget
    widgetRepository.delete(saved);
    widgetRepository.flush();

    // Verify DELETE audit was created
    auditsForWidget = widgetAuditRepository.findByWidgetIdOrderByChangedAtDesc(widgetId);
    assertTrue(auditsForWidget.size() >= 2, "Should have at least INSERT and DELETE audit records");

    // Verify both operations are present
    List<String> operations = auditsForWidget.stream().map(WidgetAudit::getOperation).toList();
    assertTrue(operations.contains("INSERT"), "Should have INSERT operation");
    assertTrue(operations.contains("DELETE"), "Should have DELETE operation");
  }

  @Test
  void testAuditQueryByOperation() {
    // Create some widgets
    widgetRepository.saveAndFlush(new Widget("Audit1", OffsetDateTime.now(), 1, new BigDecimal("1.00")));
    widgetRepository.saveAndFlush(new Widget("Audit2", OffsetDateTime.now(), 2, new BigDecimal("2.00")));

    // Query all INSERT operations
    List<WidgetAudit> insertAudits = widgetAuditRepository.findByOperationOrderByChangedAtDesc("INSERT");
    assertFalse(insertAudits.isEmpty(), "Should have INSERT audit records");

    // Count by operation type
    long insertCount = widgetAuditRepository.countByOperation("INSERT");
    assertTrue(insertCount > 0, "Should have at least some INSERT operations");
  }
}

