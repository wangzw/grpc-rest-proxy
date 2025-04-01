package wang.zhanwei;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class ExampleApplication {
  public static ApplicationContext cxt = null;

  public static void main(String[] args) {
    cxt = new SpringApplicationBuilder(ExampleApplication.class).run(args);
  }
}
