package ai.maum.data.edit.api.work.dto.request;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString // FIXME 데이터 오류 수정 후 @Override해 놓은 로그로 변경하기
public class ComplexConversationLabelingRequest {

  private String labelingId;
  private String speakerId;
  private List<Verbal> verbal;
  private List<Paraverbal> paraverbal;
  private List<Nonverbal> nonverbal;

  @Getter
  @Setter
  @ToString
  public static class SEtime {
    private int startTime;
    private int endTime;
  }

  @Getter
  @Setter
  @ToString
  public static class Verbal extends SEtime {
    private String verbalId;
    private String sentence;
    private String channel;
    private String sentiment;
    private String fcIntent;
    private String bcIntent;
    private String emotion;
    private String affectiveState;
    private VAD verbalVad;
    private VAD overallVad;

    @Getter
    @Setter
    @ToString
    public static class VAD {
      private int valence;
      private int arousal;
      private int dominance;
    }
  }

  @Getter
  @Setter
  @ToString
  public static class Paraverbal extends SEtime {
    private String paraverbalId;
    private String func;
  }

  @Getter
  @Setter
  @ToString
  public static class Nonverbal extends SEtime {
    private String nonverbalId;
    private String angle;
    private String subject;
    private String action;
    private String actionStr;
    private String obj;
    private String intent;
    private String func;
    private String emotion;
    private String affectiveState;
  }
}
