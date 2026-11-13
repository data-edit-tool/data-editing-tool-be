package ai.maum.data.edit.api.job.dto.request;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class StartJobRequest {

  private String workspaceId;
  private String projectId;
  private String jobId;
  private List<ComplexConversationLabeling> labelings;

  @Getter
  @Setter
  @ToString
  public static class ComplexConversationLabeling {
    @Field("labeling_id")
    private ObjectId labelingId;
    @Field("speaker_id")
    private String speakerId;
    @Field("verbal")
    private List<Verbal> verbal;
    @Field("paraverbal")
    private List paraverbal;
    @Field("nonverbal")
    private List nonverbal;

    @Getter
    @Setter
    @ToString
    public static class Verbal {
      @Field("verbal_id")
      private ObjectId verbalId;
      @Field("start_time")
      private int startTime;
      @Field("end_time")
      private int endTime;
      @Field("sentence")
      private String sentence;
    }
  }
}
