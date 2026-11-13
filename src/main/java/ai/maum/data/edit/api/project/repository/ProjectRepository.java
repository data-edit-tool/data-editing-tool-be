package ai.maum.data.edit.api.project.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;

import ai.maum.data.edit.api.admin.project.dto.response.AdminProjectFileListResponse;
import ai.maum.data.edit.api.admin.project.dto.response.AdminProjectListResponse;
import ai.maum.data.edit.api.admin.project.vo.EngineForUploadValue;
import ai.maum.data.edit.api.admin.project.vo.JobFileForProjectFileUploadValue;
import ai.maum.data.edit.api.admin.project.vo.SpeakerForUploadValue;
import ai.maum.data.edit.api.admin.project.vo.UploadResultNotSuccessValue;
import ai.maum.data.edit.api.category.vo.ProjectClientCtgValue;
import ai.maum.data.edit.api.project.dto.response.ProjectExtractResponse;
import ai.maum.data.edit.api.project.dto.response.ProjectInfoResponse;
import ai.maum.data.edit.api.workspace.field.project.Project;

public interface ProjectRepository extends MongoRepository<Project, ObjectId>, ProjectCustomRepository {
  @Aggregation(pipeline = {
    "{ $match: {project_id: ?0}}",
    "{ $project: { _id: 0, workspace_id: 1 } }"
  })
  Project getWorkspaceIdByProjectId(ObjectId projectId);

  @Aggregation(pipeline = {
    "{ $match : {'jobs.job_id': ?0} }",
    "{ $project: { _id: 0, workspace_id: 1 } }"
  })
  Project getWorkspaceIdByJobId(ObjectId JobId);

  @Aggregation(pipeline = {
    "{ $match : {'jobs.works.work_id': ?0} }",
    "{ $project: { _id: 0, workspace_id: 1 } }"
  })
  Project getWorkspaceIdByWorkId(ObjectId workId);

  @Aggregation(pipeline = {
    "{ $match : {'jobs.job_files.file_id': ?0} }",
    "{ $project: { _id: 0, workspace_id: 1 } }"
  })
  Project getWorkspaceIdByFileId(ObjectId FileId);

  @Aggregation(pipeline = {
    "{$match: {project_id: {$eq: ?0}}}",
    "{$project: {_id: 0 ,tool_type: '$tool_type.code'}}"
  })
  String getToolTypeByProjectId(ObjectId projectId);

  /**
   * is_multiple_work(다중 작업이 가능한지) 값을 가져오는 쿼리.
   */
  @Aggregation(pipeline = {
          "{$match: {'project_id': ?0}}"
          ,"{$project: {'is_multiple_work':1}}"
  })
  Boolean getIsMultipleWorkByProjectId(ObjectId projectId);

  @Aggregation({
      "{ $match: { workspace_id: ?0 } }",
      "{ $project: { _id: 0, project_id: 1 } }"
  })
  List<ObjectId> findProjectIdsByWorkspaceId(ObjectId workspaceId);

