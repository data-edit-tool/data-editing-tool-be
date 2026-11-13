package ai.maum.data.edit.api.job.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

import ai.maum.data.edit.api.job.vo.VoiceProperties;
import ai.maum.data.edit.api.project.vo.SpeakerInfo;
import ai.maum.data.edit.api.workspace.field.labeling.ComplexConversation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ViewerJobDetailResponse {

  private SpeakerInfo speaker;
  @Field("verbals")
  private List<Verbal> verbals;
  @Field("paraverbals")
  private List<Paraverbal> paraverbals;
  @Field("nonverbals")
  private List<Nonverbal> nonverbals;
  private List<VoiceProperties.Measurement> voiceMeasurements;

  @Getter
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class Verbal extends Label {
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
    private String affectiveState;
    @Field("verbal_vad")
    private ComplexConversation.VAD verbalVad;
    @Field("overall_vad")
    private ComplexConversation.VAD overallVad;
  }

  @Getter
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class Paraverbal extends Label {
    @Field("paraverbal_id")
    private ObjectId paraverbalId;
    @Field("func")
    private String func;
  }

  @Getter
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class Nonverbal extends Label {
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

  @Getter
  public static class Label {
    @Field("start_time")
    @Schema(title = "시작 지점")
    private Integer startTime;
    @Field("end_time")
    @Schema(title = "종료 지점")
    private Integer endTime;

    @Field("labeling_id")
    private ObjectId labelingId;
  }
}
