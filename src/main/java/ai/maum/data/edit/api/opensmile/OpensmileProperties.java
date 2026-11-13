package ai.maum.data.edit.api.opensmile;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "service.opensmile")
public class OpensmileProperties {

  private String containerName;
  private String rootPath;
  private String csvPath;
  private String execBin;
  private String configPath;
}