  @Aggregation(pipeline = {
    "{ $match: {" +
      "$expr: {" +
        "$and: [" +
          "{ $eq: ['$workspace_id', ?1] }," +
          "{ $or: [ { $eq: [?9, null]}, { $eq: ['$is_deleted', ?9]} ]}] } } }",
    "{ $facet: {" +
      "project_count: [{ $count: 'value' }]," +
      "projects: [" +
        "{ $addFields:  {" +
          "endDate: {" +
            "$cond: {" +
              "if: { $ne: [?8, null]}," +
              "then: { $dateAdd: { startDate: ?8, amount: 1, unit: 'day' } }," +
              "else: null}}}}," +
        "{ $match: {" +
          "$expr: {" +
            "$and: [" +
              "{ $or: [ {$eq: [?2, null] }, { $eq: ['$tool_type.code', ?2] }]}," +
              "{ $or: [ {$eq: [?5, null] }, { $eq: ['$post_status', ?5] }]}," +
              "{ $or: [ {$eq: [?7, null] }, { $gte: ['$created', ?7] }]}," +
              "{ $or: [ {$eq: [?8, null] }, { $lt: ['$created', '$endDate'] }]}]}}}," +
        "{ $match: { project_name: ?6 } }," +
        "{ $addFields: { post_status: { $cond: { if: { $eq: ['$post_status', 'PJ_POST_ST_ON'] }, then: '게시중', else: '일시정지' } } } }," +
        "{ $project: { relationship: 0, is_multiple_work: 0, updated: 0 } }," +
        "{ $lookup: {" +
          "from: 'client_category'," +
          "localField: 'client_ctg_id'," +
          "foreignField: 'client_ctg_id'," +
          "as: 'category' } }," +
        "{ $unwind: '$category' }," +
        "{ $project: {" +
          "project_id: 1," +
          "project_name: 1," +
          "post_status: 1," +
          "data_type: '$data_type.name'," +
          "tool_type: '$tool_type.name'," +
          "category_type: '$category.client_ctg_name'," +
          "jobs: {" +
            "$map: {" +
              "input: '$jobs'," +
              "as: 'job'," +
              "in: {" +
                "job_id: '$$job.job_id'," +
                "job_name: '$$job.job_name'," +
                "job_files: '$$job.job_files'," +
                "works: {" +
                  "$map: {" +
                    "input: '$$job.works'," +
                    "as: 'work'," +
                    "in: {" +
                      "worker_id: '$$work.worker_id'," +
                      "work_status: '$$work.work_status' }}}}}}," +
          "speakers: 1," +
          "created: 1 } }] } }",
    "{ $project: { project_count: { $arrayElemAt: ['$project_count.value', 0] }, projects: 1 } }",
    "{ $addFields: { project_count: { $cond: { if: { $eq: [{ $ifNull: ['$project_count', null] }, null] }, then: 0, else: '$project_count' } } } }",
    "{ $facet: {" +
      "data: [{ $replaceRoot: { newRoot: '$$ROOT' } }]," +
      "jobs: [" +
        "{ $unwind: '$projects' }," +
        "{ $replaceRoot: { newRoot: '$projects' } }," +
        "{ $project: { project_id: 1, jobs: 1 } }] } }",
    "{ $facet: {" +
      "data: [" +
        "{ $unwind: '$data' }," +
        "{ $replaceRoot: { newRoot: '$data' } }]," +
      "jobs_count: [" +
        "{ $unwind: '$jobs'}," +
        "{ $replaceRoot: { newRoot: '$jobs'}}," +
        "{ $project: { project_id: 1, count: { $size: { $ifNull: ['$jobs', []] } } } }]," +
      "speaker_files_info: [" +
        "{ $unwind: '$data' }," +
        "{ $replaceRoot: { newRoot: '$data' } }," +
        "{ $unwind: '$projects' }," +
        "{ $replaceRoot: { newRoot: '$projects' } }," +
        "{ $project: {" +
          "project_id: 1," +
          "speaker_info: {" +
            "$map: {" +
              "input: '$speakers'," +
              "in: {" +
                "file_name: '$$this.thumbnail_file_name'," +
                "file_status: '$$this.thumbnail_file_status'," +
                "file_size: '$$this.thumbnail_file_size' } } } } }]," +
      "job_files_info: [" +
        "{ $unwind: '$jobs' }," +
        "{ $replaceRoot: { newRoot: '$jobs' } }," +
        "{ $project: {" +
          "project_id: 1," +
          "job_files: {" +
            "$reduce: {" +
              "input: '$jobs.job_files'," +
              "initialValue: []," +
              "in: { $concatArrays: ['$$value', '$$this'] } } } } }]," +
      "stt_files_info: [" +
        "{ $unwind: '$data' }," +
        "{ $replaceRoot: { newRoot: '$data' } }," +
        "{ $unwind: '$projects' }," +
        "{ $replaceRoot: { newRoot: '$projects' } }," +
        "{ $project: {" +
          "project_id: 1," +
          "job_ids: {" +
            "$map: {" +
              "input: '$jobs'," +
              "in: {" +
                "$getField: {" +
                  "field: 'job_id'," +
                  "input: '$$this' } } } } } }," +
        "{ $lookup: {" +
          "from: 'engine'," +
          "localField: 'job_ids'," +
          "foreignField: 'job_id'," +
          "as: 'engine_info' } }," +
        "{ $project: {" +
          "project_id: 1," +
          "stt_info: {" +
            "$map: {" +
              "input: {" +
                "$reduce: {" +
                  "input: {" +
                    "$map: {" +
                      "input: '$engine_info'," +
                      "in: {" +
                        "$getField: {" +
                          "field: 'stt'," +
                          "input: '$$this' } } } }," +
                  "initialValue: []," +
                  "in: { $concatArrays: ['$$value', '$$this'] } } }," +
                "in: {" +
                  "file_name: '$$this.file_name'," +
                  "file_status: '$$this.file_status'," +
                  "file_size: '$$this.file_size' } } } } }]," +
        "works_info: [" +
          "{ $unwind: '$jobs' }," +
          "{ $replaceRoot: { newRoot: '$jobs' } }," +
          "{ $project: {" +
            "project_id: 1," +
            "works: {" +
              "$reduce: {" +
                "input: '$jobs.works'," +
                "initialValue: []," +
                "in: { $concatArrays: ['$$value', { $ifNull: ['$$this', []] }] } } } } }] } }",
    "{ $addFields: { 'data.jobs_count': '$jobs_count' } }",
    "{ $project: { jobs_count: 0 } }",
    "{ $facet: {" +
      "data: [" +
        "{ $replaceRoot: { newRoot: '$$ROOT' } }," +
        "{ $project: {" +
          "data: 1," +
          "speaker_files_count: {" +
            "$map: {" +
              "input: '$speaker_files_info'," +
              "as: 'info'," +
              "in: { project_id: '$$info.project_id', count: { $size: { $ifNull: ['$$info.speaker_info', []] } } } } }," +
          "job_files_count: {" +
            "$map: {" +
              "input: '$job_files_info'," +
              "as: 'info'," +
              "in: { project_id: '$$info.project_id', count: { $size: { $ifNull: ['$$info.job_files', []]} } } } }," +
          "stt_files_count: {" +
            "$map: {" +
              "input: '$stt_files_info'," +
              "as: 'info'," +
              "in: { project_id: '$$info.project_id', count: { $size: { $ifNull: ['$$info.stt_info', []]} } } } }," +
          "works_count: {" +
            "$map: {" +
              "input: '$works_info'," +
              "as: 'info'," +
              "in: { project_id: '$$info.project_id', count: { $size: { $ifNull: ['$$info.works', []]} } } } } } }]," +
      "speaker_files_info: [" +
        "{ $unwind: '$speaker_files_info' }," +
        "{ $replaceRoot: { newRoot: '$speaker_files_info' } }," +
        "{ $unwind: '$speaker_info' }," +
        "{ $group: {" +
          "_id: { project_id: '$project_id', file_status: '$speaker_info.file_status' }," +
          "count: { $sum: 1 } } }," +
        "{ $group: {" +
          "_id: '$_id.project_id'," +
          "file_status: {" +
            "$push: {" +
              "key: '$_id.file_status'," +
              "value: '$count' } } } }," +
        "{ $project: { _id: 0, project_id: '$_id', file_status: 1 } }]," +
      "job_files_info: [" +
        "{ $unwind: '$job_files_info' }," +
        "{ $replaceRoot: { newRoot: '$job_files_info' } }," +
        "{ $unwind: '$job_files' }," +
        "{ $group: {" +
          "_id: { project_id: '$project_id', file_status: '$job_files.file_status' }," +
          "count: { $sum: 1 } } }," +
        "{ $group: {" +
          "_id: '$_id.project_id'," +
          "file_status: {" +
            "$push: {" +
              "key: '$_id.file_status'," +
              "value: '$count' } } } }," +
        "{ $project: { _id: 0, project_id: '$_id', file_status: 1 } }]," +
      "stt_files_info: [" +
        "{ $unwind: '$stt_files_info' }," +
        "{ $replaceRoot: { newRoot: '$stt_files_info' } }," +
        "{ $unwind: '$stt_info' }," +
        "{ $group: {" +
          "_id: { project_id: '$project_id', file_status: '$stt_info.file_status' }," +
          "count: { $sum: 1 } } }," +
        "{ $group: {" +
          "_id: '$_id.project_id'," +
          "file_status: {" +
            "$push: {" +
              "key: '$_id.file_status'," +
              "value: '$count' } } } }," +
        "{ $project: { _id: 0, project_id: '$_id', file_status: 1 } }]," +
      "works_info: [" +
        "{ $unwind: '$works_info' }," +
        "{ $replaceRoot: { newRoot: '$works_info' } }," +
        "{ $unwind: '$works' }," +
        "{ $match: { $and: [{ works: { $exists: true } }, { 'works.worker_id': { $exists: true } }] } }," +
        "{ $group: {" +
          "_id: { project_id: '$project_id', work_status: '$works.work_status' }," +
          "count: { $sum: 1 } } }," +
        "{ $group: {" +
          "_id: '$_id.project_id'," +
          "work_status: {" +
            "$push: {" +
              "key: '$_id.work_status'," +
              "value: '$count' } } } }," +
        "{ $project: { _id: 0, project_id: '$_id', work_status: 1 } }] } }",
    "{ $unwind: '$data' }",
    "{ $project: {" +
      "data: '$data.data'," +
      "speaker_files_info: 1," +
      "job_files_info: 1," +
      "stt_files_info: 1," +
      "works_info: 1," +
      "speaker_files_count: '$data.speaker_files_count'," +
      "job_files_count: '$data.job_files_count'," +
      "stt_files_count: '$data.stt_files_count'," +
      "works_count: '$data.works_count' } }",
    "{ $addFields: {" +
      "'data.speaker_files_info': '$speaker_files_info'," +
      "'data.job_files_info': '$job_files_info'," +
      "'data.stt_files_info': '$stt_files_info'," +
      "'data.works_info': '$works_info'," +
      "'data.speaker_files_count': '$speaker_files_count'," +
      "'data.job_files_count': '$job_files_count'," +
      "'data.stt_files_count': '$stt_files_count'," +
      "'data.works_count': '$works_count' } }",
    "{ $project: { data: 1 } }",
    "{ $unwind: '$data' }",
    "{ $replaceRoot: { newRoot: '$data' } }",
    "{ $addFields: {" +
      "projects: {" +
        "$map: {" +
          "input: '$projects'," +
          "as: 'project'," +
          "in: {" +
            "project_id: '$$project.project_id'," +
            "project_name: '$$project.project_name'," +
            "data_type: '$$project.data_type'," +
            "tool_type: '$$project.tool_type'," +
            "category_type: '$$project.category_type'," +
            "jobs_count: {" +
              "$getField: {" +
                "field: 'count'," +
                "input: {" +
                  "$arrayElemAt: [" +
                    "{ $filter: {" +
                      "input: '$jobs_count'," +
                      "as: 'info'," +
                      "cond: { $eq: ['$$project.project_id', '$$info.project_id'] } } }, 0] }}}," +
            "upload_status: {" +
              "info: {" +
                "$reduce: {" +
                  "input: {" +
                    "$concatArrays: [" +
                      "{ $getField: {" +
                        "field: 'file_status'," +
                        "input: {" +
                          "$arrayElemAt: [" +
                            "{ $filter: {" +
                              "input: '$speaker_files_info'," +
                              "as: 'info'," +
                              "cond: { $eq: ['$$project.project_id', '$$info.project_id'] } } }, 0] } } }," +
                      "{ $getField: {" +
                        "field: 'file_status'," +
                        "input: {" +
                          "$arrayElemAt: [" +
                            "{ $filter: {" +
                              "input: '$job_files_info'," +
                              "as: 'info'," +
                              "cond: { $eq: ['$$project.project_id', '$$info.project_id'] } } }, 0] } } }," +
                      "{ $getField: {" +
                        "field: 'file_status'," +
                        "input: {" +
                          "$arrayElemAt: [" +
                            "{ $filter: {" +
                              "input: '$stt_files_info'," +
                              "as: 'info'," +
                              "cond: { $eq: ['$$project.project_id', '$$info.project_id'] } } }, 0] } } }] }," +
                  "initialValue: { PENDING: 0, PROGRESS: 0, SUCCESS: 0, ERROR: 0 }," +
                    "in: {" +
                      "PENDING: {" +
                        "$cond: {" +
                          "if: { $eq: ['$$this.key', 'PENDING'] }," +
                          "then: { $add: ['$$value.PENDING', '$$this.value'] }," +
                          "else: '$$value.PENDING' } }," +
                      "PROGRESS: {" +
                        "$cond: {" +
                          "if: { $eq: ['$$this.key', 'PROGRESS'] }," +
                          "then: { $add: ['$$value.PROGRESS', '$$this.value'] }," +
                          "else: '$$value.PROGRESS' } }," +
                      "SUCCESS: {" +
                        "$cond: {" +
                          "if: { $eq: ['$$this.key', 'SUCCESS'] }," +
                          "then: { $add: ['$$value.SUCCESS', '$$this.value'] }," +
                          "else: '$$value.SUCCESS' } }," +
                      "ERROR: {" +
                        "$cond: {" +
                          "if: { $eq: ['$$this.key', 'ERROR'] }," +
                          "then: { $add: ['$$value.ERROR', '$$this.value'] }," +
                          "else: '$$value.ERROR' } } } } }," +
              "org_count: {" +
                "$sum: [" +
                  "{ $getField: {" +
                    "field: 'count'," +
                    "input: {" +
                      "$arrayElemAt: [" +
                        "{ $filter: {" +
                          "input: '$speaker_files_count'," +
                          "as: 'info'," +
                          "cond: { $eq: ['$$project.project_id', '$$info.project_id'] } } }, 0] } } }," +
                  "{ $getField: {" +
                    "field: 'count'," +
                    "input: {" +
                      "$arrayElemAt: [" +
                        "{ $filter: {" +
                          "input: '$job_files_count'," +
                          "as: 'info'," +
                          "cond: { $eq: ['$$project.project_id', '$$info.project_id'] } } }, 0] } } }," +
                  "{ $getField: {" +
                    "field: 'count'," +
                    "input: {" +
                      "$arrayElemAt: [" +
                        "{ $filter: {" +
                          "input: '$stt_files_count'," +
                          "as: 'info'," +
                          "cond: { $eq: ['$$project.project_id', '$$info.project_id'] } } }, 0] } } }] } }," +
            "project_status: {" +
              "info: {" +
                "$getField: {" +
                  "field: 'work_status'," +
                  "input: {" +
                    "$arrayElemAt: [" +
                      "{ $filter: {" +
                        "input: '$works_info'," +
                        "as: 'info'," +
                        "cond: { $eq: ['$$project.project_id', '$$info.project_id'] } } }, 0] } } }," +
              "org_count: {" +
                "$getField: {" +
                  "field: 'count'," +
                  "input: {" +
                    "$arrayElemAt: [" +
                      "{ $filter: {" +
                        "input: '$works_count'," +
                        "as: 'info'," +
                        "cond: { $eq: ['$$project.project_id', '$$info.project_id'] } } }, 0] } } } }," +
            "post_status: '$$project.post_status'," +
            "created: '$$project.created' } } } } }",
    "{ $project: { project_count: 1, projects: 1 } }",
    "{ $facet: {" +
      "project_count: [" +
        "{ $project: { value: '$project_count' } }," +
        "{ $unwind: '$value' }]," +
      "projects: [" +
        "{ $project: { projects: 1 } }," +
        "{ $unwind: '$projects' }," +
        "{ $replaceRoot: { newRoot: '$projects' } }," +
        "{ $addFields: {" +
          "project_status: {" +
            "info: {" +
              "$arrayToObject: {" +
                "$map: {" +
                  "input: '$project_status.info'," +
                  "in: { k: '$$this.key', v: '$$this.value' } } } }," +
            "curr_count: {" +
              "$reduce: {" +
                "input: '$project_status.info'," +
                "initialValue: 0," +
                "in: { $sum: ['$$value', '$$this.value'] } } } } } }," +
        "{ $addFields: {" +
          "first_progress: {" +
            "$concat: [" +
              "{ $toString: {" +
                "$ifNull: [" +
                  "{ $cond: {" +
                    "if: { $eq: ['$project_status.curr_count', '$jobs_count'] }," +
                    "then: '$jobs_count'," +
                    "else: '$project_status.curr_count' } }, 0] } }," +
              "'/'," +
              "{ $toString: '$jobs_count' }] }," +
          "second_progress: {" +
            "$concat: [" +
              "{ $toString: {" +
                "$ifNull: [" +
                  "{ $sum: [" +
                    "'$project_status.info.IS'," +
                    "'$project_status.info.IC'," +
                    "'$project_status.info.NU'," +
                    "'$project_status.info.CM'] }, 0] } }," +
              "'/'," +
              "{ $toString: '$jobs_count' }]}," +
          "completed_progress: {" +
            "$concat: [" +
              "{ $toString: {" +
                "$ifNull: [{ $sum: ['$project_status.info.NU', '$project_status.info.CM'] }, 0] } }," +
                "'/'," +
                "{ $toString: '$jobs_count' }] }," +
          "upload_status: {" +
            "$cond: {" +
              "if: { $gt: ['$upload_status.info.ERROR', 0] }," +
              "then: 'Error'," +
              "else: {" +
                "$cond: {" +
                  "if: { $eq: ['$upload_status.info.SUCCESS', '$upload_status.org_count'] }," +
                  "then: 'Success'," +
                  "else: { $concat: [{ $toString: '$upload_status.info.SUCCESS' }, '/', { $toString: '$upload_status.org_count' }] }}}}}," +
          "project_status: {" +
            "$cond: {" +
              "if: { $eq: [{ $sum: ['$project_status.info.CM', '$project_status.info.NU'] }, '$jobs_count'] }," +
              "then: '완료'," +
              "else: {" +
                "$cond: {" +
                  "if: { $eq: ['$project_status.info', null] }," +
                  "then: '대기중'," +
                  "else: '진행중' } } } } } }," +
        "{ $addFields: {" +
          "project_status: {" +
            "$cond: {" +
              "if: { $ne: ['$upload_status', 'Success'] }," +
              "then: '-'," +
              "else: '$project_status' } }," +
          "post_status: {" +
            "$cond: {" +
              "if: { $ne: ['$upload_status', 'Success'] }," +
              "then: '-'," +
              "else: '$post_status' } } } }," +
        "{ $match: {" +
          "$expr: {" +
            "$and: [" +
              "{ $or: [ { $eq: [?5, null] }, { $ne: ['$post_status', '-']} ]}," +
              "{ $or: [ { $eq: [?4, null]}, { $ne: ['$project_status', '-']} ]} ] } } }," +
        "{ $project: { jobs_count: 0 } }," +
        "{ $match: {" +
          "$expr: {" +
            "$and: [" +
              "{ $cond: {" +
                "if: { $eq: [?3, null] }," +
                "then: true," +
                "else: {" +
                  "$cond: {" +
                    "if: { $eq: [?3, 'UL_ST_SUCCESS'] }," +
                    "then: { $eq: ['$upload_status', 'Success'] }," +
                    "else: {" +
                      "$cond: {" +
                        "if: { $eq: [?3, 'UL_ST_ERROR'] }," +
                        "then: { $eq: ['$upload_status', 'Error'] }," +
                        "else: { $and: [{ $ne: ['$upload_status', 'Success'] }, { $ne: ['$upload_status', 'Error'] }] } } } } } } }," +
              "{ $cond: {" +
                "if: { $eq: [?4, null] }," +
                "then: true," +
                "else: {" +
                  "$cond: {" +
                    "if: { $eq: [?4, 'PJ_ST_PENDING'] }," +
                    "then: { $eq: ['$project_status', '대기중'] }," +
                    "else: {" +
                      "$cond: {" +
                        "if: { $eq: [?4, 'PJ_ST_WORKING'] }," +
                        "then: { $eq: ['$project_status', '진행중'] }," +
                        "else: { $eq: ['$project_status', '완료'] } } } } } } }] } } }] } }",
    "{ $facet: {" +
      "project_count: [" +
        "{ $unwind: '$project_count' }," +
        "{ $replaceRoot: { newRoot: '$project_count' } }]," +
      "total_page: [{ $project: { value: { $ceil: { $divide: [{ $size: '$projects' }, 10] } } } }]," +
      "result_count: [{ $project: { value: { $size: '$projects' } } }]," +
      "projects: [" +
        "{ $unwind: '$projects' }," +
        "{ $replaceRoot: { newRoot: '$projects' } }," +
        "{ $sort: { project_name: -1 } }," +
        "{ $skip: ?10 }," +
        "{ $limit: ?11 }] } }",
    "{ $project: {" +
      "project_count: { $arrayElemAt: ['$project_count.value', 0] }," +
      "total_page: { $arrayElemAt: ['$total_page.value', 0] }," +
      "result_count: { $arrayElemAt: ['$result_count.value', 0] }," +
      "projects: 1 } }"
  })
  AdminProjectListResponse findProjectList(String clientId,
                                           ObjectId workspaceId,
                                           String toolType,
                                           String uploadStatus,
                                           String projectStatus,
                                           String postStatus,
                                           Pattern query,
                                           LocalDate startDate,
                                           LocalDate endDate,
                                           Boolean isDeleted,
                                           int offset,
                                           int limit);

