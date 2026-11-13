package ai.maum.data.edit.api.workspace.field.work;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

import ai.maum.data.edit.api.workspace.field.labeling.Labeling;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(title = "가공 작업 단위", description = "데이터를 가공하기 위한 가공 작업 단위")
public class Work {

  @Indexed(unique = true)
  @Field("work_id")
  private ObjectId workId;
  @Field("work_status")
  @Schema(title = "가공 상태", description = "가공 진행 중인 상태값 표시", example = "RD")
  private String workStatus;
  @Field("step")
  @Schema(title = "가공 단계", description = "가공 진행 중인 상태값 표시(1:verbal/2:paraverbal/3:nonverbal)", example = "1")
  private int step;
  @Field("worker_id")
  @Schema(title = "작업자 user_id", example = "649eacfc0000000000b14232")
  private String workerId;
  @Field("checker_id")
  @Schema(title = "검수자 user_id", example = "649eacfc0000000000b14232")
  private String checkerId;
  @Field("labelings")
  @Schema(title = "가공 데이터", description = "가공된 데이터(라벨링 데이터)")
  private List<Labeling> labelings;
  @Field("updated")
  private LocalDateTime updated;
}
