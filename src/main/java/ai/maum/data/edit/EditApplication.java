package ai.maum.data.edit;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

import lombok.RequiredArgsConstructor;

@SpringBootApplication
@EnableScheduling
@EnableAspectJAutoProxy
@ConfigurationPropertiesScan
@RequiredArgsConstructor
public class EditApplication implements CommandLineRunner, DisposableBean {

  public static void main(String[] args) {
    SpringApplication app = new SpringApplication(EditApplication.class);
    app.addListeners(new ApplicationPidFileWriter());
    app.run(args);
  }

  @Override
  public void destroy() {
  }

  @Override
  public void run(String... args) {
  }
}