  @Aggregation(pipeline = {
    "{ $match: { project_id: ?2 } }",
    "{ $project: { jobs: 1 } }",
    "{ $unwind: '$jobs' }",
    "{ $replaceRoot: { newRoot: '$jobs' } }",
    "{ $project: { job_id: 1, job_files: 1 } }",
    "{ $unwind: '$job_files' }",
    "{ $match: { 'job_files.file_name': ?3, 'job_files.file_status': 'PENDING' } }",
    "{ $project: { job_id: 1, job_file: '$job_files' } }"
  })
  JobFileForProjectFileUploadValue findJobFileForUpload(String clientId,
                                                        ObjectId workspaceId,
                                                        ObjectId projectId,
                                                        String fileName);

  @Aggregation(pipeline = {
    "{ $match: { project_id: ?2 } }",
    "{ $project: { jobs: 1 } }",
    "{ $unwind: '$jobs' }",
    "{ $replaceRoot: { newRoot: '$jobs' } }",
    "{ $project: { job_id: 1, job_files: 1 } }",
    "{ $unwind: '$job_files' }",
    "{ $match: { 'job_files.file_id': ?3, 'job_files.file_name': ?4, 'job_files.file_status': 'ERROR' } }",
    "{ $project: { job_id: 1, job_file: '$job_files' } }"
  })
  JobFileForProjectFileUploadValue findJobFileForReUpload(String clientId,
                                                          ObjectId workspaceId,
                                                          ObjectId projectId,
                                                          ObjectId fileId,
                                                          String fileName);

