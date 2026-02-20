package application.audit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test suite for AuditContext thread-local user management.
 */
class AuditContextTest {

  @AfterEach
  void cleanup() {
    AuditContext.clear();
  }

  @Test
  void testRunAsUserSetsAndClearsUser() {
    assertThat(AuditContext.getCurrentUser()).isNull();

    AuditContext.runAsUser("alice", () -> {
      assertThat(AuditContext.getCurrentUser()).isEqualTo("alice");
    });

    assertThat(AuditContext.getCurrentUser()).isNull();
  }

  @Test
  void testRunAsUserClearsEvenOnException() {
    assertThat(AuditContext.getCurrentUser()).isNull();

    try {
      AuditContext.runAsUser("bob", () -> {
        assertThat(AuditContext.getCurrentUser()).isEqualTo("bob");
        throw new RuntimeException("Test exception");
      });
    } catch (RuntimeException ignored) {
    }

    assertThat(AuditContext.getCurrentUser()).isNull();
  }

  @Test
  void testThreadLocalIsolation() {
    final Exception[] threadException = new Exception[1];

    AuditContext.runAsUser("main_thread", () -> {
      assertThat(AuditContext.getCurrentUser()).isEqualTo("main_thread");

      Thread otherThread = new Thread(() -> {
        assertThat(AuditContext.getCurrentUser()).isNull();
        AuditContext.runAsUser("other_thread", () -> {
          assertThat(AuditContext.getCurrentUser()).isEqualTo("other_thread");
        });
        assertThat(AuditContext.getCurrentUser()).isNull();
      });

      otherThread.start();
      try {
        otherThread.join();
      } catch (InterruptedException e) {
        threadException[0] = e;
        Thread.currentThread().interrupt();
      }

      assertThat(AuditContext.getCurrentUser()).isEqualTo("main_thread");
    });

    if (threadException[0] != null) {
      throw new RuntimeException(threadException[0]);
    }
    assertThat(AuditContext.getCurrentUser()).isNull();
  }

  @Test
  void testMultipleNestedCalls() {
    AuditContext.runAsUser("user1", () -> {
      assertThat(AuditContext.getCurrentUser()).isEqualTo("user1");

      // Inner call overwrites outer user
      AuditContext.runAsUser("user2", () -> {
        assertThat(AuditContext.getCurrentUser()).isEqualTo("user2");
      });

      // Back to outer user
      assertThat(AuditContext.getCurrentUser()).isEqualTo("user1");
    });
  }

  @Test
  void testGetCurrentUserReturnsNullWhenNotSet() {
    assertThat(AuditContext.getCurrentUser()).isNull();
  }

  @Test
  void testClearRemovesUser() {
    AuditContext.runAsUser("testuser", () -> {
      assertThat(AuditContext.getCurrentUser()).isEqualTo("testuser");
    });

    assertThat(AuditContext.getCurrentUser()).isNull();
    AuditContext.clear();
    assertThat(AuditContext.getCurrentUser()).isNull();
  }

  @Test
  void testDifferentUsersCreateDifferentContexts() {
    AuditContext.runAsUser("user_a", () -> {
      assertThat(AuditContext.getCurrentUser()).isEqualTo("user_a");
    });

    AuditContext.runAsUser("user_b", () -> {
      assertThat(AuditContext.getCurrentUser()).isEqualTo("user_b");
    });

    assertThat(AuditContext.getCurrentUser()).isNull();
  }
}


