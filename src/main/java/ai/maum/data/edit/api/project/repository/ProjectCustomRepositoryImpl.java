package ai.maum.data.edit.api.project.repository;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.List;

import ai.maum.data.edit.api.project.dto.response.ProjectResponse;
import ai.maum.data.edit.api.project.vo.SpeakerInfo;
import ai.maum.data.edit.api.user.enums.UserType;
import ai.maum.data.edit.api.workspace.dto.response.ProjectListResponse;
import ai.maum.data.edit.api.workspace.field.project.enums.ProjectStatus;
import ai.maum.data.edit.common.enums.WorkStatus;
import ai.maum.data.edit.common.repository.BasicCustomRepositoryImpl;
import ai.maum.data.edit.common.repository.CustomAggregation;

public class ProjectCustomRepositoryImpl extends BasicCustomRepositoryImpl implements ProjectCustomRepository {

  private final String collection = "project";

  public ProjectCustomRepositoryImpl(MongoTemplate mongoTemplate) {
    super(mongoTemplate);
  }

  @Override
  public ProjectListResponse getProjects(ObjectId workspaceId, UserType userType, ObjectId userId) {
    ProjectListResponse result = new ProjectListResponse();
    /**
     * 권한 별 분기
     * worker : (works.work_status : null || 'RD') && !works.worker_id: 로그인 id
     * checker : (works.work_status : 'RQ' || 'IM') && !works.checker_id: 로그인 id
     * viewer : works.work_status : CM || NU 최종 검수 완료된 것만
     * */
    // ===== Available Task ===== //
    Criteria matchForAvailable = setBasicCriteria();
    String[] status = new String[2];
    String typeCheck = null;
    if (userType == UserType.WORKER) {
      typeCheck = "worker_id";
      status[0] = null;
      status[1] = WorkStatus.RD.getCode();
    } else if (userType == UserType.CHECKER) {
      typeCheck = "checker_id";
      status[0] = WorkStatus.RQ.getCode();
      status[1] = WorkStatus.IM.getCode();
    } else { // viewer
      status[0] = WorkStatus.CM.getCode();
      status[1] = WorkStatus.NU.getCode();
    }
    setMatchToGetAvailable(matchForAvailable, typeCheck, userId, status);
    // ===== Available Task ===== //

    // ===== Task in progress ===== //
    Criteria matchForTaskIn = setBasicCriteria();
    setMatchToGetTaskIn(matchForTaskIn, typeCheck, userId);
    // ===== Task in progress ===== //

    // Available Task, Task in progress - $facet을 이용하여 한 쿼리로 처리하였다가 분리함. cause, $facet 단계에서 프로젝트 정보가 모두 하나로 묶여 16mb가 넘는 문제가 발생하였었음.
    result.setAllProjects(CustomAggregation.setAggregation(collection).match("workspace_id", workspaceId)
        .match(matchForAvailable)
        .sort(Sort.by(Sort.Order.asc("project_name")))
        .aggregationList(ProjectResponse.class, mongoTemplate)
    );

    result.setAllProjectsByUserId(CustomAggregation.setAggregation(collection).match("workspace_id", workspaceId)
        .match(matchForTaskIn)
        .sort(Sort.by(Sort.Order.asc("project_name")))
        .aggregationList(ProjectResponse.class, mongoTemplate)
    );
    return result;
  }

  @Override
  public List<SpeakerInfo> getSpeakers(ObjectId projectId) {
    return CustomAggregation.setAggregation(collection)
        .match("project_id", projectId)
        .project("speakers")
        .unwindAndReplaceRoot("speakers")
        .project("speaker_id", "thumbnail_file_url")
        .aggregationList(SpeakerInfo.class, mongoTemplate);
  }

  private Criteria setBasicCriteria() {
    return Criteria.where("is_deleted").is(false).and("post_status").is(ProjectStatus.POST_ON.getCode());
  }

  private void setMatchToGetAvailable(Criteria criteria, String typeCheck, ObjectId userId, String[] status) {
    if (typeCheck != null) { // worker || checker
      criteria.and("jobs.works.work_status").in(status)
          .and("jobs.works").not().elemMatch(Criteria.where(typeCheck).is(userId));
    } else { // viewer
      criteria.and("jobs.works.work_status").in(status);
    }
  }

  private void setMatchToGetTaskIn(Criteria criteria, String typeCheck, ObjectId userId) {
    criteria.and("jobs.works." + typeCheck).is(userId);
  }
}