  @Aggregation(pipeline = {
    "{ $match: { project_id: ?2 } }",
    "{ $facet: {" +
      "project_name: [{ $project: { project_name: 1 } }]," +
      "speaker_files: [" +
        "{ $project: { speakers: 1 } }," +
        "{ $unwind: '$speakers' }," +
        "{ $replaceRoot: { newRoot: '$speakers' } }," +
        "{ $project: {" +
          "file_id: '$speaker_id'," +
          "file_name: '$thumbnail_file_name'," +
          "file_status: '$thumbnail_file_status'," +
          "file_size: '$thumbnail_file_size'," +
          "file_type: 'SPEAKER' } }]," +
      "job_files: [" +
        "{ $project: { jobs: 1 } }," +
        "{ $unwind: '$jobs' }," +
        "{ $replaceRoot: { newRoot: '$jobs' } }," +
        "{ $project: { job_files: 1 } }," +
        "{ $unwind: '$job_files' }," +
        "{ $replaceRoot: { newRoot: '$job_files' } }," +
        "{ $project: {" +
          "file_id: { $toString: '$file_id' }," +
          "file_name: 1," +
          "file_size: 1," +
          "file_status: 1," +
          "file_type: 'JOB' } }]," +
      "stt_files: [" +
        "{ $project: { jobs: 1 } }," +
        "{ $unwind: '$jobs' }," +
        "{ $replaceRoot: { newRoot: '$jobs' } }," +
        "{ $project: { job_id: 1 } }," +
        "{ $lookup: {" +
          "from: 'engine'," +
          "localField: 'job_id'," +
          "foreignField: 'job_id'," +
          "as: 'engine_info' } }," +
        "{ $project: { engine_info: 1 } }," +
        "{ $unwind: '$engine_info' }," +
        "{ $replaceRoot: { newRoot: '$engine_info' } }," +
        "{ $project: { _id: 0, stt: 1 } }," +
        "{ $unwind: '$stt' }," +
        "{ $replaceRoot: { newRoot: '$stt' } }," +
        "{ $project: {" +
          "file_id: { $toString: '$stt_id' }," +
          "file_name: 1," +
          "file_status: 1," +
          "file_size: 1," +
          "file_type: 'STT' } }]} }",
    "{ $facet: {" +
      "project_name: [{ $project: { project_name: 1 } }]," +
      "total_count: [" +
        "{ $project: {" +
          "total_count: {" +
            "$sum: [" +
              "{ $size: '$speaker_files' }," +
              "{ $size: '$job_files' }," +
              "{ $size: '$stt_files' }] } } }]," +
      "success_count: [" +
        "{ $project: {" +
          "success_count: {" +
            "$sum: [" +
              "{ $size: {" +
                "$filter: {" +
                  "input: '$speaker_files'," +
                  "cond: { $eq: ['$$this.file_status', 'SUCCESS'] } } } }," +
              "{ $size: {" +
                "$filter: {" +
                  "input: '$job_files'," +
                  "cond: { $eq: ['$$this.file_status', 'SUCCESS'] } } } }," +
              "{ $size: {" +
                "$filter: {" +
                  "input: '$stt_files'," +
                  "cond: { $eq: ['$$this.file_status', 'SUCCESS'] } } } }] } } }]," +
      "error_count: [" +
        "{ $project: {" +
          "error_count: {" +
            "$sum: [" +
              "{ $size: {" +
                "$filter: {" +
                  "input: '$speaker_files'," +
                  "cond: { $eq: ['$$this.file_status', 'ERROR'] } } } }," +
              "{ $size: {" +
                "$filter: {" +
                  "input: '$job_files'," +
                  "cond: { $eq: ['$$this.file_status', 'ERROR'] } } } }," +
              "{ $size: {" +
                "$filter: {" +
                  "input: '$stt_files'," +
                  "cond: { $eq: ['$$this.file_status', 'ERROR'] } } } }] } } }]," +
      "upload_infos: [" +
        "{ $project: { upload_infos: { $concatArrays: ['$speaker_files', '$job_files', '$stt_files'] } } }," +
        "{ $unwind: '$upload_infos' }," +
        "{ $replaceRoot: { newRoot: '$upload_infos' } }," +
        "{ $addFields: {" +
          "file_status: {" +
            "$cond: {" +
              "if: { $eq: ['$file_status', 'ERROR'] }," +
              "then: -1," +
              "else: {" +
                "$cond: {" +
                  "if: { $eq: ['$file_status', 'PROGRESS'] }," +
                  "then: 0," +
                  "else: {" +
                    "$cond: {" +
                      "if: { $eq: ['$file_status', 'PENDING'] }," +
                      "then: 1," +
                      "else: 2 } } } } } } } }," +
        "{ $sort: { file_status: 1, file_name: 1 } }," +
        "{ $skip: ?3 }," +
        "{ $limit: ?4 }," +
        "{ $addFields: {" +
          "file_size: { $concat: [{ $toString: { $ceil: { $divide: ['$file_size', { $multiply: [1024, 1024] }] } } }, 'MB'] }," +
          "file_status: {" +
            "$cond: {" +
              "if: { $eq: ['$file_status', -1] }," +
              "then: 'Error'," +
              "else: {" +
                "$cond: {" +
                  "if: { $eq: ['$file_status', 0] }," +
                  "then: 'Progress'," +
                  "else: {" +
                    "$cond: {" +
                      "if: { $eq: ['$file_status', 1] }," +
                      "then: 'Pending'," +
                      "else: 'Success' } } } } } } } }] } }",
    "{ $project: {" +
      "project_name: { $arrayElemAt: [{ $arrayElemAt: ['$project_name.project_name.project_name', 0] }, 0] }," +
      "total_count: {" +
        "$concat: [" +
          "{ $toString: {" +
            "$cond: {" +
              "if: { $eq: [{ $size: '$total_count' }, 0] }," +
              "then: 0," +
              "else: { $arrayElemAt: ['$total_count.total_count', 0] } } } }, '개'] }," +
      "success_count: {" +
        "$concat: [" +
          "{ $toString: {" +
            "$cond: {" +
              "if: { $eq: [{ $size: '$success_count' }, 0] }," +
              "then: 0," +
              "else: { $arrayElemAt: ['$success_count.success_count', 0] } } } }, '개'] }," +
      "error_count: {" +
        "$concat: [" +
          "{ $toString: {" +
            "$cond: {" +
              "if: { $eq: [{ $size: '$error_count' }, 0] }," +
              "then: 0," +
              "else: { $arrayElemAt: ['$error_count.error_count', 0] } } } }, '개'] }," +
      "total_page: {" +
        "$cond: {" +
          "if: { $eq: [{ $size: '$total_count' }, 0] }," +
          "then: 0," +
          "else: {" +
            "$cond: {" +
              "if: { $eq: [{ $mod: [{ $arrayElemAt: ['$total_count.total_count', 0] }, 10] }, 0] }," +
              "then: { $divide: [{ $arrayElemAt: ['$total_count.total_count', 0] }, 10] }," +
              "else: { $add: [{ $trunc: { $divide: [{ $arrayElemAt: ['$total_count.total_count', 0] }, 10] } }, 1] } } } } }," +
      "upload_infos: 1 } }"
  })
  AdminProjectFileListResponse findFileList(String clientId,
                                            ObjectId workspaceId,
                                            ObjectId projectId,
                                            int offset,
                                            int limit);

  @Aggregation(pipeline = {
    "{ $match: { project_id: ?2 } }",
    "{ $project: { jobs: 1, speakers: 1 } }",
    "{ $facet: {" +
      "job_upload_result: [" +
        "{ $project: { jobs: 1 } }," +
        "{ $unwind: '$jobs' }," +
        "{ $replaceRoot: { newRoot: '$jobs' } }," +
        "{ $project: { job_id: 1, job_files: 1 } }," +
        "{ $lookup: {" +
          "from: 'engine'," +
          "localField: 'job_id'," +
          "foreignField: 'job_id'," +
          "as: 'engine_info' } }," +
        "{ $addFields: {" +
          "job_file_result: {" +
            "$allElementsTrue: {" +
            "$map: {" +
              "input: '$job_files'," +
              "as: 'job_file'," +
              "in: { $eq: ['$$job_file.file_status', 'SUCCESS'] } } } }," +
          "engine_result: {" +
            "$allElementsTrue: {" +
            "$map: {" +
              "input: {" +
                "$getField: {" +
                    "field: 'stt'," +
                    "input: { $arrayElemAt: ['$engine_info', 0] } } }," +
              "as: 'stt_info'," +
              "in: { $eq: ['$$stt_info.file_status', 'SUCCESS'] } } } } } }," +
        "{ $match: { $expr: { $or: [{ $eq: ['$job_file_result', false] }, { $eq: ['$engine_result', false] }] } } }," +
        "{ $count: 'result' }]," +
      "speaker_upload_result: [" +
        "{ $project: { speakers: 1 } }," +
        "{ $unwind: '$speakers' }," +
        "{ $replaceRoot: { newRoot: '$speakers' } }," +
        "{ $match: { $expr: { $ne: ['$thumbnail_file_status', 'SUCCESS'] } } }," +
        "{ $count: 'result' }] } }",
    "{ $project: {" +
      "job_upload_result: { $cond: { if: { $eq: [{ $size: '$job_upload_result' }, 0] }, then: 0, else: { $arrayElemAt: ['$job_upload_result.result', 0] } } }," +
      "speaker_upload_result: { $cond: { if: { $eq: [{ $size: '$speaker_upload_result' }, 0] }, then: 0, else: { $arrayElemAt: ['$speaker_upload_result.result', 0] } } } } }"
  })
  UploadResultNotSuccessValue findUploadResultNotSuccess(String clientId, ObjectId workspaceId, ObjectId projectId);

  @Aggregation(pipeline = {
          "{ $match: { 'project_id': ?0 } }"
          ,"{ $project: { client_ctg_id: 1 } }"
  })
  ProjectClientCtgValue getClientCtgIdByProjectId(ObjectId projectId);

