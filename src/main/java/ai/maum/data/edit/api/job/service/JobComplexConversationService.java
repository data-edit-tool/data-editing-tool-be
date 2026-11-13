package ai.maum.data.edit.api.job.service;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import ai.maum.data.edit.api.job.dto.request.StartJobRequest;
import ai.maum.data.edit.api.job.repository.JobComplexConversationRepository;
import ai.maum.data.edit.api.workspace.field.project.ComplexConversation;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JobComplexConversationService {

  @Autowired
  JobComplexConversationRepository jobRepository;

  public List<ComplexConversation> findSpeakersByJobId(String jobId) {
    return jobRepository.findSpeakersByJobId(new ObjectId(jobId));
  }

  public List<StartJobRequest.ComplexConversationLabeling> setVerbal(StartJobRequest request) {
    List<StartJobRequest.ComplexConversationLabeling> labelings = request.getLabelings();
    for (int i = 0; i < labelings.size(); i++) {
      List<StartJobRequest.ComplexConversationLabeling.Verbal> data = labelings.get(i).getVerbal();
      for (int j = 0; j < data.size(); j++) {
        data.get(j).setVerbalId(new ObjectId());
      }

      labelings.get(i).setVerbal(data);
      labelings.get(i).setLabelingId(new ObjectId());
      // MongoDB 특성상 빈 배열을 넣어줌.
      labelings.get(i).setParaverbal(new ArrayList());
      labelings.get(i).setNonverbal(new ArrayList());
    }

    return labelings;
  }
}
