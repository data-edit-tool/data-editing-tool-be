package ai.maum.data.edit.api.workspace.field.labeling;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Field;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@ToString
@SuperBuilder
@NoArgsConstructor
@Schema(title = "가공 데이터", description = "가공된 데이터(라벨링 데이터)")
public class Labeling {

  @Indexed(unique = true)
  @Field("labeling_id")
  private ObjectId labelingId;

  @Setter
  @Field("meta")
  private Meta meta;

  @Getter
  @Setter
  @Builder(toBuilder = true)
  public static class Meta {
    @Field("work_id")
    private ObjectId workId;
    @Field("work_status")
    private String workStatus;
    @Field("step")
    private Integer step;
    @Field("check_step")
    private Integer checkStep;
    @Field("job_id")
    private ObjectId jobId;
    @Field("worker_id")
    private ObjectId workerId;
    @Field("checker_id")
    private ObjectId checkerId;
    @Field("has_rejected")
    private Boolean hasRejected;
  }
}
