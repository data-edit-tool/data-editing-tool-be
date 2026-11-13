package ai.maum.data.edit.api.work.service;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.maum.data.edit.api.job.repository.JobRepository;
import ai.maum.data.edit.api.opensmile.OpensmileService;
import ai.maum.data.edit.api.project.repository.ProjectRepository;
import ai.maum.data.edit.api.user.enums.UserType;
import ai.maum.data.edit.api.work.WorkHistoryEntity;
import ai.maum.data.edit.api.work.dto.request.SaveLabelingRequest;
import ai.maum.data.edit.api.work.dto.request.UpdateStatusRequest;
import ai.maum.data.edit.api.work.dto.response.WorkStatusReasonResponse;
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
public class WorkService {

  private final ProjectRepository projectRepository;
  private final JobRepository jobRepository;
  private final WorkRepository workRepository;
  private final LComplexConversationService lComplexConversationService;
  private final WorkHistoryRepository workHistoryRepository;

  private final OpensmileService opensmileService;

  public List<WorkStatusReasonResponse> getStatusReasonList(ObjectId workId, String workStatus) {
    return workHistoryRepository.findStatusReasonList(workId, workStatus);
  }

  public Labeling saveLabeling(SaveLabelingRequest request) {
    Labeling labeling = null;

    String workStatus =
        workRepository.findWorkByWorkId(new ObjectId(request.getWorkspaceId()), new ObjectId(request.getWorkId()))
            .getWorkStatus();
    String toolCode = projectRepository.getToolTypeByProjectId(new ObjectId(request.getProjectId()));// 툴 코드 가져오기

    if (toolCode.equals(ToolTypeMeta.T_TYPE_CC.getToolCode())) { // 복합대화
      labeling = saveLabelingForT_TYPE_CC(request, workStatus);
    }

    return labeling;
  }

  public boolean updateStatus(UpdateStatusRequest request) {
    ObjectId projectId = new ObjectId(request.getProjectId());
    ObjectId jobId = new ObjectId(request.getJobId());
    ObjectId workId = new ObjectId(request.getWorkId());
    String nextStatus = request.getNextWorkStatus();

    checkPossibilityToUpdateStatus(projectId, jobId, workId, nextStatus);

    Map<String, Object> where = new HashMap<>();
    where.put("project_id", new ObjectId(request.getProjectId()));

    Map<String, Object> arrayFilters = new HashMap<>();
    arrayFilters.put("job.job_id", new ObjectId(request.getJobId()));
    arrayFilters.put("work.work_id", workId);

    Map<String, Object> set = new HashMap<>();
    set.put("jobs.$[job].works.$[work].work_status", nextStatus);

    UserInfo user = UserUtil.getUser();
    if (user.getType() == UserType.CHECKER && // 검수자일 경우
        (nextStatus.equals(WorkStatus.IS.getCode()) || nextStatus.equals(WorkStatus.IC.getCode()))) { // 검수 시작 시
      set.put("jobs.$[job].works.$[work].checker_id", user.getUserId());
      if (request.getWorkStatus().equals(WorkStatus.CM.getCode())) {
        set.put("jobs.$[job].works.$[work].check_step", 3);
      } else {
        set.put("jobs.$[job].works.$[work].check_step", 1);
      }
    }

    String reason = null; // 반려 || 작업 불가 사유

    if (nextStatus.equals(WorkStatus.RJ.getCode()) // 반려
        || nextStatus.equals(WorkStatus.IM.getCode()) // 작업 불가
        || nextStatus.equals(WorkStatus.NU.getCode()) // 작업 불가 최종 승인
    ) {
      reason = request.getReason();
      // 반려 시 work step이 1이 되도록 update
      if (nextStatus.equals(WorkStatus.RJ.getCode())) {
        set.put("jobs.$[job].works.$[work].step", 1);
      } else if (nextStatus.equals(WorkStatus.IM.getCode())) {
        set.put("jobs.$[job].works.$[work].check_step", null);
      }
    } else if (nextStatus.equals(WorkStatus.RQ.getCode())) {
      set.put("jobs.$[job].works.$[work].check_step", null);
    } else if (nextStatus.equals(WorkStatus.CM.getCode()) && // 검수 완료
        ToolTypeMeta.T_TYPE_CC.getToolCode().equals(projectRepository.getToolTypeByProjectId(projectId)) // 복합대화툴일 경우
    ) {
      opensmileService.insertCsv(new ObjectId(request.getWorkspaceId()), projectId, jobId, workId);
    }

    // work_status 업데이트
    boolean check = workRepository.updateOneWithFilters(Project.class, where, arrayFilters, set, null);

    if (check) {
      // work_history 이력 남기기
      workHistoryRepository.insert(
          WorkHistoryEntity.builder()
              .userId(user.getUserId())
              .preWorkStatus(request.getWorkStatus())
              .workStatus(request.getNextWorkStatus())
              .workId(workId)
              .reason(reason)
              .build()
      );
    }

    return check;
  }

