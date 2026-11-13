package ai.maum.data.edit.api.workspace.repository;

import org.bson.types.ObjectId;

import java.util.List;

import ai.maum.data.edit.api.user.enums.UserType;
import ai.maum.data.edit.api.workspace.dto.response.WorkspaceListResponse;
import ai.maum.data.edit.common.repository.BasicCustomRepository;

public interface WorkspaceCustomRepository extends BasicCustomRepository {
  List<WorkspaceListResponse> findWorkspaceEntityBy(String clientId, ObjectId userId, String workspaceName,
      String workspaceIntro, String incharge, String startDate, String endDate);

  boolean containsAccessUser(ObjectId workspaceId, ObjectId userId, UserType userType);
}
