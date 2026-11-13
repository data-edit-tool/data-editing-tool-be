package ai.maum.data.edit.api.workspace.dto.response;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(title = "워크스페이스 상세 응답")
@Document(collection = "workspace")
public class WorkspaceListResponse {

  @Field("workspace_id")
  private ObjectId workspaceId;
  @Schema(title = "고객사 ID")
  @Field("client_id")
  private String clientId;
  @Schema(title = "워크스페이스명")
  @Field("workspace_name")
  private String workspaceName;
  @Schema(title = "워크스페이스 소개")
  @Field("workspace_intro")
  private String workspaceIntro;
  @Schema(title = "작업 기간 ( 시작 )")
  @Field("start_date")
  private LocalDate startDate;
  @Schema(title = "작업 기간 ( 종료 )")
  @Field("end_date")
  private LocalDate endDate;
  @Field("created")
  private LocalDate created;
}
