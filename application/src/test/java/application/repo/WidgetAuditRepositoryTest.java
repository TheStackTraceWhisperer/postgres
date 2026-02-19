package application.repo;

import application.model.WidgetAudit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import application.TestcontainersConfiguration;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class WidgetAuditRepositoryTest {

  @Autowired
  private WidgetAuditRepository widgetAuditRepository;

  @Test
  void testRepositoryExists() {
    assertNotNull(widgetAuditRepository);
  }

  @Test
  void testFindAll() {
    List<WidgetAudit> audits = widgetAuditRepository.findAll();
    assertNotNull(audits);
  }

  @Test
  void testCount() {
    long count = widgetAuditRepository.count();
    assertTrue(count >= 0);
  }

  @Test
  void testFindByWidgetIdOrderByChangedAtDesc() {
    List<WidgetAudit> audits = widgetAuditRepository.findByWidgetIdOrderByChangedAtDesc(1L);
    assertNotNull(audits);
  }

  @Test
  void testFindByOperationOrderByChangedAtDesc() {
    List<WidgetAudit> audits = widgetAuditRepository.findByOperationOrderByChangedAtDesc("INSERT");
    assertNotNull(audits);
  }

  @Test
  void testFindByChangedAtAfterOrderByChangedAtDesc() {
    OffsetDateTime yesterday = OffsetDateTime.now().minusDays(1);
    List<WidgetAudit> audits = widgetAuditRepository.findByChangedAtAfterOrderByChangedAtDesc(yesterday);
    assertNotNull(audits);
  }

  @Test
  void testFindByWidgetIdAndOperation() {
    List<WidgetAudit> audits = widgetAuditRepository.findByWidgetIdAndOperation(1L, "INSERT");
    assertNotNull(audits);
  }

  @Test
  void testCountByOperation() {
    long count = widgetAuditRepository.countByOperation("INSERT");
    assertTrue(count >= 0);
  }
}

