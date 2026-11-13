package ai.maum.data.edit.api.project.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@JsonPropertyOrder({"mainsetInfo", "subsetInfo"})
@Schema(title = "프로젝트 가공 데이터 추출 응답")
public class ProjectExtractResponse {

  @Field("mainset_info")
  @Schema(title = "프로젝트 정보")
  private ExtractMainsetInfo mainsetInfo;
  @Field("subset_info")
  @Schema(title = "작업 정보")
  private ExtractSubsetInfo subsetInfo;

  @Data
  @JsonPropertyOrder({"mainsetId", "mainsetTopic", "mainsetPath", "speakerInfo", "cameraInfo"})
  public static class ExtractMainsetInfo {
    @Field("mainset_id")
    @Schema(title = "프로젝트 ID", example = "SET001_M004")
    private String mainsetId;
    @Field("mainset_topic")
    @Schema(title = "프로젝트 주제", example = "M004")
    private String mainsetTopic;
    @Field("mainset_path")
    @Schema(title = "프로젝트 경로", example = "/path/to/directory")
    private String mainsetPath;
    @Field("speaker_info")
    @Schema(title = "발화자 정보")
    private ExtractSpeakerInfo speakerInfo;
    @Field("camera_info")
    @Schema(title = "카메라 정보")
    private ExtractCameraInfo cameraInfo;
    @Field("file_info")
    @Schema(title = "파일 정보")
    private ExtractFileInfo fileInfo;

    @Data
    @JsonPropertyOrder({"speakerIds", "speakerProfiles", "relation"})
    public static class ExtractSpeakerInfo {
      @Field("speaker_ids")
      @Schema(title = "발화자 ID 목록", example = "[\"001\", \"002\"]")
      private List<String> speakerIds;
      @Field("speaker_profiles")
      @Schema(title = "발화자 프로필 목록")
      private List<ExtractSpeakerProfile> speakerProfiles;
      @Schema(title = "발화자 관계", example = "FRIENDS")
      private String relation;

      @Data
      @JsonPropertyOrder({"speakerId", "gender", "age"})
      public static class ExtractSpeakerProfile {
        @Field("speaker_id")
        @Schema(title = "발화자 ID", example = "001")
        private String speakerId;
        @Schema(title = "발화자 성별", example = "M")
        private String gender;
        @Schema(title = "발화자 나이", example = "30")
        private int age;
        @Schema(title = "발화자 역할", example = "SYSTEM")
        private String role;
      }
    }

    @Data
    @JsonPropertyOrder({"cameraIds", "cameraProfiles"})
    public static class ExtractCameraInfo {
      @Field("camera_ids")
      @Schema(title = "카메라 ID 목록", example = "[\"FS_MIX\", \"WS_001\", \"WS_002\"]")
      private List<String> cameraIds;
      @Field("camera_profiles")
      @Schema(title = "카메라 프로필 목록")
      private List<ExtractCameraProfile> cameraProfiles;

      @Data
      @JsonPropertyOrder({"cameraId", "cameraAngle", "targetSpeaker"})
      public static class ExtractCameraProfile {
        @Field("camera_id")
        @Schema(title = "카메라 ID", example = "FS_MIX")
        private String cameraId;
        @Field("camera_angle")
        @Schema(title = "카메라 각도", example = "FS")
        private String cameraAngle;
        @Field("target_speaker")
        @Schema(title = "카메라 대상", example = "MIX")
        private String targetSpeaker;
      }
    }

    @Data
    @JsonPropertyOrder({"videoInfo", "audioInfo"})
    public static class ExtractFileInfo {
      @Field("video_info")
      @Schema(title = "영상 파일 정보")
      private ExtractVideoFileInfo videoInfo;
      @Field("audio_info")
      @Schema(title = "음성 파일 정보")
      private ExtractAudioFileInfo audioInfo;

      @Data
      @JsonPropertyOrder({"videoFileIds", "videoFiles"})
      public static class ExtractVideoFileInfo {
        @Field("video_file_ids")
        @Schema(title = "영상 파일명 목록", example = "[\"SET004_M003_S001_F_20_FS.mp4\", \"SET004_M003_S001_F_20_CS.mp4\"]")
        private List<String> videoFileIds;
        @Field("video_files")
        @Schema(title = "영상 파일 정보 목록")
        private List<ExtractDetailVideoFileInfo> videoFiles;

        @Data
        @JsonPropertyOrder({"videoId", "videoDuration", "videoFormat", "fps", "resolution"})
        public static class ExtractDetailVideoFileInfo {
          @Field("video_id")
          @Schema(title = "영상 파일명", example = "SET004_M003_S001_F_20_FS.mp4")
          private String videoId;
          @Field("video_duration")
          @Schema(title = "영상 파일 길이", example = "00:05:00.000")
          private String videoDuration;
          @Field("video_format")
          @Schema(title = "영상 파일 형식", example = "mp4")
          private String videoFormat;
          @Schema(title = "영상 파일 FPS", example = "30.00")
          private String fps;
          @Schema(title = "영상 파일 해상도", example = "1920x1080")
          private String resolution;
        }
      }

