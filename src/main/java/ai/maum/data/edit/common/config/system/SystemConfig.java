package ai.maum.data.edit.common.config.system;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SystemConfig implements ApplicationContextAware {
  private static ApplicationContext context;

  public static ApplicationContext getContext() {
    return context;
  }

  @Override
  public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
    context = applicationContext;
  }
}