  @Aggregation(pipeline = {
    "{$match: { project_id: ?0 }}"
    ,"{$facet: {" +
      "is_access: [ " + // 해당 프로젝트에 접근 가능한지 확인
        "{$match: {" +
          "$or: [" +
            // worker
            "{$and: [" +
              "{ $expr: { $eq: [?1, 'W'] } }" +
              ",{$or: [" +
                "{'jobs': { $elemMatch: { works: null } }}" + // 아직 작업 시작한 건이 아무것도 없을 때
                ",{'jobs.works': {" +
                  "$elemMatch: {" +
                    "$or: [" +
                      "{ worker_id: null, work_status: null }" +
                      ",{worker_id: ?2" +
                      ",work_status: { $in: ['RD', 'AS', 'RJ', 'RQ'] }}" +
                    "]" +
                  "}" +
                "}}" +
              "]}" +
            "]}" +
            // checker
            ",{$and: [" +
              "{ $expr: { $eq: [?1, 'C'] } }" +
              ",{'jobs.works': {" +
                "$elemMatch: {" +
                  "$or: [" +
                    "{ checker_id: null, work_status: 'RQ'}" +
                    ",{checker_id: ?2" +
                    ",work_status: { $in: ['RQ', 'IS', 'IC', 'IM', 'CM', 'NU'] }}" +
                  "]" +
                "}" +
              "}}" +
            "]}" +
              // viewer
            ",{$and: [" +
              "{ $expr: { $eq: [?1, 'V'] } }" +
              ",{'jobs.works': {" +
                "$elemMatch: {" +
                  "work_status: { $in: ['CM', 'NU'] }" +
                "}" +
              "}}" +
            "]}" +
          "]" +
        "}}" + // end of $match
      "]" + // end of is_access
      ",project_name: [ {$project: {_id: 0, project_name: '$project_name'}} ]" +
    "}}" // end of $facet
    ,"{$project: {" +
      "is_access: {" +
        "$cond: {" +
          "if: {" +
            "$and: [" +
              "{$ne: ['$is_access', null]}" +
              ",{$ne: [{$size: '$is_access'}, 0]}" +
            "]" +
          "}" +
          ",then: true" +
          ",else: false" +
        "}}" +
      ",project_name: {$arrayElemAt: ['$project_name.project_name', 0]}" +
    "}}"
  })
  ProjectInfoResponse findProjectInfo(ObjectId projectId, String userType, ObjectId userId);

  @Aggregation(pipeline = {
    "{ $match: { project_id: ?2 } }",
    "{ $project: { jobs: 1 } }",
    "{ $unwind: '$jobs' }",
    "{ $replaceRoot: { newRoot: '$jobs' } }",
    "{ $match: {" +
      "$expr: {" +
        "$cond: {" +
          "if: { $eq: ['$job_name', { $arrayElemAt: [{ $split: [?3, '_'] }, 2] }] }," +
          "then: true," +
          "else: false } } } }",
    "{ $lookup: {" +
      "from: 'engine'," +
      "localField: 'job_id'," +
      "foreignField: 'job_id'," +
      "as: 'engine' } }",
    "{ $project: { engine: 1 } }",
    "{ $unwind: '$engine' }",
    "{ $replaceRoot: { newRoot: '$engine' } }",
    "{ $project: { _id: 0, engine_id: 1, stt: 1 } }",
    "{ $match: { 'stt.file_name': ?3 } }",
    "{ $facet: {" +
      "engine_id: [{ $project: { engine_id: 1 } }]," +
      "stt_id: [" +
      "{ $project: { stt: 1 } }," +
      "{ $unwind: '$stt' }," +
      "{ $replaceRoot: { newRoot: '$stt' } }," +
      "{ $match: { file_name: ?3, file_status: 'PENDING' } }," +
      "{ $project: { stt_id: 1 } }] } }",
    "{ $project: {" +
      "engine_id: { $arrayElemAt: ['$engine_id.engine_id', 0] }," +
      "stt_id: { $arrayElemAt: ['$stt_id.stt_id', 0] } } }"
  })
  EngineForUploadValue findEngineForUpload(String clientId,
                                           ObjectId workspaceId,
                                           ObjectId projectId,
                                           String fileName);

  @Aggregation(pipeline = {
    "{ $match: { project_id: ?2 } }",
    "{ $project: { speakers: 1 } }",
    "{ $unwind: '$speakers' }",
    "{ $replaceRoot: { newRoot: '$speakers' } }",
    "{ $match: { thumbnail_file_name: ?3, thumbnail_file_status: 'PENDING' } }"
  })
  SpeakerForUploadValue findSpeakerForUpload(String clientId,
                                             ObjectId workspaceId,
                                             ObjectId projectId,
                                             String fileName);

  @Aggregation(pipeline = {
    "{ $match: { project_id: ?2 } }",
    "{ $project: { speakers: 1 } }",
    "{ $unwind: '$speakers' }",
    "{ $replaceRoot: { newRoot: '$speakers' } }",
    "{ $match: { speaker_id: ?3, thumbnail_file_name: ?4, thumbnail_file_status: 'ERROR' } }"
  })
  SpeakerForUploadValue findSpeakerForReUpload(String clientId,
                                               ObjectId workspaceId,
                                               ObjectId projectId,
                                               String speakerId,
                                               String fileName);

