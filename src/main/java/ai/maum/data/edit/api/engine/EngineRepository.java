package ai.maum.data.edit.api.engine;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

import ai.maum.data.edit.api.admin.project.vo.EngineForUploadValue;
import ai.maum.data.edit.api.engine.vo.EngineLabelingInfo;
import ai.maum.data.edit.api.workspace.field.labeling.ComplexConversation;
import ai.maum.data.edit.common.repository.BasicCustomRepository;

public interface EngineRepository extends MongoRepository<EngineEntity, ObjectId>, BasicCustomRepository {

  @Aggregation(pipeline = {
      "{ $match: { job_id: ?0 } }",
      "{ $project: {" +
          "_id: 0," +
          "verbal: {" +
            "$reduce: {" +
              "input: { $ifNull: ['$stt', []] }," +
              "initialValue: []," +
              "in: {" +
                "$concatArrays: [" +
                  "$$value," +
                  "{ $cond: [" +
                    "{ $gt: [ { $size: { $ifNull: ['$$this.data', []] } }, 0 ] }," +
                    "{ $map: {" +
                      "input: '$$this.data'," +
                      "as: 'd'," +
                      "in: {" +
                        "verbal_id: '$$d.data_id'," +
                        "labeling_id: '$$this.stt_id'," +
                        "speaker_id: '$$this.speaker_id'," +
                        "start_time: '$$d.start_time'," +
                        "end_time: '$$d.end_time'," +
                        "sentence: '$$d.sentence'" +
                      "}" +
                    "} }," +
                  "[]" +
                "] }" +
              "]" +
            "}" +
          "}" +
        "}" +
      "} }"
  })
  ComplexConversation findByJobId(ObjectId jobId);

  @Aggregation(pipeline = {
    "{ $match: { job_id: ?0 } }",
    "{ $project: { _id: 0, stt: 1 } }",
    "{ $unwind: '$stt' }",
    "{ $replaceRoot: { newRoot: '$stt' } }",
    "{ $project: { " +
        "speaker_id: 1, " +
        "labeling_id: '$stt_id', " +
        "verbals: { " +
          "$map: { " +
            "input: '$data', " +
            "as: 'd', " +
            "in: { " +
              "verbal_id: '$$d.data_id', " +
              "start_time: '$$d.start_time', " +
              "end_time: '$$d.end_time', " +
              "sentence: '$$d.sentence' " +
            "} " +
          "} " +
        "} " +
    "} }"
  })
  List<EngineLabelingInfo> findLabelingByJobId(ObjectId objectId);

    @Aggregation(pipeline = {
            "{ $match: { 'stt.stt_id': ?0 } }",
            "{ $facet: {" +
                "engine_id: [{ $project: { engine_id: 1 } }]," +
                "stt_id: [" +
                    "{ $project: { stt: 1 } }," +
                    "{ $unwind: '$stt' }," +
                    "{ $replaceRoot: { newRoot: '$stt' } }," +
                    "{ $match: { file_name: ?1, file_status: 'ERROR' } }," +
                    "{ $project: { stt_id: 1 } }] } }",
            "{ $project: {" +
                "engine_id: { $arrayElemAt: ['$engine_id.engine_id', 0] }," +
                "stt_id: { $arrayElemAt: ['$stt_id.stt_id', 0] } } }"
    })
    EngineForUploadValue findEngineForReUpload(ObjectId sttId,
                                               String fileName);
}
