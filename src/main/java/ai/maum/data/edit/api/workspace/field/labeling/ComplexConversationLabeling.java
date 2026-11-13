package ai.maum.data.edit.api.workspace.field.labeling;

import com.fasterxml.jackson.annotation.JsonInclude;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ComplexConversationLabeling extends Labeling {

  @Field("speaker_id")
  private String speakerId;
  @Field("verbal")
  private List<Verbal> verbal;
  @Field("paraverbal")
  private List<Paraverbal> paraverbal;
  @Field("nonverbal")
  private List<Nonverbal> nonverbal;

  @Getter
  @Setter
  @SuperBuilder
  @NoArgsConstructor
  public static class SEtime {
    @Field("start_time")
    @Schema(title = "시작 지점")
    private int startTime;
    @Field("end_time")
    @Schema(title = "종료 지점")
    private int endTime;
  }

  @Getter
  @Setter
  @SuperBuilder
  @NoArgsConstructor
  @ToString(callSuper = true)
  public static class Verbal extends SEtime { // = Verbal
    @Field("verbal_id")
    private ObjectId verbalId;
    @Field("sentence")
    private String sentence;
    @Field("channel")
    private String channel;
    @Field("sentiment")
    private String sentiment;
    @Field("fc_intent")
    private String fcIntent;
    @Field("bc_intent")
    private String bcIntent;
    @Field("emotion")
    private String emotion;
    @Field("affective_state")
    private String affectiveState; // nonverbal에도 속함
    @Field("verbal_vad")
    private VAD verbalVad;
    @Field("overall_vad")
    private VAD overallVad;

    @Getter
    @Setter
    @SuperBuilder
    @NoArgsConstructor
    public static class VAD {
      @Field("valence")
      private int valence;
      @Field("arousal")
      private int arousal;
      @Field("dominance")
      private int dominance;
    }
  }

  @Getter
  @Setter
  @SuperBuilder
  @NoArgsConstructor
  public static class Paraverbal extends SEtime {
    @Field("paraverbal_id")
    private ObjectId paraverbalId;
    @Field("func")
    private String func;
  }

  @Getter
  @Setter
  @SuperBuilder
  @NoArgsConstructor
  public static class Nonverbal extends SEtime {
    @Field("nonverbal_id")
    private ObjectId nonverbalId;
    @Field("angle")
    private String angle;
    @Field("subject")
    private String subject;
    @Field("action")
    private String action;
    @Field("action_str")
    private String actionStr;
    @Field("obj")
    private String obj;
    @Field("intent")
    private String intent;
    @Field("func")
    private String func;
    @Field("emotion")
    private String emotion;
    @Field("affective_state")
    private String affectiveState;
  }
}
