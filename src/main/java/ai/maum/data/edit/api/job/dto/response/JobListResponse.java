package ai.maum.data.edit.api.job.dto.response;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title = "작업 목록 응답")
public class JobListResponse {

  @Field("target_idx")
  @Schema(title = "현재 확인해야할 작업 인덱스, null의 경우 접근할 수 있는 작업 없음", example = "0")
  private Integer targetIdx;
  @Schema(title = "작업 목록")
  private List<Info> list;

  @Data
  public static class Info {
    @Field("job_id")
    @Schema(title = "작업 ID", example = "507f1f77bcf86cd799439011")
    private ObjectId jobId;
    @Field("job_name")
    @Schema(title = "작업명", example = "S001")
    private String jobName;
    @Field("is_access")
    @Schema(title = "접근 가능 유무", example = "true")
    private Boolean isAccess;
    @Field("work_status")
    @Schema(title = "작업 상태", example = "RD")
    private String workStatus;
  }
}
