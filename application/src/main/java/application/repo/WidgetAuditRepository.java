package application.repo;

import application.model.WidgetAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.List;

public interface WidgetAuditRepository extends JpaRepository<WidgetAudit, Long> {

  List<WidgetAudit> findByWidgetIdOrderByChangedAtDesc(Long widgetId);

  List<WidgetAudit> findByOperationOrderByChangedAtDesc(String operation);

  List<WidgetAudit> findByChangedAtAfterOrderByChangedAtDesc(OffsetDateTime changedAt);

  @Query("SELECT wa FROM WidgetAudit wa WHERE wa.widgetId = ?1 AND wa.operation = ?2 ORDER BY wa.changedAt DESC")
  List<WidgetAudit> findByWidgetIdAndOperation(Long widgetId, String operation);

  long countByOperation(String operation);
}

