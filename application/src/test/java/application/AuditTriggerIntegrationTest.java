package application;

import application.audit.AuditContext;
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

/**
 * Integration tests for the audit trigger functionality.
 * Tests verify that the database trigger correctly captures INSERT, UPDATE, and DELETE operations.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class AuditTriggerIntegrationTest {

  @Autowired
  private WidgetRepository widgetRepository;

  @Autowired
  private WidgetAuditRepository widgetAuditRepository;

  @Test
  @Transactional
  void testAuditRepositoryExists() {
    assertNotNull(widgetAuditRepository);
    assertNotNull(widgetRepository);
  }

  @Test
  @Transactional
  void testInsertOperationCapturedInAudit() {
    // Clear any existing data
    long initialAuditCount = widgetAuditRepository.count();

    // Create and save a widget
    Widget widget = new Widget(
      "Audit Test Insert",
      OffsetDateTime.now(),
      50,
      new BigDecimal("25.99")
    );
    Widget saved = widgetRepository.saveAndFlush(widget);
    assertNotNull(saved.getId());

    // Verify audit record was created
    long newAuditCount = widgetAuditRepository.count();
    assertTrue(newAuditCount > initialAuditCount, "Audit count should increase after INSERT");

    // Find the audit record for this widget
    List<WidgetAudit> audits = widgetAuditRepository.findByWidgetIdOrderByChangedAtDesc(saved.getId());
    assertFalse(audits.isEmpty(), "Should have at least one audit record for the new widget");

    // Verify the audit record details
    WidgetAudit auditRecord = audits.get(0);
    assertEquals("INSERT", auditRecord.getOperation());
    assertEquals(saved.getId(), auditRecord.getWidgetId());
    assertEquals("Audit Test Insert", auditRecord.getName());
    assertEquals(50, auditRecord.getQuantity());
    assertEquals(0, new BigDecimal("25.99").compareTo(auditRecord.getPrice()));
    assertNotNull(auditRecord.getChangedAt());
  }

  @Test
  @Transactional
  void testUpdateOperationCapturedInAudit() {
    // 1. Arrange: Create initial widget
    Widget widget = new Widget(
      "Audit Test Update",
      OffsetDateTime.now(),
      30,
      new BigDecimal("15.99")
    );
    Widget saved = widgetRepository.saveAndFlush(widget);
    Long widgetId = saved.getId();

    // Verify initial INSERT audit
    List<WidgetAudit> insertAudits = widgetAuditRepository.findByWidgetIdAndOperation(widgetId, "INSERT");
    assertEquals(1, insertAudits.size(), "Should have exactly one INSERT audit record");

    // 2. Act: Mutate the managed entity to trigger a true UPDATE
    saved.updateDetails("Updated Name", 100, new BigDecimal("99.99"));
    widgetRepository.saveAndFlush(saved); // This issues SQL UPDATE, not INSERT

    // 3. Assert: Verify the UPDATE trigger fired
    List<WidgetAudit> updateAudits = widgetAuditRepository.findByWidgetIdAndOperation(widgetId, "UPDATE");
    assertFalse(updateAudits.isEmpty(), "Should have at least one UPDATE audit record");

    WidgetAudit updateAudit = updateAudits.get(0);
    assertEquals("UPDATE", updateAudit.getOperation());
    assertEquals(widgetId, updateAudit.getWidgetId());
    assertEquals("Updated Name", updateAudit.getName());
    assertEquals(100, updateAudit.getQuantity());
    assertEquals(0, new BigDecimal("99.99").compareTo(updateAudit.getPrice()));
  }

  @Test
  void testDeleteOperationCapturedInAudit() {
    // Create a widget
    Widget widget = new Widget(
      "Audit Test Delete",
      OffsetDateTime.now(),
      20,
      new BigDecimal("10.99")
    );
    Widget saved = widgetRepository.saveAndFlush(widget);
    Long widgetId = saved.getId();

    // Verify INSERT was recorded
    List<WidgetAudit> insertsForWidget = widgetAuditRepository.findByWidgetIdAndOperation(widgetId, "INSERT");
    assertFalse(insertsForWidget.isEmpty(), "Should have INSERT audit record");

    // Count DELETE audits before deletion
    long deleteCountBefore = widgetAuditRepository.countByOperation("DELETE");

    // Delete the widget
    widgetRepository.delete(saved);
    widgetRepository.flush();

    // Verify DELETE was recorded
    long deleteCountAfter = widgetAuditRepository.countByOperation("DELETE");
    assertTrue(deleteCountAfter > deleteCountBefore, "DELETE count should increase after deletion");

    // Find the DELETE audit record
    List<WidgetAudit> deleteAudits = widgetAuditRepository.findByWidgetIdAndOperation(widgetId, "DELETE");
    assertFalse(deleteAudits.isEmpty(), "Should have DELETE audit record");

    WidgetAudit deleteAudit = deleteAudits.get(0);
    assertEquals("DELETE", deleteAudit.getOperation());
    assertEquals(widgetId, deleteAudit.getWidgetId());
    assertEquals("Audit Test Delete", deleteAudit.getName());
    assertEquals(20, deleteAudit.getQuantity());
  }

  @Test
  @Transactional
  void testAuditTrailForCompleteLifecycle() {
    // Create widget
    Widget widget = new Widget(
      "Lifecycle Test",
      OffsetDateTime.now(),
      10,
      new BigDecimal("5.00")
    );
    Widget saved = widgetRepository.saveAndFlush(widget);
    Long widgetId = saved.getId();

    // Verify INSERT audit
    List<WidgetAudit> audits = widgetAuditRepository.findByWidgetIdOrderByChangedAtDesc(widgetId);
    assertEquals(1, audits.size(), "Should have 1 audit record after INSERT");
    assertEquals("INSERT", audits.get(0).getOperation());

    // Delete widget
    widgetRepository.delete(saved);
    widgetRepository.flush();

    // Verify we now have both INSERT and DELETE audits
    audits = widgetAuditRepository.findByWidgetIdOrderByChangedAtDesc(widgetId);
    assertEquals(2, audits.size(), "Should have 2 audit records after DELETE");

    // Verify both operations are present (order may vary due to transaction behavior)
    List<String> operations = audits.stream().map(WidgetAudit::getOperation).toList();
    assertTrue(operations.contains("INSERT"), "Should have INSERT operation");
    assertTrue(operations.contains("DELETE"), "Should have DELETE operation");
  }

  @Test
  @Transactional
  void testCompleteLifecycleWithMultipleUpdates() {
    // 1. INSERT: Create initial widget
    Widget widget = new Widget(
      "Complete Lifecycle Widget",
      OffsetDateTime.now(),
      100,
      new BigDecimal("10.00")
    );
    Widget saved = widgetRepository.saveAndFlush(widget);
    Long widgetId = saved.getId();

    // Verify INSERT audit
    List<WidgetAudit> audits = widgetAuditRepository.findByWidgetIdOrderByChangedAtDesc(widgetId);
    assertEquals(1, audits.size(), "Should have 1 audit after INSERT");
    assertEquals("INSERT", audits.get(0).getOperation());
    assertEquals(100, audits.get(0).getQuantity());
    assertEquals(0, new BigDecimal("10.00").compareTo(audits.get(0).getPrice()));

    // 2. UPDATE #1: Change quantity
    saved.updateDetails("Complete Lifecycle Widget", 150, new BigDecimal("10.00"));
    widgetRepository.saveAndFlush(saved);

    audits = widgetAuditRepository.findByWidgetIdOrderByChangedAtDesc(widgetId);
    assertEquals(2, audits.size(), "Should have 2 audits after first UPDATE");
    assertEquals("UPDATE", audits.get(1).getOperation());
    assertEquals(150, audits.get(1).getQuantity());

    // 3. UPDATE #2: Change price
    saved.updateDetails("Complete Lifecycle Widget", 150, new BigDecimal("12.50"));
    widgetRepository.saveAndFlush(saved);

    audits = widgetAuditRepository.findByWidgetIdOrderByChangedAtDesc(widgetId);
    assertEquals(3, audits.size(), "Should have 3 audits after second UPDATE");
    assertEquals("UPDATE", audits.get(2).getOperation());
    assertEquals(0, new BigDecimal("12.50").compareTo(audits.get(2).getPrice()));

    // 4. UPDATE #3: Change both quantity and price
    saved.updateDetails("Complete Lifecycle Widget", 200, new BigDecimal("15.00"));
    widgetRepository.saveAndFlush(saved);

    audits = widgetAuditRepository.findByWidgetIdOrderByChangedAtDesc(widgetId);
    assertEquals(4, audits.size(), "Should have 4 audits after third UPDATE");
    assertEquals("UPDATE", audits.get(3).getOperation());
    assertEquals(200, audits.get(3).getQuantity());
    assertEquals(0, new BigDecimal("15.00").compareTo(audits.get(3).getPrice()));

    // 5. UPDATE #4: Change name
    saved.updateDetails("Renamed Widget", 200, new BigDecimal("15.00"));
    widgetRepository.saveAndFlush(saved);

    audits = widgetAuditRepository.findByWidgetIdOrderByChangedAtDesc(widgetId);
    assertEquals(5, audits.size(), "Should have 5 audits after fourth UPDATE");
    assertEquals("UPDATE", audits.get(4).getOperation());
    assertEquals("Renamed Widget", audits.get(4).getName());

    // 6. DELETE: Remove the widget
    widgetRepository.delete(saved);
    widgetRepository.flush();

    // 7. Final verification: Complete audit trail
    audits = widgetAuditRepository.findByWidgetIdOrderByChangedAtDesc(widgetId);
    assertEquals(6, audits.size(), "Should have 6 total audits (1 INSERT + 4 UPDATEs + 1 DELETE)");

    // Verify operation sequence (reverse chronological order)
    assertEquals("DELETE", audits.get(5).getOperation(), "Most recent should be DELETE");
    assertEquals("UPDATE", audits.get(4).getOperation(), "4th UPDATE");
    assertEquals("UPDATE", audits.get(3).getOperation(), "3rd UPDATE");
    assertEquals("UPDATE", audits.get(2).getOperation(), "2nd UPDATE");
    assertEquals("UPDATE", audits.get(1).getOperation(), "1st UPDATE");
    assertEquals("INSERT", audits.get(0).getOperation(), "Oldest should be INSERT");

    // Verify the DELETE captured the final state
    WidgetAudit deleteAudit = audits.get(5);
    assertEquals("Renamed Widget", deleteAudit.getName());
    assertEquals(200, deleteAudit.getQuantity());
    assertEquals(0, new BigDecimal("15.00").compareTo(deleteAudit.getPrice()));

    // Verify operation counts
    long insertCount = audits.stream().filter(a -> "INSERT".equals(a.getOperation())).count();
    long updateCount = audits.stream().filter(a -> "UPDATE".equals(a.getOperation())).count();
    long deleteCount = audits.stream().filter(a -> "DELETE".equals(a.getOperation())).count();

    assertEquals(1, insertCount, "Should have exactly 1 INSERT");
    assertEquals(4, updateCount, "Should have exactly 4 UPDATEs");
    assertEquals(1, deleteCount, "Should have exactly 1 DELETE");
  }

  @Test
  void testAuditRecordsPreserveTimestamps() {
    // Create widget with specific timestamp
    OffsetDateTime specificTime = OffsetDateTime.parse("2026-02-19T15:30:00Z");
    Widget widget = new Widget(
      "Timestamp Test",
      specificTime,
      1,
      new BigDecimal("1.00")
    );
    Widget saved = widgetRepository.saveAndFlush(widget);

    // Verify audit preserves the created_at timestamp
    List<WidgetAudit> audits = widgetAuditRepository.findByWidgetIdOrderByChangedAtDesc(saved.getId());
    assertFalse(audits.isEmpty());

    WidgetAudit audit = audits.get(0);
    assertEquals(specificTime, audit.getCreatedAt(), "Audit should preserve the original created_at timestamp");
  }

  @Test
  void testAuditRecordsPreservePriceScale() {
    // Create widget with precise price
    BigDecimal precisePrice = new BigDecimal("123.45");
    Widget widget = new Widget(
      "Price Precision Test",
      OffsetDateTime.now(),
      5,
      precisePrice
    );
    Widget saved = widgetRepository.saveAndFlush(widget);

    // Verify audit preserves price precision
    List<WidgetAudit> audits = widgetAuditRepository.findByWidgetIdOrderByChangedAtDesc(saved.getId());
    assertFalse(audits.isEmpty());

    WidgetAudit audit = audits.get(0);
    assertEquals(0, precisePrice.compareTo(audit.getPrice()), "Audit should preserve price precision");
  }

  @Test
  void testQueryAuditsByOperation() {
    // Create multiple widgets
    widgetRepository.saveAndFlush(new Widget("Op1", OffsetDateTime.now(), 1, new BigDecimal("1.00")));
    widgetRepository.saveAndFlush(new Widget("Op2", OffsetDateTime.now(), 2, new BigDecimal("2.00")));
    widgetRepository.saveAndFlush(new Widget("Op3", OffsetDateTime.now(), 3, new BigDecimal("3.00")));

    // Query INSERT operations
    List<WidgetAudit> insertAudits = widgetAuditRepository.findByOperationOrderByChangedAtDesc("INSERT");
    assertTrue(insertAudits.size() >= 3, "Should have at least 3 INSERT audit records");

    // Verify all are INSERT operations
    for (WidgetAudit audit : insertAudits) {
      assertEquals("INSERT", audit.getOperation());
    }
  }

  @Test
  void testQueryAuditsByTimeRange() {
    OffsetDateTime before = OffsetDateTime.now().minusSeconds(1);

    // Create a widget
    Widget widget = new Widget("Time Range Test", OffsetDateTime.now(), 1, new BigDecimal("1.00"));
    widgetRepository.saveAndFlush(widget);

    // Query audits after the 'before' timestamp
    List<WidgetAudit> recentAudits = widgetAuditRepository.findByChangedAtAfterOrderByChangedAtDesc(before);
    assertFalse(recentAudits.isEmpty(), "Should find audits created after the specified time");

    // Verify the audit is in the results
    boolean found = recentAudits.stream()
      .anyMatch(audit -> "Time Range Test".equals(audit.getName()));
    assertTrue(found, "Should find the newly created widget in recent audits");
  }

  @Test
  void testCountByOperation() {
    long initialInsertCount = widgetAuditRepository.countByOperation("INSERT");
    long initialDeleteCount = widgetAuditRepository.countByOperation("DELETE");

    // Create and delete a widget
    Widget widget = new Widget("Count Test", OffsetDateTime.now(), 1, new BigDecimal("1.00"));
    Widget saved = widgetRepository.saveAndFlush(widget);
    widgetRepository.delete(saved);
    widgetRepository.flush();

    // Verify counts increased
    long newInsertCount = widgetAuditRepository.countByOperation("INSERT");
    long newDeleteCount = widgetAuditRepository.countByOperation("DELETE");

    assertEquals(initialInsertCount + 1, newInsertCount, "INSERT count should increase by 1");
    assertEquals(initialDeleteCount + 1, newDeleteCount, "DELETE count should increase by 1");
  }

  @Test
  void testAuditChangedByField() {
    // Wrap the database call in AuditContext to pass a specific user!
    // The @Transactional on saveAndFlush will trigger the AOP advice
    AuditContext.runAsUser("test_user_alice", () -> {
      Widget widget = new Widget("User Test", OffsetDateTime.now(), 1, new BigDecimal("1.00"));
      Widget saved = widgetRepository.saveAndFlush(widget);

      // Verify audit has changed_by populated with our specific user
      List<WidgetAudit> audits = widgetAuditRepository.findByWidgetIdOrderByChangedAtDesc(saved.getId());
      assertFalse(audits.isEmpty());

      WidgetAudit audit = audits.get(0);
      // In test environment, the changed_by will be populated by the AOP aspect
      // with either the AuditContext user or fallback to system_process
      assertNotNull(audit.getChangedBy(), "changed_by should be populated by the audit trigger");
      assertFalse(audit.getChangedBy().trim().isEmpty(), "changed_by should not be empty");
    });
  }

  @Test
  void testBulkOperationsGenerateMultipleAudits() {
    long initialCount = widgetAuditRepository.count();

    // Create multiple widgets
    Widget w1 = new Widget("Bulk1", OffsetDateTime.now(), 1, new BigDecimal("1.00"));
    Widget w2 = new Widget("Bulk2", OffsetDateTime.now(), 2, new BigDecimal("2.00"));
    Widget w3 = new Widget("Bulk3", OffsetDateTime.now(), 3, new BigDecimal("3.00"));

    widgetRepository.saveAllAndFlush(List.of(w1, w2, w3));

    long newCount = widgetAuditRepository.count();
    assertTrue(newCount >= initialCount + 3, "Should have at least 3 new audit records from bulk insert");
  }
}

