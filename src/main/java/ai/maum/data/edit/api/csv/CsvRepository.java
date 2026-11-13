package ai.maum.data.edit.api.csv;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

import ai.maum.data.edit.api.job.vo.VoiceProperties;

public interface CsvRepository extends MongoRepository<CsvEntity, ObjectId> {

  List<CsvEntity> findByJobId(ObjectId jobId);

  CsvEntity findByJobIdAndJobFileId(ObjectId jobId, ObjectId jobFileId);

  @Aggregation(pipeline = {
    "{ $match: { job_id: ?0 } }",
    "{ $facet: {" +
      "max_values: [" +
        "{ $unwind: '$datas' }, " +
        "{ $project: { " +
          "_id: 0, " +
          "pitch: { $toDouble: '$datas.pitch' }, " +
          "loudness: { $toDouble: '$datas.loudness' }" +
        "} }," +
        "{ $group: {" +
          "_id: null, " +
          "max_pitch: { $max: '$pitch' }, " +
          "max_loudness: { $max: '$loudness' }" +
        "} }" +
      "], " +
      "documents_to_normalize: [" +
        "{ $project: { " +
          "_id: 0, " +
          "speakerId: '$speaker_id', " +
          "datas: 1" +
        "} }" +
      "]" +
    "} }",
    "{ $unwind: '$max_values' }",
    "{ $unwind: '$documents_to_normalize' }",
    "{ $project:  { " +
      "_id: 0, " +
      "speakerId: '$documents_to_normalize.speakerId', " +
      "measurements: { " +
        "$map: {" +
          "input: '$documents_to_normalize.datas', " +
          "as: 'data', " +
          "in: { " +
            "offset_millis: '$$data.time', " +
            "pitch: { " +
              "$divide: [ { $toDouble: '$$data.pitch' }, '$max_values.max_pitch' ]" +
            "}, " +
            "loudness: { " +
              "$divide: [ { $toDouble: '$$data.loudness' }, '$max_values.max_loudness' ]" +
            "}, " +
          "} " +
        "}" +
      "}" +
    "} }"
  })
  List<VoiceProperties> findVoicePropertiesByJobId(ObjectId jobId);
}
