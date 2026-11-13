package ai.maum.data.edit.api.workspace.field;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Field;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Builder
@Schema(title = "가이드", description = "작업하기 위한 가이드 파일을 관리하는 컬렉션")
public class Guides {

  @Indexed(unique = true)
  @Field("guide_id")
  private ObjectId guideId;
  @Field("guide_file_name")
  @Schema(title = "가이드 파일 이름", example = "file.txt")
  private String guideFileName;
  @Field("guide_file_path")
  @Schema(title = "가이드 파일 저장 경로", example = "/path/to/directory")
  private String guideFilePath;
  @Field("guide_file_url")
  @Schema(title = "가이드 파일 다운로드 URL", example = "https://maum.cloudfront.net/data_edit")
  private String guideFileUrl;
  @Field("guide_file_size")
  @Schema(title = "가이드 파일 크기", example = "1280000")
  private long guideFileSize;
}
