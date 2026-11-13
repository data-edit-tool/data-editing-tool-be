package ai.maum.data.edit.api.job.service;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.List;

import ai.maum.data.edit.api.job.dto.response.JobFileResponse;
import ai.maum.data.edit.api.job.repository.JobFileRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JobFileService {

  private final JobFileRepository jobFileRepository;

  public JobFileResponse findJobFile(String fileId) {
    return jobFileRepository.findJobFileByFileId(new ObjectId(fileId));
  }

  public List<JobFileResponse> jobFileList(String jobId) {
    return jobFileRepository.findAllJobFileByJobId(new ObjectId(jobId));
  }
}