  @Aggregation({
    "{ $match: { workspace_id: ?1, project_id: ?2 } }",
    "{ $unwind: '$jobs' }",
    "{ $replaceRoot: {" +
      "newRoot: {" +
        "$mergeObjects: [" +
          "{ project_name: '$project_name'," +
            "topic: '$topic'," +
            "speakers: '$speakers'," +
            "relationship: '$relationship'," +
            "client_ctg_id: '$client_ctg_id' }," +
          "'$jobs'] } } }",
    "{ $match: {" +
      "$expr: {" +
        "$cond: {" +
          "if: {" +
            "$not: {" +
              "$or: [" +
                "{ $eq: [{ $type: '$works' }, 'missing'] }," +
                "{ $eq: ['$works', null] }] } }," +
          "then: { $in: [{ $getField: { input: { $first: '$works' }, field: 'work_status' } }, ?3] }," +
          "else: false } } } }",
    "{ $lookup: {" +
      "from: 'client_category'," +
      "localField: 'client_ctg_id'," +
      "foreignField: 'client_ctg_id'," +
      "as: 'category_info' } }",
      "{ $facet: {" +
        "project_info: [" +
          "{ $project: {" +
            "project_name: 1," +
            "category_info: {" +
              "$reduce: {" +
                "input: { $first: '$category_info.categories' }," +
                "initialValue: []," +
                "in: {" +
                  "$concatArrays: [" +
                    "'$$value'," +
                    "[{ ctg_id: '$$this.ctg_id', ctg_name: '$$this.ctg_name' }]," +
                    "{ $reduce: {" +
                      "input: '$$this.ctg_depth'," +
                      "initialValue: []," +
                      "in: {" +
                        "$concatArrays: [" +
                          "'$$value'," +
                          "[{ ctg_id: '$$this.ctg_id', ctg_name: '$$this.ctg_name' }]," +
                          "{ $reduce: {" +
                            "input: '$$this.ctg_depth'," +
                            "initialValue: []," +
                            "in: {" +
                              "$concatArrays: [" +
                              "'$$value'," +
                              "[{ ctg_id: '$$this.ctg_id', ctg_name: '$$this.ctg_name' }]," +
                              "{ $reduce: {" +
                                "input: '$$this.ctg_depth'," +
                                "initialValue: []," +
                                "in: {" +
                                  "$concatArrays: [" +
                                    "'$$value'," +
                                    "[{ ctg_id: '$$this.ctg_id', ctg_name: '$$this.ctg_name' }]] } } }] } } }] } } }] } } } } }]," +
              "targets: [{ $project: { job_id: 1, job_name: 1 } }]," +
              "mainset_info: [" +
                "{ $group: {" +
                  "_id: null," +
                  "project_name: { $first: '$project_name' }," +
                  "topic: { $first: '$topic' }," +
                  "speakers: { $first: '$speakers' }," +
                  "relation: { $first: '$relationship' }," +
                  "job_files: { $push: '$job_files' } } }," +
                "{ $project: { _id: 0 } }," +
                "{ $addFields: {" +
                  "job_files: {" +
                    "$reduce: {" +
                      "input: '$job_files'," +
                      "initialValue: []," +
                      "in: { $concatArrays: ['$$value', '$$this'] } } } } }," +
                "{ $project: {" +
                  "mainset_id: '$project_name'," +
                  "mainset_topic: {" +
                    "$switch: {" +
                      "branches: [" +
                        "{ case: { $eq: ['$topic', 'M001'] }, then: '가족/학교/대인관계' }," +
                        "{ case: { $eq: ['$topic', 'M002'] }, then: '연애/결혼/육아' }," +
                        "{ case: { $eq: ['$topic', 'M003'] }, then: '취업/진로/적성/직장' }," +
                        "{ case: { $eq: ['$topic', 'M004'] }, then: '취미/엔터' }," +
                        "{ case: { $eq: ['$topic', 'M005'] }, then: '건강/자기관리/스트레스' }," +
                        "{ case: { $eq: ['$topic', 'M006'] }, then: '기타' }," +
                        "{ case: { $eq: ['$topic', 'M007'] }, then: '영화/드라마/음악(컨텐츠)' }," +
                        "{ case: { $eq: ['$topic', 'M008'] }, then: '연극/뮤지컬/콘서트(공연)' }," +
                        "{ case: { $eq: ['$topic', 'M009'] }, then: '전시회/박물관(전시)' }," +
                        "{ case: { $eq: ['$topic', 'M010'] }, then: '책/독서' }," +
                        "{ case: { $eq: ['$topic', 'M011'] }, then: '스포츠/레저/운동/헬스' }," +
                        "{ case: { $eq: ['$topic', 'M012'] }, then: '패션/뷰티' }," +
                        "{ case: { $eq: ['$topic', 'M013'] }, then: '음식/음료/요리' }," +
                        "{ case: { $eq: ['$topic', 'M014'] }, then: '반려동물/반려식물' }," +
                        "{ case: { $eq: ['$topic', 'M015'] }, then: '여행계획' }," +
                        "{ case: { $eq: ['$topic', 'M016'] }, then: '여행일반' }," +
                        "{ case: { $eq: ['$topic', 'M017'] }, then: '역사/정치/경제/사회' }," +
                        "{ case: { $eq: ['$topic', 'M018'] }, then: '생활(자취/이사/소비)' }] } }," +
                      "mainset_path: {" +
                        "$let: {" +
                          "vars: { path: { $first: '$job_files.file_path' } }," +
                          "in: { $substr: ['$$path', 0, { $indexOfCP: ['$$path', '/job'] }] } } }," +
                      "speaker_info: {" +
                        "speaker_ids: '$speakers.speaker_id'," +
                        "speaker_profiles: {" +
                          "$map: {" +
                          "input: '$speakers'," +
                          "in: {" +
                            "speaker_id: '$$this.speaker_id'," +
                            "gender: '$$this.gender'," +
                            "age: '$$this.age' ," +
                            "role: '$$this.role'" +
                          "} } }," +
                        "relation: '$relation' }," +
                      "camera_info: {" +
                        "$let: {" +
                          "vars: {" +
                            "camera_ids: {" +
                              "$reduce: {" +
                              "input: { $setUnion: '$job_files.file_info.angle' }," +
                              "initialValue: []," +
                              "in: {" +
                                "$let: {" +
                                  "vars: { angle: '$$this' }," +
                                  "in: {" +
                                    "$concatArrays: [" +
                                      "'$$value'," +
                                      "{ $reduce: {" +
                                        "input: {" +
                                          "$setUnion: {" +
                                            "$reduce: {" +
                                              "input: '$job_files'," +
                                              "initialValue: []," +
                                              "in: {" +
                                                "$cond: {" +
                                                  "if: { $eq: ['$$this.file_ext', 'mp4'] }," +
                                                  "then: { $concatArrays: ['$$value', [{ $toUpper: { $arrayElemAt: [{ $split: ['$$this.file_name', '_'] }, 3] } }]] }," +
                                                  "else: '$$value' } } } } }," +
                                        "initialValue: []," +
                                        "in: {" +
                                          "$cond: {" +
                                            "if: {" +
                                              "$or: [" +
                                                "{ $and: [" +
                                                  "{ $eq: ['$$angle', 'FS'] }," +
                                                  "{ $eq: ['$$this', 'MIX'] }] }," +
                                                "{ $and: [" +
                                                  "{ $ne: ['$$angle', 'FS'] }," +
                                                  "{ $ne: ['$$this', 'MIX'] }] }] }," +
                                            "then: { $concatArrays: ['$$value', [{ $concat: ['$$angle', '_', '$$this'] }]] }," +
                                            "else: '$$value' } } } }] } } } } } }," +
                              "in: {" +
                                "camera_ids: '$$camera_ids'," +
                                "camera_profiles: {" +
                                  "$map: {" +
                                    "input: '$$camera_ids'," +
                                    "in: {" +
                                      "$let: {" +
                                        "vars: { result: { $split: ['$$this', '_'] } }," +
                                        "in: {" +
                                          "camera_id: '$$this'," +
                                          "camera_angle: { $arrayElemAt: ['$$result', 0] }," +
                                          "target_speaker: { $arrayElemAt: ['$$result', 1] } } } } } } } } }," +
                      "file_info: {" +
                        "$let: {" +
                          "vars: {" +
                            "video_files: {" +
                              "$reduce: {" +
                                "input: '$job_files'," +
                                "initialValue: []," +
                                "in: {" +
                                  "$cond: {" +
                                    "if: { $eq: ['$$this.file_ext', 'mp4'] }," +
                                    "then: {" +
                                      "$concatArrays: [" +
                                        "'$$value'," +
                                        "[{" +
                                          "video_id: '$$this.file_name'," +
                                          "video_duration: { $concat: ['$$this.file_info.duration', '00'] }," +
                                          "video_format: '$$this.file_ext'," +
                                          "fps: { $concat: [{ $toString: '$$this.file_info.fps' }, '.00'] }," +
                                          "resolution: { $concat: [{ $toString: '$$this.file_info.width' }, 'x', { $toString: '$$this.file_info.height' }] } }]] }," +
                                    "else: '$$value' } } } }," +
                              "audio_files: {" +
                                "$reduce: {" +
                                  "input: '$job_files'," +
                                  "initialValue: []," +
                                  "in: {" +
                                    "$cond: {" +
                                      "if: { $eq: ['$$this.file_ext', 'wav'] }," +
                                      "then: {" +
                                        "$concatArrays: [" +
                                          "'$$value'," +
                                          "[{" +
                                            "audio_id: '$$this.file_name'," +
                                            "audio_duration: { $concat: ['$$this.file_info.duration', '00'] }," +
                                            "audio_format: '$$this.file_ext'," +
                                            "audio_sample_rate: '$$this.file_info.sample_rate'," +
                                            "audio_channel: '$$this.file_info.channel' }]] }," +
                                      "else: '$$value' } } } } }," +
                          "in: {" +
                            "video_info: {" +
                            "video_file_ids: '$$video_files.video_id'," +
                            "video_files: '$$video_files' }," +
                            "audio_info: {" +
                            "audio_file_ids: '$$audio_files.audio_id'," +
                            "audio_files: '$$audio_files' } } } } } }]," +
              "labeling_info: [" +
                "{ $unwind: '$works' }," +
                "{ $replaceRoot: { newRoot: { $mergeObjects: [{ job_id: '$job_id', job_name: '$job_name', job_files: '$job_files.file_name' }, '$works'] } } }," +
                "{ $unwind: '$labelings' }," +
                "{ $replaceRoot: { newRoot: { $mergeObjects: [{ job_id: '$job_id', job_name: '$job_name', job_files: '$job_files', work_status: '$work_status' }, '$labelings'] } } }," +
                "{ $project: {" +
                  "job_id: 1," +
                  "job_name: 1," +
                  "job_files: 1," +
                  "work_status: 1," +
                  "speaker_id: 1," +
                  "data: { $concatArrays: ['$verbal', '$paraverbal', '$nonverbal'] } } }," +
                "{ $unwind: '$data' }," +
                "{ $replaceRoot: { newRoot: { $mergeObjects: [{ job_id: '$job_id', job_name: '$job_name', job_files: '$job_files', work_status: '$work_status', speaker_id: '$speaker_id' }, '$data'] } } }," +
                "{ $addFields: {" +
                  "start_time: { $divide: ['$start_time', 1000] }," +
                  "end_time: { $divide: ['$end_time', 1000] }," +
                  "org_start_time: '$start_time'," +
                  "org_end_time: '$end_time' } }," +
                  "{ $addFields: {" +
                    "section_id: {" +
                      "$cond: {" +
                        "if: { $ne: [{ $type: '$verbal_id' }, 'missing'] }," +
                        "then: '$verbal_id'," +
                        "else: {" +
                          "$cond: {" +
                            "if: { $ne: [{ $type: '$paraverbal_id' }, 'missing'] }," +
                            "then: '$paraverbal_id'," +
                            "else: '$nonverbal_id' } } } }," +
                    "speaker_id: '$speaker_id'," +
                    "vocal_or_body: {" +
                      "$cond: {" +
                        "if: { $ne: [{ $type: '$nonverbal_id' }, 'missing'] }," +
                        "then: 'body'," +
                        "else: 'vocal' } }," +
                    "start_time: {" +
                      "$let: {" +
                        "vars: {" +
                          "hour: {" +
                            "$cond: {" +
                              "if: { $gte: ['$start_time', 3600] }," +
                              "then: { $toInt: { $divide: ['$start_time', 3600] } }," +
                              "else: 0 } }," +
                          "minute: {" +
                            "$let: {" +
                              "vars: { target: { $mod: ['$start_time', 3600] } }," +
                              "in: {" +
                                "$cond: {" +
                                  "if: { $gte: ['$$target', 60] }," +
                                  "then: { $toInt: { $divide: ['$$target', 60] } }," +
                                  "else: 0 } } } }," +
                          "second: { $toInt: { $mod: [{ $mod: ['$start_time', 3600] }, 60] } }," +
                          "ms: { $mod: ['$org_start_time', 1000] } }," +
                        "in: {" +
                          "$concat: [" +
                            "{ $cond: {" +
                              "if: { $lte: ['$$hour', 9] }," +
                              "then: { $concat: ['0', { $toString: '$$hour' }] }," +
                              "else: { $toString: '$$hour' } } }," +
                            "':'," +
                            "{ $cond: {" +
                              "if: { $lte: ['$$minute', 9] }," +
                              "then: { $concat: ['0', { $toString: '$$minute' }] }," +
                              "else: { $toString: '$$minute' } } }," +
                            "':'," +
                            "{ $cond: {" +
                              "if: { $lte: ['$$second', 9] }," +
                              "then: { $concat: ['0', { $toString: '$$second' }] }," +
                              "else: { $toString: '$$second' } } }," +
                            "'.'," +
                            "{ $let: {" +
                              "vars: { target: { $toString: '$$ms' } }," +
                              "in: {" +
                                "$cond: {" +
                                  "if: { $lt: [{ $strLenCP: '$$target' }, 3] }," +
                                  "then: { $concat: [{ $substr: ['000', 0, { $subtract: [3, { $strLenCP: '$$target' }] }] }, '$$target'] }," +
                                  "else: '$$target' } } } }] } } }," +
                      "end_time: {" +
                        "$let: {" +
                          "vars: {" +
                            "hour: {" +
                              "$cond: {" +
                                "if: { $gte: ['$end_time', 3600] }," +
                                "then: { $toInt: { $divide: ['$end_time', 3600] } }," +
                                "else: 0 } }," +
                            "minute: {" +
                              "$let: {" +
                                "vars: { target: { $mod: ['$end_time', 3600] } }," +
                                "in: {" +
                                  "$cond: {" +
                                    "if: { $gte: ['$$target', 60] }," +
                                    "then: { $toInt: { $divide: ['$$target', 60] } }," +
                                    "else: 0 } } } }," +
                            "second: { $toInt: { $mod: [{ $mod: ['$end_time', 3600] }, 60] } }," +
                            "ms: { $mod: ['$org_end_time', 1000] } }," +
                          "in: {" +
                            "$concat: [" +
                              "{ $cond: {" +
                                "if: { $lte: ['$$hour', 9] }," +
                                "then: { $concat: ['0', { $toString: '$$hour' }] }," +
                                "else: { $toString: '$$hour' } } }," +
                              "':'," +
                              "{ $cond: {" +
                                "if: { $lte: ['$$minute', 9] }," +
                                "then: { $concat: ['0', { $toString: '$$minute' }] }," +
                                "else: { $toString: '$$minute' } } }," +
                              "':'," +
                              "{ $cond: {" +
                                "if: { $lte: ['$$second', 9] }," +
                                "then: { $concat: ['0', { $toString: '$$second' }] }," +
                                "else: { $toString: '$$second' } } }," +
                              "'.'," +
                              "{ $let: {" +
                                "vars: { target: { $toString: '$$ms' } }," +
                                "in: {" +
                                  "$cond: {" +
                                    "if: { $lt: [{ $strLenCP: '$$target' }, 3] }," +
                                    "then: { $concat: ['$$target', { $substr: ['000', 0, { $subtract: [3, { $strLenCP: '$$target' }] }] }] }," +
                                    "else: '$$target' } } } }] } } } } }] } }",
          "{ $facet: {" +
            "project_info: [{ $project: { project_info: 1 } }]," +
            "targets: [{ $project: { targets: 1 } }]," +
            "mainset_info: [{ $project: { mainset_info: 1 } }]," +
            "section: [" +
                "{ $project: { labeling_info: 1 } }," +
                "{ $unwind: '$labeling_info' }," +
                "{ $replaceRoot: { newRoot: '$labeling_info' } }," +
                "{ $project: {" +
                    "job_id: 1," +
                    "job_name: 1," +
                    "section_id: 1," +
                    "speaker_id: 1," +
                    "vocal_or_body: 1," +
                    "start_time: 1," +
                    "end_time: 1 } }," +
                "{ $sort: { start_time: 1 } }," +
                "{ $group: { _id: { job_id: '$job_id', job_name: '$job_name' }, data: { $push: '$$ROOT' } } }]," +
            "information: [" +
              "{ $project: { labeling_info: 1 } }," +
              "{ $unwind: '$labeling_info' }," +
              "{ $replaceRoot: { newRoot: '$labeling_info' } }," +
              "{ $addFields: {" +
                "verbal: {" +
                  "$cond: {" +
                    "if: { $ne: [{ $type: '$verbal_id' }, 'missing'] }," +
                    "then: {" +
                      "section_id: '$section_id'," +
                      "speaker_id: '$speaker_id'," +
                      "channel: '$channel'," +
                      "sentence: '$sentence'," +
                      "sentiment: '$sentiment'," +
                      "emotion: '$emotion'," +
                      "fc_intent: '$fc_intent'," +
                      "bc_intent: '$bc_intent'," +
                      "org_start_time: '$org_start_time'," +
                      "org_end_time: '$org_end_time' }," +
                    "else: '$$REMOVE' } }," +
                "'para-verbal': {" +
                  "$cond: {" +
                    "if: { $ne: [{ $type: '$paraverbal_id' }, 'missing'] }," +
                    "then: {" +
                      "section_id: '$section_id'," +
                      "speaker_id: '$speaker_id'," +
                      "function: '$func'," +
                      "org_start_time: '$org_start_time'," +
                      "org_end_time: '$org_end_time' }," +
                    "else: '$$REMOVE' } }," +
                "'non-verbal': {" +
                  "$cond: {" +
                    "if: { $ne: [{ $type: '$nonverbal_id' }, 'missing'] }," +
                    "then: {" +
                      "section_id: '$section_id'," +
                      "camera_angle: '$angle'," +
                      "body: {" +
                        "gesture: {" +
                          "subject: '$subject'," +
                          "action: {" +
                            "main: '$action'," +
                            "description: '$action_str'}," +
                          "object: '$obj' }," +
                        "function: '$func'," +
                        "emotion: '$emotion' }," +
                      "'non-verbal_only': {" +
                        "intent: '$intent'," +
                        "affective_state: '$affective_state' } }," +
                    "else: '$$REMOVE' } } } }," +
              "{ $group: { _id: { job_id: '$job_id', job_name: '$job_name' }, data: { $push: '$$ROOT' } } }] } }",
          "{ $facet: {" +
            "mainset_info: [{ $project: { mainset_info: 1 } }]," +
            "sets: [" +
              "{ $project: {" +
                "project_name: { $first: { $first: '$project_info.project_info.project_name' } }," +
                "category_info: { $first: { $first: '$project_info.project_info.category_info' } }," +
                "targets: 1," +
                "section: 1," +
                "information: 1 } }," +
              "{ $addFields: {" +
                "verbal_info: {" +
                  "$map: {" +
                    "input: '$information'," +
                    "in: {" +
                      "_id: '$$this._id'," +
                      "data: {" +
                        "$filter: {" +
                          "input: '$$this.data'," +
                          "cond: { $ne: [{ $type: '$$this.verbal' }, 'missing'] } } } } } }," +
                "paraverbal_info: {" +
                  "$map: {" +
                    "input: '$information'," +
                    "in: {" +
                      "_id: '$$this._id'," +
                      "data: {" +
                        "$filter: {" +
                          "input: '$$this.data'," +
                          "cond: { $ne: [{ $type: '$$this.para-verbal' }, 'missing'] } } } } } } } }," +
                "{ $project: {" +
                  "project_name: 1," +
                  "category_info: 1," +
                  "result: {" +
                    "$reduce: {" +
                      "input: { $first: '$targets.targets' }," +
                      "initialValue: []," +
                      "in: {" +
                        "$let: {" +
                          "vars: { target: '$$this' }," +
                          "in: {" +
                            "$concatArrays: [" +
                              "'$$value'," +
                              "[{" +
                                "job_id: '$$target.job_id'," +
                                "job_name: '$$target.job_name'," +
                                "section: {" +
                                  "$filter: {" +
                                    "input: '$section'," +
                                    "cond: { $eq: ['$$this._id.job_id', '$$target.job_id'] } } }," +
                                "information: {" +
                                  "$filter: {" +
                                    "input: '$information'," +
                                    "cond: { $eq: ['$$this._id.job_id', '$$target.job_id'] } } }," +
                                "verbal_info: {" +
                                  "$filter: {" +
                                    "input: '$verbal_info'," +
                                    "cond: { $eq: ['$$this._id.job_id', '$$target.job_id'] } } }," +
                                "paraverbal_info: {" +
                                  "$filter: {" +
                                    "input: '$paraverbal_info'," +
                                    "cond: { $eq: ['$$this._id.job_id', '$$target.job_id'] } } } }]] } } } } } } }," +
                  "{ $unwind: '$result' }," +
                  "{ $replaceRoot: { newRoot: { $mergeObjects: [{ project_name: '$project_name', category_info: '$category_info' }, '$result'] } } }," +
                  "{ $sort: { job_name: 1 } }," +
                  "{ $project: {" +
                    "project_name: 1," +
                    "job_id: 1," +
                    "job_name: 1," +
                    "category_info: 1," +
                    "section: { $first: '$section.data' }," +
                    "information: { $first: '$information.data' }," +
                    "verbal_info: { $first: '$verbal_info.data' }," +
                    "paraverbal_info: { $first: '$paraverbal_info.data' } } }," +
                  "{ $project: {" +
                    "project_name: 1," +
                    "job_id: 1," +
                    "job_name: 1," +
                    "section: 1," +
                    "subset_status: { $first: '$information.work_status' }," +
                    "information: {" +
                      "$mergeObjects: [" +
                        "{ job_files: { $first: '$information.job_files' } }," +
                        "{ verbal: {" +
                          "$map: {" +
                            "input: '$information.verbal'," +
                            "as: 'v'," +
                            "in: {" +
                              "$mergeObjects: [" +
                                "'$$v'," +
                                "{ $let: {" +
                                  "vars: {" +
                                    "result: {" +
                                      "$reduce: {" +
                                        "input: '$category_info'," +
                                        "initialValue: {}," +
                                        "in: {" +
                                          "channel: {" +
                                            "$cond: {" +
                                              "if: { $eq: ['$$v.channel', { $toString: '$$this.ctg_id' }] }," +
                                              "then: '$$this.ctg_name'," +
                                              "else: '$$value.channel' } }," +
                                          "sentiment: {" +
                                            "$cond: {" +
                                              "if: { $eq: ['$$v.sentiment', { $toString: '$$this.ctg_id' }] }," +
                                              "then: '$$this.ctg_name'," +
                                              "else: '$$value.sentiment' } }," +
                                          "emotion: {" +
                                            "$cond: {" +
                                              "if: { $eq: ['$$v.emotion', { $toString: '$$this.ctg_id' }] }," +
                                              "then: '$$this.ctg_name'," +
                                              "else: '$$value.emotion' } }," +
                                          "fc_intent: {" +
                                            "$cond: {" +
                                              "if: { $eq: ['$$v.fc_intent', { $toString: '$$this.ctg_id' }] }," +
                                              "then: '$$this.ctg_name'," +
                                              "else: '$$value.fc_intent' } }," +
                                          "bc_intent: {" +
                                            "$cond: {" +
                                              "if: { $eq: ['$$v.bc_intent', { $toString: '$$this.ctg_id' }] }," +
                                              "then: '$$this.ctg_name'," +
                                              "else: '$$value.bc_intent' } } } } } }," +
                                  "in: '$$result' } }] } } } }," +
                              "{ 'para-verbal': {" +
                                "$map: {" +
                                  "input: '$verbal_info'," +
                                  "as: 'v'," +
                                  "in: {" +
                                    "section_id: '$$v.section_id'," +
                                    "speed: {" +
                                      "$let: {" +
                                        "vars: { targets: { $regexFindAll: { input: '$$v.sentence', regex: ?4 } } }," +
                                        "in: {" +
                                          "$let: {" +
                                            "vars: {" +
                                              "result: {" +
                                                "$round: [" +
                                                  "{ $divide: [" +
                                                    "{ $strLenCP: {" +
                                                      "$cond: {" +
                                                        "if: { $gt: [{ $size: '$$targets' }, 0] }," +
                                                        "then: {" +
                                                          "$reduce: {" +
                                                            "input: '$$targets'," +
                                                            "initialValue: '$$v.sentence'," +
                                                            "in: { $replaceOne: { input: '$$value', find: '$$this.match', replacement: '' } } } }," +
                                                        "else: '$$v.sentence' } } }," +
                                                    "{ $subtract: [" +
                                                      "{ $divide: ['$$v.org_end_time', 1000] }," +
                                                      "{ $divide: ['$$v.org_start_time', 1000] }] }] }, 3] } }," +
                                            "in: {" +
                                              "$let: {" +
                                                "vars: { info: { $split: [{ $toString: '$$result' }, '.'] } }," +
                                                "in: {" +
                                                  "$concat: [" +
                                                    "{ $toString: '$$result' }," +
                                                    "{ $cond: {" +
                                                      "if: { $gt: [{ $size: '$$info' }, 1] }," +
                                                      "then: { $substr: ['000', 0, { $subtract: [3, { $strLenCP: { $toString: { $arrayElemAt: ['$$info', 1] } } }] }] }," +
                                                      "else: '.000' } }] } } } } } } }," +
                                    "'para-verbal_ft': {" +
                                      "$map: {" +
                                        "input: {" +
                                          "$filter: {" +
                                            "input: '$paraverbal_info'," +
                                            "as: 'p'," +
                                            "cond: {" +
                                              "$and: [" +
                                                "{ $eq: ['$$p.speaker_id', '$$v.speaker_id'] }," +
                                                "{ $gte: ['$$p.org_start_time', '$$v.org_start_time'] }," +
                                                "{ $lte: ['$$p.org_end_time', '$$v.org_end_time'] }] } } }," +
                                        "as: 'filtered_p'," +
                                        "in: {" +
                                          "section_id: '$$filtered_p.section_id'," +
                                          "function: {" +
                                            "$getField: {" +
                                              "input: {" +
                                                "$first: {" +
                                                  "$filter: {" +
                                                    "input: '$category_info'," +
                                                    "cond: { $eq: ['$$filtered_p.func', { $toString: '$$this.ctg_id' }] } } } }," +
                                              "field: 'ctg_name' } } } } }," +
                                    "speaker_id: '$$v.speaker_id'," +
                                    "org_start_time: '$$v.org_start_time'," +
                                    "org_end_time: '$$v.org_end_time' } } } }," +
                              "{ 'non-verbal': {" +
                                "$map: {" +
                                  "input: '$information.non-verbal'," +
                                  "as: 'n'," +
                                  "in: {" +
                                    "$mergeObjects: [" +
                                      "'$$n'," +
                                      "{ $let: {" +
                                        "vars: {" +
                                          "result: {" +
                                            "$reduce: {" +
                                              "input: '$category_info'," +
                                              "initialValue: { body: { gesture: { action: { description: '' } } } }," +
                                              "in: {" +
                                                "camera_angle: {" +
                                                  "$cond: {" +
                                                    "if: { $eq: ['$$n.camera_angle', { $toString: '$$this.ctg_id' }] }," +
                                                    "then: '$$this.ctg_name'," +
                                                    "else: '$$value.camera_angle' } }," +
                                                "body: {" +
                                                  "gesture: {" +
                                                    "subject: {" +
                                                      "$cond: {" +
                                                        "if: { $eq: ['$$n.body.gesture.subject', { $toString: '$$this.ctg_id' }] }," +
                                                        "then: '$$this.ctg_name'," +
                                                        "else: '$$value.body.gesture.subject' } }," +
                                                    "action: {" +
                                                      "main: {" +
                                                        "$cond: {" +
                                                          "if: { $eq: ['$$n.body.gesture.action.main', { $toString: '$$this.ctg_id' }] }," +
                                                          "then: '$$this.ctg_name'," +
                                                          "else: '$$value.body.gesture.action.main' } }," +
                                                      "description: {" +
                                                        "$cond: {" +
                                                          "if: { $eq: ['$$n.body.gesture.action.description', { $toString: '$$this.ctg_id' }] }," +
                                                          "then: '$$this.ctg_name'," +
                                                          "else: '$$value.body.gesture.action.description' } } }," +
                                                    "object: {" +
                                                      "$cond: {" +
                                                        "if: { $eq: ['$$n.body.gesture.object', { $toString: '$$this.ctg_id' }] }," +
                                                        "then: '$$this.ctg_name'," +
                                                        "else: '$$value.body.gesture.object' } } }," +
                                                  "function: {" +
                                                    "$cond: {" +
                                                      "if: { $eq: ['$$n.body.function', { $toString: '$$this.ctg_id' }] }," +
                                                      "then: '$$this.ctg_name'," +
                                                      "else: '$$value.body.function' } }," +
                                                  "emotion: {" +
                                                    "$cond: {" +
                                                      "if: { $eq: ['$$n.body.emotion', { $toString: '$$this.ctg_id' }] }," +
                                                      "then: '$$this.ctg_name'," +
                                                      "else: '$$value.body.emotion' } } }," +
                                                "'non-verbal_only': {" +
                                                    "intent: {" +
                                                        "$cond: {" +
                                                            "if: { $eq: ['$$n.non-verbal_only.intent', { $toString: '$$this.ctg_id' }] }," +
                                                            "then: '$$this.ctg_name'," +
                                                            "else: '$$value.non-verbal_only.intent' } }," +
                                                    "affective_state: {" +
                                                        "$cond: {" +
                                                            "if: { $eq: ['$$n.non-verbal_only.affective_state', { $toString: '$$this.ctg_id' }] }," +
                                                            "then: '$$this.ctg_name'," +
                                                            "else: '$$value.non-verbal_only.affective_state' } } } } } } }," +
                                        "in: '$$result' } }] } } } }," +
                              "{ overall: {" +
                                "$map: {" +
                                  "input: '$verbal_info'," +
                                  "as: 'v'," +
                                  "in: {" +
                                    "section_id: '$$v.section_id'," +
                                    "affective_state: {" +
                                      "$getField: {" +
                                        "input: {" +
                                          "$first: {" +
                                            "$filter: {" +
                                              "input: '$category_info'," +
                                              "cond: { $eq: ['$$v.affective_state', { $toString: '$$this.ctg_id' }] }}}}," +
                                        "field: 'ctg_name' } }," +
                                    "vad: {" +
                                      "vp: '$$v.verbal_vad'," +
                                      "vpn: '$$v.overall_vad' } } } } }] } } }," +
                  "{ $addFields: {" +
                    "subset_id: { $concat: ['$project_name', '_', '$job_name'] }," +
                    "video_name_set: {" +
                      "$filter: {" +
                        "input: '$information.job_files'," +
                        "cond: { $eq: [{ $arrayElemAt: [{ $split: [{ $arrayElemAt: [{ $split: ['$$this', '_'] }, 7] }, '.'] }, 1] }, 'mp4'] } } }," +
                    "audio_name_set: {" +
                      "$filter: {" +
                        "input: '$information.job_files'," +
                        "cond: { $eq: [{ $arrayElemAt: [{ $split: [{ $arrayElemAt: [{ $split: ['$$this', '_'] }, 7] }, '.'] }, 1] }, 'wav'] } } }," +
                    "section: {" +
                      "job_id: '$$REMOVE'," +
                      "job_name: '$$REMOVE' }," +
                    "information: {" +
                      "verbal: {" +
                        "$map: {" +
                          "input: '$information.verbal'," +
                          "in: '$$this'" +
                        "} } } } }," +
            "{ $addFields: {" +
              "information: {" +
                "job_files: '$$REMOVE'," +
                "verbal: {" +
                  "speaker_id: '$$REMOVE'," +
                  "org_start_time: '$$REMOVE'," +
                  "org_end_time: '$$REMOVE' } } } }," +
            "{ $project: {" +
              "job_id: 1," +
              "subset_id: 1," +
              "subset_status: 1," +
              "video_name_set: 1," +
              "audio_name_set: 1," +
              "dialogue: {" +
                "section: '$section'," +
                "information: '$information' } } }] } }",
    "{ $project: {" +
      "mainset_info: { $first: { $getField: { input: { $first: { $first: '$mainset_info.mainset_info' } }, field: 'mainset_info' } } }," +
      "subset_info: { sets: '$sets' } } }"
  })
  ProjectExtractResponse extractProjects(String clientId, ObjectId workspaceId, ObjectId projectId, List<String> statuses, Pattern verbalFilter);
}
