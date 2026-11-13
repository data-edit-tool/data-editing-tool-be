package ai.maum.data.edit.api.workspace.field.job;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Field;

import ai.maum.data.edit.common.enums.FileMeta;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
@Builder
@Schema(title = "원천 데이터 파일", description = "데이터 가공을 위한 원천 데이터 파일")
public class JobFile {

  @Indexed(unique = true)
  @Field("file_id")
  private ObjectId fileId;
  @Field("file_name")
  @Indexed(unique = true)
  @Schema(title = "파일명", description = "파일명")
  private String fileName;
  @Field("file_path")
  @Schema(title = "파일 경로", description = "파일 경로", example = "/home/minds/..")
  private String filePath;
  @Field("file_url")
  @Schema(title = "파일 다운로드 URL",
      description = "파일 다운로드 URL",
      example = "https://maum.cloudfront.net/data_edit/workspace/507f1f77bcf86cd799439011/project/507f1f77bcf86cd799439012/job/507f1f77bcf86cd799439013/67575137-2e20-4f62-b3cc-c10908c6aa73.mp4")
  private String fileUrl;
  @Field("file_type")
  @Schema(title = "파일 타입", description = "파일 타입(image, text etc)")
  private String fileType;
  /**
   * @see FileMeta
   */
  @Field("file_ext")
  private String fileExt;
  @Field("file_size")
  private Long fileSize;
  @Setter
  @Field("file_info")
  @Schema(title = "파일 정보", description = "파일 정보")
  private FileInfo fileInfo;
  @Field("file_status")
  @Schema(title = "파일 상태", description = "파일 상태")
  private String fileStatus;

  @Getter
  @ToString
  @Builder
  public static class FileInfo {
    private String duration;
    private String format;
    private Integer fps;
    private Integer width;
    private Integer height;
    @Field("sample_rate")
    private String sampleRate;
    private String channel;
    private String angle;
  }
}