  private void checkPossibilityToUpdateStatus(ObjectId projectId, ObjectId jobId, ObjectId workId, String nextStatus) {
    String currStatus = workRepository.checkCurrStatus(projectId, jobId, workId);
    List<String> checkStatus = new ArrayList<>();
    checkStatus.add(WorkStatus.IS.getCode());
    checkStatus.add(WorkStatus.IC.getCode());
    checkStatus.add(WorkStatus.NU.getCode());
    checkStatus.add(WorkStatus.CM.getCode());

    if (checkStatus.contains(nextStatus)
        && currStatus.equals(WorkStatus.AS.getCode())) { // 작업 중일 때 검수 단계에 진입하지 못 하도록
      throw BaseException.of(ResponseCode.WK_CANNOT_CHECK);
    } else if (checkStatus.contains(currStatus)
        && nextStatus.equals(WorkStatus.AS.getCode())) { // 검수 중일 때 제출 취소 못 하도록
      throw BaseException.of(ResponseCode.WK_CANNOT_CANCEL);
    }
  }

  /**
   * 툴 복합대화 라벨링 데이터 저장
   */
  private ComplexConversation saveLabelingForT_TYPE_CC(SaveLabelingRequest request, String workStatus) {
    int moveStep = request.getMoveStep();
    ComplexConversation labeling = null;

    UserInfo user = UserUtil.getUser();
    if (user.getType() == UserType.VIEWER) { // Viewer일 경우 단순히 데이터를 보여주는 작업만 함.
      labeling = jobRepository.findLabelingByJobIdAndUserIdForViewer(new ObjectId(request.getJobId()), moveStep);
    } else {
      int step;
      if (workStatus.equals(WorkStatus.IM.getCode())) { // 작업 불가일 경우, 데이터 저장없이 단순히 step 이동만 이루어짐.
        step = moveStep;
      } else {
        step =
            Integer.valueOf(
                lComplexConversationService.saveLabeling(user.getUserId(), user.getType(), request, workStatus)
                    .toString());
      }

      // 저장한 값 가져오기
      if (user.getType() == UserType.WORKER) {
        labeling =
            jobRepository.findLabelingByJobIdAndUserId(new ObjectId(request.getJobId()), user.getUserId(), step);
      } else if (user.getType() == UserType.CHECKER) {
        labeling =
            jobRepository.findLabelingByJobIdAndUserIdForChecker(new ObjectId(request.getJobId()), user.getUserId(),
                step);
      }
    }

    // 메타 정보 세팅
    if (labeling != null && labeling.getMeta() != null && labeling.getMeta().getWorkId() != null) {
      labeling.setSpeakers(projectRepository.getSpeakers(new ObjectId(request.getProjectId()))); // 발화자 정보
      labeling.setMeta(labeling.getMeta().toBuilder() // 반려 여부
          .hasRejected(
              !workHistoryRepository.findStatusReasonList(labeling.getMeta().getWorkId(), WorkStatus.RJ.getCode())
                  .isEmpty())
          .build());
    }

    return labeling;
  }
}
