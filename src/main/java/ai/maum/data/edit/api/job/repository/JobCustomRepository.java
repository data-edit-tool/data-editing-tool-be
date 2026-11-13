package ai.maum.data.edit.api.job.repository;

import org.bson.types.ObjectId;

import java.util.List;

import ai.maum.data.edit.api.admin.job.request.AdminJobAssignableUserListRequest;
import ai.maum.data.edit.api.admin.job.response.AdminJobAssignableUserResponse;
import ai.maum.data.edit.api.admin.job.response.AdminJobResponse;
import ai.maum.data.edit.api.user.enums.UserType;
import ai.maum.data.edit.common.enums.WorkStatus;
import ai.maum.data.edit.common.response.PageResponse;

public interface JobCustomRepository {

  PageResponse<AdminJobAssignableUserResponse> getAssignableUsers(AdminJobAssignableUserListRequest request);

  List<AdminJobResponse> getJobs(ObjectId projectId);

  void updateWorker(ObjectId projectId, ObjectId jobId, WorkStatus workStatus, ObjectId userId, UserType userType);
}
