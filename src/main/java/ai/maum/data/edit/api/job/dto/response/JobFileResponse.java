package ai.maum.data.edit.api.job.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(title = "작업을 위한 원천 데이터 파일 정보")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JobFileResponse {

  @Field("file_id")
  private ObjectId fileId;
  @Schema(title = "파일 원본명")
  @Field("file_name")
  private String fileName;
  @Schema(title = "파일 경로", example = "/s3/edit/...")
  @Field("file_path")
  private String filePath;
  @Schema(title = "파일 url", example = "https://maum.cloudfront.net/data_edit")
  @Field("file_url")
  private String fileUrl;
  @Schema(title = "파일 타입(video, audio 등)")
  @Field("file_type")
  private String fileType;
}
