package ai.maum.data.edit.api.project.repository;

import org.bson.types.ObjectId;

import java.util.List;

import ai.maum.data.edit.api.project.vo.SpeakerInfo;
import ai.maum.data.edit.api.user.enums.UserType;
import ai.maum.data.edit.api.workspace.dto.response.ProjectListResponse;
import ai.maum.data.edit.common.repository.BasicCustomRepository;

public interface ProjectCustomRepository extends BasicCustomRepository {

  ProjectListResponse getProjects(ObjectId workspaceId, UserType userType, ObjectId userId);

  List<SpeakerInfo> getSpeakers(ObjectId projectId);
}