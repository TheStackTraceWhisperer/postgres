package application;

import application.repo.WidgetRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class ApplicationTests {

  @Autowired
  private ApplicationContext context;

  @Autowired
  private WidgetRepository widgetRepository;

  @Test
  void contextLoads() {
    assertNotNull(context);
  }

  @Test
  void testApplicationBeanExists() {
    Application application = context.getBean(Application.class);
    assertNotNull(application);
  }

  @Test
  void testWidgetRepositoryBeanExists() {
    assertNotNull(widgetRepository);
  }

  @Test
  void testCommandLineRunnerBeanExists() {
    CommandLineRunner runner = context.getBean(CommandLineRunner.class);
    assertNotNull(runner);
  }

  @Test
  void testCommandLineRunnerExecutes() {
    CommandLineRunner runner = context.getBean(CommandLineRunner.class);

    // Should execute without throwing exceptions
    assertDoesNotThrow(() -> runner.run());
  }

  @Test
  void testWidgetRepositoryIsAccessible() {
    long count = widgetRepository.count();
    assertTrue(count >= 0);
  }

  @Test
  void testMainMethodExists() throws NoSuchMethodException {
    assertNotNull(Application.class.getMethod("main", String[].class));
  }
}