      @Data
      @JsonPropertyOrder({"audioFileIds", "audioFiles"})
      public static class ExtractAudioFileInfo {
        @Field("audio_file_ids")
        @Schema(title = "음성 파일명 목록", example = "[\"SET002_M002_S001_001_F_30_FRIEND_Pin.wav\", \"SET002_M002_S001_002_M_40_FRIEND_Pin.wav\"]")
        private List<String> audioFileIds;
        @Field("audio_files")
        @Schema(title = "음성 파일 정보 목록")
        private List<ExtractDetailAudioFileInfo> audioFiles;

        @Data
        @JsonPropertyOrder({"audioId", "audioDuration", "audioFormat", "audio_sample_rate", "audio_channel"})
        public static class ExtractDetailAudioFileInfo {
          @Field("audio_id")
          @Schema(title = "음성 파일명", example = "SET002_M002_S001_001_F_30_FRIEND_Pin.wav")
          private String audioId;
          @Field("audio_duration")
          @Schema(title = "음성 파일 길이", example = "00:05:00.000")
          private String audioDuration;
          @Field("audio_format")
          @Schema(title = "음성 파일 형식", example = "wav")
          private String audioFormat;
          @Field("audio_sample_rate")
          @Schema(title = "음성 파일 샘플레이트", example = "22050")
          private String audioSampleRate;
          @Field("audio_channel")
          @Schema(title = "음성 파일 채널", example = "stereo")
          private String audioChannel;
        }
      }
    }
  }

  @Data
  @JsonPropertyOrder({"sets"})
  public static class ExtractSubsetInfo {
    @Schema(title = "작업 대화 정보 목록")
    List<ExtractSet> sets;

    @Data
    @JsonPropertyOrder({"subsetId", "subsetStatus", "videoNameSet", "audioNameSet", "dialogue"})
    public static class ExtractSet {
      @Field("subset_id")
      @Schema(title = "작업 ID", example = "SET001_M004_S001")
      private String subsetId;
      @Field("subset_status")
      @Schema(title = "작업 상태", example = "CM")
      private String subsetStatus;
      @Field("video_name_set")
      @Schema(title = "영상 파일명 목록", example = "[\"SET004_M003_S001_F_20_FS.mp4\", \"SET004_M003_S001_F_20_CS.mp4\"]")
      private List<String> videoNameSet;
      @Field("audio_name_set")
      @Schema(title = "음성 파일명 목록", example = "[\"SET002_M002_S001_001_F_30_FRIEND_Pin.wav\", \"SET002_M002_S001_002_M_40_FRIEND_Pin.wav\"]")
      private List<String> audioNameSet;
      @Schema(title = "대화 정보")
      private ExtractDialogue dialogue;
      @JsonIgnore
      @Field("job_id")
      private ObjectId jobId;

      @Data
      @JsonPropertyOrder({"section", "information"})
      public static class ExtractDialogue {
        @Schema(title = "구간 정보")
        private List<ExtractSection> section;
        @Schema(title = "대화 정보")
        private ExtractInfo information;

        @Data
        @JsonPropertyOrder({"sectionId", "speakerId", "vocalOrBody", "startTime", "endtime"})
        public static class ExtractSection {
          @Field("section_id")
          @Schema(title = "구간 ID", example = "64a523400000000000e76889")
          private ObjectId sectionId;
          @Field("speaker_id")
          @Schema(title = "발화자 ID", example = "001")
          private String speakerId;
          @Field("vocal_or_body")
          @Schema(title = "정보 소스의 종류", example = "vocal")
          private String vocalOrBody;
          @Field("start_time")
          @Schema(title = "구간 시작 시간", example = "00:03:13.1")
          private String startTime;
          @Field("end_time")
          @Schema(title = "구간 종료 시간", example = "00:05:00.5")
          private String endTime;
        }

        @Data
        @JsonPropertyOrder({"verbal", "paraVerbal", "nonVerbal", "overall"})
        public static class ExtractInfo {
          private List<ExtractInfoVerbal> verbal;
          @JsonProperty("para-verbal")
          @Field("para-verbal")
          private List<ExtractInfoParaVerbal> paraVerbal;
          @JsonProperty("non-verbal")
          @Field("non-verbal")
          private List<ExtractInfoNonVerbal> nonVerbal;
          private List<ExtractInfoOverall> overall;

          @Data
          @JsonPropertyOrder({"sectionId", "channel", "sentence", "sentiment", "emotion", "fcIntent", "bcIntent"})
          public static class ExtractInfoVerbal {
            @Field("section_id")
            @Schema(title = "구간 ID", example = "64a523400000000000e76889")
            private ObjectId sectionId;
            @Schema(title = "대화 채널 정보", example = "front")
            private String channel;
            @Schema(title = "발화 문장", example = "안녕하세요")
            private String sentence;
            @Schema(title = "발화 감성", example = "positive")
            private String sentiment;
            @Schema(title = "발화 감정", example = "행복")
            private String emotion;
            @Field("bc_intent")
            private String bcIntent;
            @Field("fc_intent")
            private String fcIntent;
          }

