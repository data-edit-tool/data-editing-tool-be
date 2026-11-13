package ai.maum.data.edit.api.work.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Schema(title = "가공 상태 수정 요청")
public class UpdateStatusRequest {

  private String workspaceId;
  private String projectId;
  private String jobId;
  private String workId;

  @Schema(title = "현재 가공 상태", description = "업데이트될 상태 코드", example = "PR")
  private String workStatus;
  @Schema(title = "이전 가공 상태", description = "업데이트 전 상태 코드", example = "IM")
  private String nextWorkStatus;
  @Schema(title = "(작업불가||반려) 사유")
  private String reason;
}
