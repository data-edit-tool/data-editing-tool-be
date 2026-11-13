package ai.maum.data.edit.api.job.service;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ai.maum.data.edit.api.csv.CsvRepository;
import ai.maum.data.edit.api.engine.EngineRepository;
import ai.maum.data.edit.api.engine.vo.EngineLabelingInfo;
import ai.maum.data.edit.api.job.dto.request.StartJobRequest;
import ai.maum.data.edit.api.job.dto.response.JobListResponse;
import ai.maum.data.edit.api.job.dto.response.ViewerJobDetailResponse;
import ai.maum.data.edit.api.job.repository.JobRepository;
import ai.maum.data.edit.api.job.vo.VoiceProperties;
import ai.maum.data.edit.api.project.repository.ProjectRepository;
import ai.maum.data.edit.api.project.vo.SpeakerInfo;
import ai.maum.data.edit.api.user.enums.UserType;
import ai.maum.data.edit.api.work.WorkHistoryEntity;
import ai.maum.data.edit.api.work.repository.WorkHistoryRepository;
import ai.maum.data.edit.api.work.repository.WorkRepository;
import ai.maum.data.edit.api.workspace.field.labeling.ComplexConversation;
import ai.maum.data.edit.api.workspace.field.labeling.Labeling;
import ai.maum.data.edit.api.workspace.field.project.Project;
import ai.maum.data.edit.common.config.security.UserInfo;
import ai.maum.data.edit.common.enums.ResponseCode;
import ai.maum.data.edit.common.enums.ToolTypeMeta;
import ai.maum.data.edit.common.enums.WorkStatus;
import ai.maum.data.edit.common.exception.BaseException;
import ai.maum.data.edit.common.util.UserUtil;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JobService {

  private final JobRepository jobRepository;
  private final EngineRepository engineRepository;
  private final ProjectRepository projectRepository;
  private final WorkRepository workRepository;
  private final WorkHistoryRepository workHistoryRepository;
  private final JobComplexConversationService jobComplexConversationService;
  private final CsvRepository csvRepository;

  public Labeling findLabeling(ObjectId projectId, int step, ObjectId jobId) {
    if (step < 0 || step > 3) {
      step = 0;
    }

    UserInfo user = UserUtil.getUser();
    Labeling labeling = null;
    String toolCode = projectRepository.getToolTypeByProjectId(projectId);// 툴 코드 가져오기

    if (toolCode.equals(ToolTypeMeta.T_TYPE_CC.getToolCode())) { // CC:복합대화
      labeling = findLabelingForT_TYPE_CC(projectId, jobId, user.getUserId(), user.getType(), step, toolCode);
    }

    return labeling;
  }

  public JobListResponse getJobList(ObjectId projectId) {
    UserInfo user = UserUtil.getUser();
    return jobRepository.findJobList(projectId, user.getUserId(), user.getType().getCode());
  }

  public ComplexConversation startJob(StartJobRequest request) {
    ObjectId userId = UserUtil.getUserId();
    ObjectId projectId = new ObjectId(request.getProjectId());
    ObjectId jobId = new ObjectId(request.getJobId());

    ComplexConversation labeling = null;
    if (workRepository.getWorksSize(projectId, jobId) > 0) { // 이미 작업 중인 게 있는 지 확인.
      // worker_id, work_status 변경
      Map<String, Object> where = new HashMap<>();
      where.put("project_id", projectId);

      Map<String, Object> arrayFilters = new HashMap<>();
      arrayFilters.put("job.job_id", jobId);

      Map<String, Object> set = new HashMap<>();
      set.put("jobs.$[job].works.0.worker_id", userId);
      set.put("jobs.$[job].works.0.work_status", WorkStatus.AS.getCode());

      boolean isUpdated = workRepository.updateOneWithFilters(Project.class, where, arrayFilters, set, null);

      if (isUpdated) {
        labeling = jobRepository.findLabelingByJobIdAndUserId(jobId, userId, 1);
        labeling.setSpeakers(projectRepository.getSpeakers(projectId));
        boolean hasRejected =
            !workHistoryRepository.findStatusReasonList(labeling.getMeta().getWorkId(), WorkStatus.RJ.getCode())
                .isEmpty();
        labeling.getMeta().setHasRejected(hasRejected);

        workHistoryRepository.insert(
            WorkHistoryEntity.builder()
                .userId(userId)
                .preWorkStatus(WorkStatus.RD.getCode())
                .workStatus(WorkStatus.AS.getCode())
                .workId(labeling.getMeta().getWorkId())
                .build()
        );
      }
    } else {
      Map<String, Object> where = new HashMap<>();
      where.put("project_id", projectId);

      Map<String, Object> value = new HashMap<>();
      value.put("work_id", new ObjectId());
      value.put("work_status", WorkStatus.AS.getCode());
      value.put("step", 1);
      value.put("worker_id", userId);

      // setLabelings
      String toolCode = projectRepository.getToolTypeByProjectId(projectId);// 툴 코드 가져오기
      List<StartJobRequest.ComplexConversationLabeling> labelings = new ArrayList<>();
      if (toolCode.equals(ToolTypeMeta.T_TYPE_CC.getToolCode())) { // CC:복합대화
        labelings = jobComplexConversationService.setVerbal(request);
      }
      value.put("labelings", labelings);

      // works 생성
      Map<String, Object> push = new HashMap<>();
      push.put("jobs.$[job].works", value);

      Map<String, Object> arrayFilters = new HashMap<>();
      arrayFilters.put("job.job_id", jobId);

      boolean isPushed = workRepository.updateOneWithFilters(Project.class, where, arrayFilters, null, push);

      if (isPushed) {
        labeling = jobRepository.findLabelingByJobIdAndUserId(jobId, userId, 0);
        labeling.setSpeakers(projectRepository.getSpeakers(projectId));

        workHistoryRepository.insert(
            WorkHistoryEntity.builder()
                .userId(userId)
                .preWorkStatus(null)
                .workStatus(WorkStatus.AS.getCode())
                .workId(labeling.getMeta().getWorkId())
                .build()
        );
      }
    }

    return labeling;
  }

  public List<ViewerJobDetailResponse> getViewerJobDetail(ObjectId projectId, ObjectId jobId) {
    if (UserUtil.getUserType() != UserType.VIEWER) {
      throw BaseException.of(ResponseCode.JOB_VIEWER_ONLY_ACCESS);
    }

    Map<String, List<VoiceProperties.Measurement>> voiceMeasurements = csvRepository.findVoicePropertiesByJobId(jobId)
        .stream()
        .collect(Collectors.toMap(
            VoiceProperties::getSpeakerId,
            VoiceProperties::getMeasurements
        ));
    List<ViewerJobDetailResponse> jobResponses = jobRepository.findLabelingForViewer(projectId, jobId);

    // Speaker 정보를 Map으로 조회
    Map<String, String> speakers = projectRepository.getSpeakers(projectId)
        .stream()
        .collect(Collectors.toMap(
            SpeakerInfo::getSpeakerId,
            SpeakerInfo::getThumbnailFileUrl
        ));

    jobResponses.forEach(response -> {
      if (response.getSpeaker() != null && response.getSpeaker().getSpeakerId() != null) {
        String speakerId = response.getSpeaker().getSpeakerId();
        String thumbnailUrl = speakers.get(speakerId);

        if (thumbnailUrl != null) {
          response.getSpeaker().setThumbnailFileUrl(thumbnailUrl);
        }

        // 모든 발화자의 음성 측정 데이터가 섞인 'MIX' 데이터만 존재할 경우 공통 적용, 그 외에는 개별 발화자 데이터 적용
        if (voiceMeasurements.size() == 1 && voiceMeasurements.containsKey("MIX")) {
          response.setVoiceMeasurements(voiceMeasurements.get("MIX"));
        } else if (voiceMeasurements.containsKey(speakerId)) {
          response.setVoiceMeasurements(voiceMeasurements.get(speakerId));
        }
      }
    });

    return jobResponses;
  }

  private ComplexConversation findLabelingForT_TYPE_CC(ObjectId projectId, ObjectId jobId, ObjectId userId,
      UserType userType, int step, String toolCode) {
    ComplexConversation labeling;

    if (userType == UserType.WORKER) {
      // job_id에 해당하는 login user 본인 또는 다른 사람이 작업한 것이 있는 지 확인.
      labeling = jobRepository.findLabelingByJobIdAndUserId(jobId, userId, step);

      if (labeling == null || labeling.getMeta() == null || labeling.getMeta().getWorkId() == null) {
        labeling = setLabeling(labeling, toolCode, ToolTypeMeta.T_TYPE_CC, projectId, jobId);
        labeling.getMeta().setWorkStatus(WorkStatus.RD.getCode());
      }

      labeling.setMeta(labeling.getMeta().toBuilder() // 반려 여부
          .hasRejected(
              !workHistoryRepository.findStatusReasonList(labeling.getMeta().getWorkId(), WorkStatus.RJ.getCode())
                  .isEmpty())
          .build());
    } else if (userType == UserType.CHECKER) {
      // job_id에 해당하는 login user 본인 또는 다른 사람이 작업한 것이 있는 지 확인.
      labeling = jobRepository.findLabelingByJobIdAndUserIdForChecker(jobId, userId, step); // step 때문에 나뉨..
    } else {
      labeling = jobRepository.findLabelingByJobIdAndUserIdForViewer(jobId, step);
    }

    if (labeling != null && (labeling.getLabelingInfo() == null || labeling.getLabelingInfo().isEmpty())) {
      List<EngineLabelingInfo> labelingInfos = engineRepository.findLabelingByJobId(jobId);
      if (labelingInfos == null || labelingInfos.isEmpty()) {
        labeling.setLabelingInfo(new ArrayList<>());
      } else {
        labeling.setLabelingInfo(labelingInfos.stream()
            .map(info -> ComplexConversation.LabelingInfo.builder()
                .labelingId(info.getLabelingId() == null ? null : info.getLabelingId().toString())
                .speakerId(info.getSpeakerId())
                .build())
            .collect(Collectors.toList()));
      }
    }

    // 발화자 정보
    labeling.setSpeakers(projectRepository.getSpeakers(projectId));

    return labeling;
  }

  private ComplexConversation setLabeling(ComplexConversation labeling, String toolCode, ToolTypeMeta toolType,
      ObjectId projectId, ObjectId jobId) {
    if (toolCode.equals(toolType.getToolCode())) { // CC:복합대화
      // Meta 정보 가져오기
      ComplexConversation.Meta meta = jobRepository.findByProjectIdOrJobId(projectId, jobId);
      // engine.stt에서 stt 정보를 가져오기.
      ComplexConversation complexLabeling = engineRepository.findByJobId(jobId);
      if (complexLabeling != null) {
        Map<String, String> labelingInfo = new HashMap<>();
        complexLabeling.getVerbal()
            .forEach(v -> labelingInfo.put(v.getLabelingId().toString(), v.getSpeakerId()));
        complexLabeling.setLabelingInfo(labelingInfo.entrySet()
            .stream()
            .map(info -> ComplexConversation.LabelingInfo.builder()
                .labelingId(info.getKey())
                .speakerId(info.getValue())
                .build())
            .collect(Collectors.toList()));
        complexLabeling.setLabelingAll(new ArrayList<>());
        complexLabeling.setMeta(meta);
        labeling = complexLabeling;
      }
    }

    return labeling;
  }
}
