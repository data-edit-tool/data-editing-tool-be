package ai.maum.data.edit.api.work.repository;

import org.bson.types.ObjectId;

import ai.maum.data.edit.api.user.enums.UserType;
import ai.maum.data.edit.api.work.dto.request.SaveLabelingRequest;
import ai.maum.data.edit.common.repository.BasicCustomRepository;

public interface WorkCustomRepository extends BasicCustomRepository {

  String checkCurrStatus(ObjectId projectId, ObjectId jobId, ObjectId workId);

  boolean checkMyJob(ObjectId userId, UserType userType, SaveLabelingRequest request);
}
