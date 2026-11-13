package ai.maum.data.edit.api.job.repository;

import org.bson.types.ObjectId;

import java.util.List;

import ai.maum.data.edit.api.job.dto.response.JobFileResponse;
import ai.maum.data.edit.common.repository.BasicCustomRepository;

public interface JobFileCustomRepository extends BasicCustomRepository {
  List<JobFileResponse> getFiles(ObjectId workspaceId, ObjectId projectId, ObjectId jobId);
}
