package application.audit;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

/**
 * AOP aspect that intercepts @Transactional methods and passes the current user
 * from AuditContext to the database via PostgreSQL session variable 'app.current_user'.
 *
 * This bridges the gap between the application layer (AuditContext) and the database
 * layer (Postgres triggers), allowing audit triggers to know who made the change.
 */
@Aspect
@Component
public class AuditContextAspect {

  @PersistenceContext
  private EntityManager entityManager;

  /**
   * Intercept any method annotated with @Transactional and set the Postgres session variable.
   * This runs BEFORE the transaction begins, ensuring the trigger can access the user info.
   */
  @Before("@annotation(org.springframework.transaction.annotation.Transactional) || @within(org.springframework.transaction.annotation.Transactional)")
  public void setPostgresSessionUser() {
    // 1. Grab the user from the AuditContext thread-local stack
    String currentUser = AuditContext.getCurrentUser();

    // 2. Fallback for system startup, background jobs, or when no user is set
    if (currentUser == null || currentUser.trim().isEmpty()) {
      currentUser = "system_process";
    }

    // 3. Pass it to Postgres via set_config() for the duration of this transaction
    // The true flag makes it local to the session (for this transaction only)
    entityManager.createNativeQuery("SELECT set_config('app.current_user', :username, true)")
                 .setParameter("username", currentUser)
                 .getSingleResult();
  }
}

