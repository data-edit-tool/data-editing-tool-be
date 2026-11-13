package ai.maum.data.edit.api.admin.job;

import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ai.maum.data.edit.api.admin.job.request.AdminJobAssignableUserListRequest;
import ai.maum.data.edit.api.admin.job.request.AdminJobWorkerUpdateRequest;
import ai.maum.data.edit.api.admin.job.response.AdminJobAssignableUserResponse;
import ai.maum.data.edit.api.admin.job.response.AdminJobResponse;
import ai.maum.data.edit.api.admin.job.vo.StartJobLabelingInfo;
import ai.maum.data.edit.api.engine.EngineRepository;
import ai.maum.data.edit.api.engine.vo.EngineLabelingInfo;
import ai.maum.data.edit.api.job.repository.JobRepository;
import ai.maum.data.edit.api.user.enums.UserType;
import ai.maum.data.edit.api.work.repository.WorkHistoryRepository;
import ai.maum.data.edit.api.work.repository.WorkRepository;
import ai.maum.data.edit.api.workspace.field.project.Project;
import ai.maum.data.edit.api.workspace.repository.WorkspaceRepository;
import ai.maum.data.edit.common.enums.ResponseCode;
import ai.maum.data.edit.common.enums.WorkStatus;
import ai.maum.data.edit.common.exception.BaseException;
import ai.maum.data.edit.common.response.PageResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminJobService {

  private final JobRepository jobRepository;
  private final WorkRepository workRepository;
  private final WorkspaceRepository workspaceRepository;
  private final WorkHistoryRepository workHistoryRepository;

  private final EngineRepository engineRepository;

  public PageResponse<AdminJobAssignableUserResponse> getAssignableUsers(AdminJobAssignableUserListRequest request) {
    return jobRepository.getAssignableUsers(request);
  }

  public List<AdminJobResponse> getJobs(ObjectId projectId) {
    return jobRepository.getJobs(projectId);
  }

  public void updateWorker(ObjectId jobId, AdminJobWorkerUpdateRequest request) {
    ObjectId userId = null;
    if (StringUtils.isNotBlank(request.getUserId())) {
      userId = new ObjectId(request.getUserId());
    }

    ObjectId projectId = new ObjectId(request.getProjectId());
    UserType userType = UserType.fromCode(request.getUserType());
    String status = workRepository.getWorkStatus(projectId, jobId);
    WorkStatus workStatus = WorkStatus.fromCode(status);

    if (userId != null) {
      // 워크스페이스 접근 가능 여부 검증
      if (!workspaceRepository.containsAccessUser(new ObjectId(request.getWorkspaceId()), userId, userType)) {
        throw BaseException.of(ResponseCode.WS_ACCESS_USER_NOT_EXIST);
      }

      // 작업자를 등록하면, 기존에 work 데이터가 존재하는지 확인하여, 존재하지 않는다면 work 데이터를 생성해야 한다.
      if (userType == UserType.WORKER) {
        // 작업자 지정은 RD, 변경은 AS, RJ 상태에서만 가능
        if (workStatus != WorkStatus.RD && workStatus != WorkStatus.AS && workStatus != WorkStatus.RJ) {
          throw BaseException.of(ResponseCode.JOB_WORKER_ASSIGN_NOT_ALLOWED);
        }

        if (workRepository.getWorksSize(projectId, jobId) == 0) {
          // 기본 work 데이터 생성
          Map<String, Object> where = new HashMap<>();
          where.put("project_id", projectId);

          Map<String, Object> value = new HashMap<>();
          value.put("work_id", new ObjectId());
          value.put("work_status", WorkStatus.AS.getCode());
          value.put("step", 1);
          value.put("worker_id", userId);

          // 라벨링 데이터 조회
          List<EngineLabelingInfo> labelingInfos = engineRepository.findLabelingByJobId(jobId);
          if (labelingInfos != null) {
            List<StartJobLabelingInfo> labelings = labelingInfos.stream()
                .map(StartJobLabelingInfo::new)
                .collect(Collectors.toList());
            value.put("labelings", labelings);
          } else {
            value.put("labelings", new ArrayList<>());
          }

          // works 생성
          Map<String, Object> push = new HashMap<>();
          push.put("jobs.$[job].works", value);

          Map<String, Object> arrayFilters = new HashMap<>();
          arrayFilters.put("job.job_id", jobId);

          workRepository.updateOneWithFilters(Project.class, where, arrayFilters, null, push);
          return;
        } else {
          if (workStatus == WorkStatus.RD) {
            // 서브셋에 작업자가 이미 존재하다면 작업자 지정 불가
            if (workRepository.existsWorkerByUserType(projectId, jobId, userType.getCode())) {
              throw BaseException.of(ResponseCode.JOB_WORKER_ASSIGN_NOT_ALLOWED);
            }
          }
        }
      } else {
        // 검수자 지정은 RQ, 변경은 IS 상태에서만 가능
        if (workStatus != WorkStatus.RQ && workStatus != WorkStatus.IS) {
          throw BaseException.of(ResponseCode.JOB_CHECKER_ASSIGN_NOT_ALLOWED);
        }

        if (workStatus == WorkStatus.RQ) {
          // 서브셋에 검수자가 이미 존재하다면 검수자 지정 불가
          if (workRepository.existsWorkerByUserType(projectId, jobId, userType.getCode())) {
            throw BaseException.of(ResponseCode.JOB_CHECKER_ASSIGN_NOT_ALLOWED);
          }
        }
      }
    } else {
      // 작업자 해제는 AS, RJ 상태에서만 가능
      if (userType == UserType.WORKER && (workStatus != WorkStatus.AS && workStatus != WorkStatus.RJ)) {
        throw BaseException.of(ResponseCode.JOB_WORKER_CHANGE_NOT_ALLOWED);
      }

      // 검수자 해제는 RQ, IS 상태에서만 가능
      if (userType == UserType.CHECKER && (workStatus != WorkStatus.RQ && workStatus != WorkStatus.IS)) {
        throw BaseException.of(ResponseCode.JOB_CHECKER_CHANGE_NOT_ALLOWED);
      }
    }

    // 변경되는 작업자의 유형에 따라 work_status 변경
    jobRepository.updateWorker(projectId, jobId, workStatus, userId, userType);
  }
}
