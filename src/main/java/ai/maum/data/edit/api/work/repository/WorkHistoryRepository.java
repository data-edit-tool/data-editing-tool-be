package ai.maum.data.edit.api.work.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

import ai.maum.data.edit.api.work.WorkHistoryEntity;
import ai.maum.data.edit.api.work.dto.response.WorkStatusReasonResponse;
import ai.maum.data.edit.common.repository.BasicCustomRepository;

public interface WorkHistoryRepository extends MongoRepository<WorkHistoryEntity, ObjectId>, BasicCustomRepository {
  @Aggregation(pipeline = {
      "{ $match: { work_id: ?0, work_status: ?1 } }",
      "{ $sort: { created: 1 } }",
      "{ $project: { _id: 0, reason: 1, created: 1 } }"
  })
  List<WorkStatusReasonResponse> findStatusReasonList(ObjectId workId, String workStatus);
}