          @Data
          @JsonPropertyOrder({"sectionId", "speed", "loudness", "pitch", "paraVerbalFunction"})
          public static class ExtractInfoParaVerbal {
            @Field("section_id")
            @Schema(title = "구간 ID", example = "64a523400000000000e76889")
            private ObjectId sectionId;
            @Schema(title = "발화 속도", example = "3.548")
            private String speed;
            @Schema(title = "발화 크기", example = "[\"3.3, 1.1, 2.2\"]")
            private List<String> loudness;
            @Schema(title = "발화 높낮이", example = "[\"2.2, 3.3, 1.1\"]")
            private List<String> pitch;
            @JsonProperty("para-verbal_ft")
            @Field("para-verbal_ft")
            private List<ExtractInfoParaVerbalFunction> paraVerbalFunction;
            @JsonIgnore
            @Field("speaker_id")
            private String speakerId;
            @JsonIgnore
            @Field("org_start_time")
            private int orgStartTime;
            @JsonIgnore
            @Field("org_end_time")
            private int orgEndTime;

            @Data
            @JsonPropertyOrder({"sectionId", "function"})
            public static class ExtractInfoParaVerbalFunction {
              @Field("section_id")
              @Schema(title = "구간 ID", example = "64a523400000000000e76889")
              private ObjectId sectionId;
              @Schema(title = "준언어 기능", example = "정서 표현")
              private String function;
            }
          }

          @Data
          @JsonPropertyOrder({"sectionId", "cameraAngle", "body", "nonVerbalOnly", "gaze"})
          public static class ExtractInfoNonVerbal {
            @Field("section_id")
            @Schema(title = "구간 ID", example = "64a523400000000000e76889")
            private ObjectId sectionId;
            @Field("camera_angle")
            @Schema(title = "카메라 각도", example = "CS")
            private String cameraAngle;
            @Schema(title = "비언어 정보")
            private ExtractInfoNonVerbalBody body;
            @JsonProperty("non-verbal_only")
            @Field("non-verbal_only")
            @Schema(title = "비언어 단독출현 시 정보")
            private ExtractInfoNonVerbalOnly nonVerbalOnly;
            @Schema(title = "시선", example = "front")
            private String gaze;

            @Data
            @JsonPropertyOrder({"gesture", "function", "emotion"})
            public static class ExtractInfoNonVerbalBody {
              @Schema(title = "제스처 정보")
              private ExtractInfoNonVerbalBodyGesture gesture;
              @Schema(title = "비언어 기능", example = "호응적")
              private String function;
              @Schema(title = "표정 감정", example = "행복")
              private String emotion;

              @Data
              @JsonPropertyOrder({"subject", "action", "object"})
              public static class ExtractInfoNonVerbalBodyGesture {
                @Schema(title = "제스처 주체", example = "한손")
                private String subject;
                @Schema(title = "제스처 행위 정보")
                private ExtractInfoNonVerbalBodyGestureAction action;
                @Schema(title = "제스처 객체", example = "손")
                private String object;

                @Data
                @JsonPropertyOrder({"main", "description"})
                public static class ExtractInfoNonVerbalBodyGestureAction {
                  @Schema(title = "제스처 행위", example = "만듦")
                  private String main;
                  @Schema(title = "제스처 행위 자연어 기술", example = "손을 맞댐")
                  private String description;
                }
              }
            }

            @Data
            @JsonPropertyOrder({"intent", "affectiveState"})
            public static class ExtractInfoNonVerbalOnly {
              @Schema(title = "비언어 단독출현 시 의도", example = "동의")
              private String intent;
              @Field("affective_state")
              @Schema(title = "비언어 단독출현 시 정서적 상태", example = "기쁨")
              private String affectiveState;
            }
          }

          @Data
          @JsonPropertyOrder({"sectionId", "affectiveState", "vad"})
          public static class ExtractInfoOverall {
            @Field("section_id")
            @Schema(title = "구간 ID", example = "64a523400000000000e76889")
            private ObjectId sectionId;
            @Field("affective_state")
            @Schema(title = "정서적 상태", example = "기쁨")
            private String affectiveState;
            @Schema(title = "VAD 정보")
            private ExtractInfoOverallVad vad;

            @Data
            @JsonPropertyOrder({"vp", "vpn"})
            public static class ExtractInfoOverallVad {
              @Schema(title = "VP 정보")
              private ExtractInfoOverallVadInfo vp;
              @Schema(title = "VPN 정보")
              private ExtractInfoOverallVadInfo vpn;

              @Data
              @JsonPropertyOrder({"valence", "arousal", "dominance"})
              public static class ExtractInfoOverallVadInfo {
                @Schema(title = "Valence 정보", example = "2")
                private int valence;
                @Schema(title = "Arousal 정보", example = "1")
                private int arousal;
                @Schema(title = "Dominance 정보", example = "3")
                private int dominance;
              }
            }
          }
        }
      }
    }
  }
}
