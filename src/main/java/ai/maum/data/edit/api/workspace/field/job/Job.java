package ai.maum.data.edit.api.workspace.field.job;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

import ai.maum.data.edit.api.workspace.field.work.Work;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Builder
@Schema(title = "데이터 가공 단위", description = "프로젝트 하위에 생성되는 하나의 가공 단위")
@Document("project")
public class Job {

  @Indexed(unique = true)
  @Field("job_id")
  private ObjectId jobId;
  @Field("job_name")
  private String jobName;
  @Field("job_files")
  @Schema(title = "원천데이터 파일", description = "데이터 가공을 위한 원천 데이터 파일")
  private List<JobFile> jobFiles;
  @Field("works")
  @Schema(title = "가공 작업 단위", description = "데이터를 가공하기 위한 가공 작업 단위")
  private List<Work> works;
}
