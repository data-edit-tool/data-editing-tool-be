package ai.maum.data.edit.api.workspace.field.labeling;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SampleRes {

  @Field("by_project")
  private List<Sample> byProject;
  @Field("by_job")
  private List<Sample2> byJob;

  @Getter
  @Setter
  @NoArgsConstructor
  public static class Sample {
    @Field("job_id")
    private ObjectId joIid;
    @Field("job_name")
    private String jobName;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  public static class Sample2 {
    @Field("speaker_id")
    private String speakerId;
  }
}
