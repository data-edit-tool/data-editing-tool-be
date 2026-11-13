package ai.maum.data.edit.api.job.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

import ai.maum.data.edit.api.workspace.field.job.Job;
import ai.maum.data.edit.api.workspace.field.project.ComplexConversation;
import ai.maum.data.edit.common.repository.BasicCustomRepository;

public interface JobComplexConversationRepository extends MongoRepository<Job, ObjectId>, BasicCustomRepository {

  @Aggregation(pipeline = {
      "{$match: { 'jobs.job_id': ?0 }}",
      "{$project: { speakers: 1 }}",
      "{$unwind: '$speakers' }",
      "{$replaceRoot: { newRoot: '$speakers' }}"
  })
  List<ComplexConversation> findSpeakersByJobId(ObjectId jobId);
}
