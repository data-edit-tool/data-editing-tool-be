package ai.maum.data.edit.api.workspace.field.project;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

import ai.maum.data.edit.api.workspace.field.job.Job;
import ai.maum.data.edit.common.domain.BaseField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Document(collection = "project")
public class Project extends BaseField {

  @Indexed(unique = true)
  @Field("project_id")
  private ObjectId projectId;
  @Field("workspace_id")
  private ObjectId workspaceId;
  @Field("project_name")
  @Schema(title = "프로젝트명")
  private String projectName;
  @Field("topic")
  @Schema(title = "프로젝트 주제 코드")
  private String topic;
  @Field("post_status")
  @Schema(title = "게시 상태")
  private String postStatus;
  @Field("data_type")
  @Schema(title = "데이터 타입", description = "가공할 원천 데이터 타입")
  private Type dataType;
  @Field("tool_type")
  @Schema(title = "툴 타입", description = "가공 툴 타입")
  private Type toolType;
  @Field("client_ctg_id")
  @Schema(title = "카테고리 ID", description = "레이블링 카테고리 목록 ID")
  private ObjectId clientCtgId;
  @Field("is_multiple_work")
  @Schema(title = "다중 작업 가능 여부")
  private boolean isMultipleWork;
  @Field("jobs")
  @Schema(title = "데이터 가공 단위들", description = "프로젝트 하위에 생성되는 하나의 가공 단위")
  private List<Job> jobs;
  @Field("is_deleted")
  @Schema(title = "삭제 여부")
  private boolean isDeleted;

  @Getter
  @ToString
  @Builder
  public static class Type {
    @Field("code")
    private String code;
    @Field("name")
    private String name;
  }
}
