package application.audit;

import java.util.Deque;
import java.util.LinkedList;

/**
 * Thread-safe context holder for audit information.
 * Allows the application layer to set the current user for database-level audit tracking.
 * Supports nested calls via a stack-based approach.
 */
public class AuditContext {
  private static final ThreadLocal<Deque<String>> USER_STACK = ThreadLocal.withInitial(LinkedList::new);

  /**
   * Execute a runnable as a specific user.
   * The user will be available to the database trigger via the 'app.current_user' session variable.
   * Supports nested calls - inner user is popped after action completes.
   *
   * @param username the user to act as
   * @param action   the action to execute
   */
  public static void runAsUser(String username, Runnable action) {
    Deque<String> stack = USER_STACK.get();
    stack.push(username);
    try {
      action.run();
    } finally {
      if (!stack.isEmpty()) {
        stack.pop();
      }
    }
  }

  /**
   * Get the current user from the thread-local context.
   *
   * @return the current user, or null if not set
   */
  public static String getCurrentUser() {
    Deque<String> stack = USER_STACK.get();
    return stack.isEmpty() ? null : stack.peek();
  }

  /**
   * Clear all users from the context.
   */
  public static void clear() {
    USER_STACK.remove();
  }
}

