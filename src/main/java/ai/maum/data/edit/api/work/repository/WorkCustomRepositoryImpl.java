package ai.maum.data.edit.api.work.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;

import ai.maum.data.edit.api.user.enums.UserType;
import ai.maum.data.edit.api.work.dto.request.SaveLabelingRequest;
import ai.maum.data.edit.api.workspace.WorkspaceEntity;
import ai.maum.data.edit.api.workspace.field.work.Work;
import ai.maum.data.edit.common.repository.BasicCustomRepositoryImpl;
import ai.maum.data.edit.common.repository.CustomAggregation;

public class WorkCustomRepositoryImpl extends BasicCustomRepositoryImpl implements WorkCustomRepository {

  public WorkCustomRepositoryImpl(MongoTemplate mongoTemplate) {
    super(mongoTemplate);
  }

  @Override
  public String checkCurrStatus(ObjectId projectId, ObjectId jobId, ObjectId workId) {
    return CustomAggregation.setAggregation("project")
        .match("project_id", projectId)
        .project("jobs")
        .unwind("jobs")
        .match("jobs.job_id", jobId)
        .replaceRoot("jobs")
        .project("works")
        .unwind("works")
        .match("works.work_id", workId)
        .replaceRoot("works")
        .project("work_status")
        .aggregation(Work.class, mongoTemplate)
        .getWorkStatus()
        ;
  }

  @Override
  public boolean checkMyJob(ObjectId userId, UserType userType, SaveLabelingRequest request) {
    StringBuilder key = new StringBuilder("works.");
    if (userType == UserType.WORKER) {
      key.append("worker_id");
    } else if (userType == UserType.CHECKER) {
      key.append("checker_id");
    }

    return CustomAggregation.setAggregation("project")
        .match("project_id", new ObjectId(request.getProjectId()))
        .project("jobs")
        .unwind("jobs")
        .match("jobs.job_id", new ObjectId(request.getJobId()))
        .replaceRoot("jobs")
        .project("works")
        .unwind("works")
        .match(
            "works.work_id", new ObjectId(request.getWorkId())
            , key.toString(), userId
        )
        .aggregation(WorkspaceEntity.class, mongoTemplate) != null;
  }
}
