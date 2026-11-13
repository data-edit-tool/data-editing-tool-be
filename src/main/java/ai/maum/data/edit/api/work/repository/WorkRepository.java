package ai.maum.data.edit.api.work.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import ai.maum.data.edit.api.workspace.field.project.Project;
import ai.maum.data.edit.api.workspace.field.work.Work;

public interface WorkRepository extends MongoRepository<Project, ObjectId>, WorkCustomRepository {
  @Aggregation(pipeline = {
      "{ $match: {project_id: ?0} }",
      "{ $project: {jobs: 1} }",
      "{ $unwind: '$jobs' }",
      "{ $replaceRoot: {newRoot: '$jobs'} }",
      "{ $match: {job_id: ?1} }",
      "{ $project: { " +
          "exists: { " +
            "$cond: [ " +
              "{ $eq: [ ?2, 'W' ] }, " +
              "{ $gt: [ { $size: { $filter: { input: { $ifNull: [ '$works.worker_id', [] ] }, cond: { $ne: ['$$this', null] } } } }, 0 ] }, " +
              "{ $gt: [ { $size: { $filter: { input: { $ifNull: [ '$works.checker_id', [] ] }, cond: { $ne: ['$$this', null] } } } }, 0 ] } " +
            "] " +
          "} " +
      "}}"
  })
  Boolean existsWorkerByUserType(ObjectId projectId, ObjectId jobId, String userType);

  @Aggregation(pipeline = {
      "{ $match: { workspace_id: ?0 } }",
      "{ $project: { jobs: 1 } }",
      "{ $unwind: '$jobs' }",
      "{ $replaceRoot: { newRoot: '$jobs' } }",
      "{ $project: { works: 1 } }",
      "{ $unwind: '$works' }",
      "{ $replaceRoot: { newRoot: '$works' } }",
      "{ $match: { work_id: ?1 } }"
  })
  Work findWorkByWorkId(ObjectId workspaceId, ObjectId workId);

  @Aggregation(pipeline = {
      "{ $match: {project_id: ?0} }",
      "{ $project: {jobs: 1} }",
      "{ $unwind: '$jobs' }",
      "{ $replaceRoot: {newRoot: '$jobs'} }",
      "{ $match: {job_id: ?1} }",
      "{ $project: { " +
          "status: { " +
              "$cond: [ " +
                "{ $eq: [ { $ifNull: ['$works', null] }, null ] }, " +
                "'RD' , " +
                "{ $arrayElemAt: [ '$works.work_status', 0 ] }" +
             "] " +
          "} " +
      "}}"
  })
  String getWorkStatus(ObjectId projectId, ObjectId jobId);

  @Aggregation(pipeline = {
      "{ $match: {project_id: ?0} }",
      "{ $project: {jobs: 1} }",
      "{ $unwind: '$jobs' }",
      "{ $replaceRoot: {newRoot: '$jobs'} }",
      "{ $match: {job_id: ?1} }",
      "{ $project: { size: { $cond: [ { $eq: [{ $type: '$works' }, 'array'] } ,{$size: '$works'} ,0 ] } } }"
  })
  Integer getWorksSize(ObjectId projectId, ObjectId jobId);
}
