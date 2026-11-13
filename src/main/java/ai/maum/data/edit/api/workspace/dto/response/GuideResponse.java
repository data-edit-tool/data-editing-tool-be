package ai.maum.data.edit.api.workspace.dto.response;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(title = "가이드 파일 url")
@Document(collection = "workspace")
public class GuideResponse {

  @Field("guides")
  private List<GuideUrl> guides;

  @Getter
  @Setter
  public static class GuideUrl {
    @Field("guide_file_url")
    @Schema(title = "가이드 파일 다운로드 URL", example = "https://maum.cloudfront.net/data_edit")
    private String guideFileUrl;
  }
}
