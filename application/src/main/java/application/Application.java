package application;

import application.repo.WidgetRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {
  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @Bean
  CommandLineRunner logWidgetCount(WidgetRepository repository) {
    return args -> {
      long count = repository.count();
      System.out.println("Widgets in database: " + count);
    };
  }
}

