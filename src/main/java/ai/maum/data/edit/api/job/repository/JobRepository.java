package ai.maum.data.edit.api.job.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

import ai.maum.data.edit.api.job.dto.response.JobListResponse;
import ai.maum.data.edit.api.job.dto.response.ViewerJobDetailResponse;
import ai.maum.data.edit.api.workspace.field.job.Job;
import ai.maum.data.edit.api.workspace.field.labeling.ComplexConversation;
import ai.maum.data.edit.api.workspace.field.labeling.ComplexConversationLabeling;
import ai.maum.data.edit.common.repository.BasicCustomRepository;

public interface JobRepository extends MongoRepository<Job, ObjectId>, BasicCustomRepository, JobCustomRepository {

  @Aggregation(pipeline = {
    "{ $match: { project_id: ?0 } }",
    "{ $project: { jobs: 1 } }",
    "{ $unwind: '$jobs' }",
    "{ $replaceRoot: { newRoot: '$jobs' } }",
    "{ $project: { job_id: 1, job_name: 1, works: 1 } }",
    "{ $sort: { job_name: 1 } }",
    "{ $addFields: { work: { $first: '$works' } } }",
    "{ $project: { works: 0, work: { work_id: 0, labelings: 0 } } }",
    "{ $addFields: {" +
      "is_mine: {" +
        "$cond: {" +
          "if: { $eq: [?2, 'W'] }," +
          "then : { $eq: ['$work.worker_id', ?1] }," +
          "else: {" +
            "$cond: {" +
              "if: { $eq: [?2, 'C'] }," +
              "then: { $eq: ['$work.checker_id', ?1] }," +
              "else: false } } } } } }",
    "{ $group: {" +
      "_id: {" +
        "is_mine: '$is_mine'," +
        "work_status: {" +
          "name: {" +
            "$cond: {" +
              "if: { $eq: ['$work', null] }," +
              "then: 'RD'," +
              "else: '$work.work_status' } }," +
          "order: {" +
            "$cond: {" +
              "if: { $eq: [?2, 'W'] }," +
              "then: {" +
                "$cond: {" +
                  "if: { $eq: ['$work.work_status', 'RJ'] }," +
                  "then: 1," +
                  "else: {" +
                    "$cond: {" +
                      "if: { $eq: ['$work.work_status', 'AS'] }," +
                      "then: 2," +
                      "else: {" +
                        "$cond: {" +
                          "if: { $eq: ['$work', null] }," +
                          "then: 3," +
                          "else: {" +
                            "$cond: {" +
                              "if: { $eq: ['$work.work_status', 'RQ'] }," +
                              "then: 4," +
                              "else: {" +
                                "$cond: {" +
                                  "if: { $not: { $in: ['$work.work_status', ['IM', 'IC', 'NU']] } }," +
                                  "then: 5," +
                                  "else: 6 } } } } } } } } } }," +
              "else: {" +
                "$cond: {" +
                  "if: { $eq: [?2, 'C'] }," +
                  "then: {" +
                    "$cond: {" +
                      "if: { $in: ['$work.work_status', ['IS', 'IC']] }," +
                      "then: 1," +
                      "else: {" +
                        "$cond: {" +
                          "if: { $in: ['$work.work_status', ['RQ', 'IM']] }," +
                          "then: 2," +
                          "else: {" +
                            "$cond: {" +
                              "if: { $eq: ['$work.work_status', 'CM'] }," +
                              "then: 3," +
                              "else: {" +
                                "$cond: {" +
                                  "if: { $ne: ['$work.work_status', 'NU'] }," +
                                  "then: 4," +
                                  "else: 5 } } } } } } } }," +
                  "else: {" +
                    "$cond: {" +
                      "if: { $eq: ['$work.work_status', 'CM'] }," +
                      "then: 1," +
                      "else: {" +
                        "$cond: {" +
                          "if: { $eq: ['$work.work_status', 'NU'] }," +
                          "then: 2," +
                          "else: 3 } } } } } } } } } }," +
      "list: { $push: '$$ROOT' } } }",
    "{ $addFields: {" +
      "is_access: {" +
        "$cond: {" +
          "if: { $eq: [?2, 'W'] }," +
          "then: {" +
            "$and: [" +
              "{ $not: { $in: ['$_id.work_status.name', ['IM', 'IS', 'IC', 'CM', 'NU']] } }," +
              "{ $or: [" +
                "{ $eq: ['$_id.is_mine', true] }," +
                "{ $eq: ['$_id.work_status.name', 'RD'] }] }] }," +
          "else: {" +
            "$cond: {" +
              "if: { $eq: [?2, 'C'] }," +
              "then: {" +
                "$and: [" +
                  "{ $not: { $in: ['$_id.work_status.name', ['RD', 'AS', 'RJ', 'NU']] } }," +
                    "{ $or: [" +
                      "{ $eq: ['$_id.is_mine', true] }," +
                      "{ $in: ['$_id.work_status.name', ['RQ', 'IM']] }] }] }," +
              "else: { $in: ['$_id.work_status.name', ['CM', 'NU']] } } } } } } }",
    "{ $sort: { is_access: -1, '_id.is_mine': -1, '_id.work_status.order': 1 } }",
    "{ $unwind: '$list' }",
    "{ $project: { _id: 0, list: { $mergeObjects: ['$list', { is_access: '$is_access' }]} }}",
    "{ $project: { " +
      "'list.job_id': 1," +
      "'list.job_name': 1," +
      "'list.is_access': 1," +
      "'list.work_status': {" +
        "$cond: {" +
          "if: { $eq: ['$list.work', null]}," +
          "then: 'RD'," +
          "else: '$list.work.work_status' }}," +
      "'list.updated': '$list.work.updated' }}",
    "{ $sort: { 'list.job_name': 1 }}",
    "{ $group: { _id: null, list: { $push: '$list'}}}",
    "{ $project: { _id: 0, list: 1 }}",
    "{ $addFields: {" +
      "access_list: {" +
        " $filter:  {" +
          "input: '$list'," +
          "as: 'item'," +
          "cond: { $eq: ['$$item.is_access', true]}}}}}",
    "{ $addFields: {" +
      "rj_job_id: {" +
        "$ifNull: [" +
          "{ $arrayElemAt: [" +
            "{ $map:  {" +
              "input: {" +
                "$filter: {" +
                  "input:  '$access_list'," +
                  "as: 'item'," +
                  "cond: { $and: [" +
                    "{ $eq: ['$$item.work_status', 'RJ'] }," +
                    "{ $eq: [?2, 'W']}] }}}," +
          "as: 'rj_job_list'," +
          "in: '$$rj_job_list.job_id'}}, 0]}, null]}}}",
    "{ $addFields: {" +
      "target_job_id: {" +
        "$cond: {" +
          "if: { $eq: [?2, 'W']}," +
          "then: {" +
            "$cond: {" +
              "if: { $ne: ['$rj_job_id', null]}," +
              "then: '$rj_job_id'," +
              "else: {" +
                "$let: {" +
                  "vars: {" +
                    "filtered_list: {" +
                      "$sortArray: {" +
                        "input: {" +
                          "$filter: {" +
                            "input: '$access_list'," +
                            "as: 'item'," +
                            "cond: { $eq: ['$$item.work_status', 'AS'] }}}," +
                        "sortBy: { updated: -1 } }}}," +
                  "in: {" +
                    "$cond: {" +
                      "if: { $eq: [{ $size: { $ifNull: ['$$filtered_list', []] } }, 0] }," +
                      "then: { $arrayElemAt: ['$access_list.job_id', 0] }," +
                      "else: { $arrayElemAt: ['$$filtered_list.job_id', 0] } }}}}}}," +
          "else: {" +
            "$cond: {" +
              "if: { $eq: [?2, 'C'] }," +
              "then: {" +
                "$let: {" +
                  "vars: {" +
                    "filtered_list: {" +
                      "$sortArray: {" +
                        "input: {" +
                          "$filter: {" +
                            "input: '$access_list'," +
                            "as: 'item'," +
                            "cond: { $in: ['$$item.work_status', ['IS', 'IC']] }}}," +
                        "sortBy: { updated: -1 } }}}," +
                  "in: {" +
                    "$cond: {" +
                      "if: { $eq: [{ $size: { $ifNull: ['$$filtered_list', []] } }, 0] }," +
                      "then: { $arrayElemAt: ['$access_list.job_id', 0] }," +
                      "else: { $arrayElemAt: ['$$filtered_list.job_id', 0] } }}}}," +
              "else : { $arrayElemAt: ['$access_list.job_id', 0]} }}}}}}",
    "{ $project: {" +
      "target_job_id: 1," +
      "list: {" +
        "$map: {" +
          "input: '$list'," +
          "in: { $mergeObjects: ['$$this', { idx: { $indexOfArray: ['$list', '$$this'] } }] } } } } }",
    "{ $project: {" +
      "target_idx: {" +
        "$getField: {" +
          "field: 'idx'," +
          "input: {" +
            "$first: {" +
              "$filter: {" +
                "input: '$list'," +
                "cond: { $eq: ['$$this.job_id', '$target_job_id'] } } } } } }," +
      "list: { job_id: 1, job_name: 1, is_access: 1, work_status: 1 } } }"
  })
  JobListResponse findJobList(ObjectId projectId,
                              ObjectId userId,
                              String userType);

  @Aggregation(pipeline = {
    "{$match: { 'projects.jobs.job_id': ?0 }}",
    "{$project: { _id: 0, projects: 1 }}",
    "{$unwind: '$projects'}",
    "{$match: { 'projects.jobs.job_id': ?0 }}",
    "{$project: {" +
      "job: {" +
        "$filter: {" +
          "input: '$projects.jobs'" +
          ",as: 'job'" +
          ",cond: { $eq: ['$$job.job_id', ?0]}" +
        "}" +
      "}" +
    "}}",
    "{$unwind: '$job'}",
    "{$replaceRoot: {newRoot: '$job'}}",
    //,"{$match: {'works.work_status': {$nin: ['NU', 'IC', 'IM']} }}" // IC:작업 불가 검수 중, NU:작업 불가 승인, IM:작업 불가
    "{$unwind: '$works'}",
    "{$limit: 1}",
    "{$project: {" +
      "labelings : '$works.labelings'," +
      "step: '$works.step'," +
      "work_status: '$works.work_status'" +
    "}}",
    "{$unwind: '$labelings'}",
    "{$project: {" +
      "'labelings.labeling_id':1," +
      "'labelings.speaker_id': 1," +
      "'labelings.verbal': {" +
        "$cond: [{$or: [{$eq: [?1, 1]}, {$and: [{$eq: [?1, 0]}, {$eq: ['$step', 1]}]} ]}, '$labelings.verbal', '$$REMOVE']" +
      "}," +
      "'labelings.paraverbal': {" +
        "$cond: [{$or: [{$eq: [?1, 2]}, {$and: [{$eq: [?1, 0]}, {$eq: ['$step', 2]}]} ]}, '$labelings.paraverbal', '$$REMOVE']" +
      "}," +
      "'labelings.nonverbal': {" +
        "$cond: [{$or: [{$eq: [?1, 3]}, {$and: [{$eq: [?1, 0]}, {$eq: ['$step', 3]}]} ]}, '$labelings.nonverbal', '$$REMOVE']" +
      "}," +
      "'labelings.work_status': '$work_status'" +
    "}}",
    "{$replaceRoot: {newRoot: '$labelings'}}"
  })
  List<ComplexConversationLabeling> findLabelingByJobId(ObjectId jobId, int step);

  @Aggregation(pipeline = {
    "{ $match: { 'projects.project_id': ?0 } }",
    "{ $project: { _id:0 ,projects:1 } }",
    "{ $unwind: '$projects' }",
    "{ $replaceRoot: { newRoot: '$projects' } }",
    "{ $match: { 'project_id': ?0 } }",
    "{ $unwind: '$jobs' }",
    "{ $match: {" + // 본인이 한 작업만 가져오기
      "'jobs.works.worker_id': ?1," +
      "'jobs.works.work_status': {$in: ['AS', 'MF', 'RJ'] }" +
    "} }",
    "{ $project: { jobs: 1 } }",
    "{ $sort: { job_id: 1 } }",
    "{ $limit : 1 }",
    "{ $replaceRoot: { newRoot: '$jobs' } }",
    "{ $addFields: { 'works.job_id': '$job_id' } }",
    "{ $unwind: '$works' }",
    "{ $replaceRoot: { newRoot: '$works' } }",
      // speaker 별 정보와 labeling_each_speaker를 합치기 전.
    "{ $facet: {" +
      "meta: [" +
        "{$project: {" +
          "job_id: 1" +
          ",step: 1" +
          ",work_status: 1" +
          ",worker_id: 1" +
          ",work_id: 1" +
        "} }" +
      "]," +
      "labeling_all: [" +
        "{ $project: { labelings: 1 } }," +
        "{ $unwind: '$labelings' }," +
        "{ $replaceRoot: { newRoot: '$labelings' } }," +
        "{ $addFields: { 'verbal.speaker_id': '$speaker_id', 'verbal.labeling_id': '$labeling_id' } }," +
        "{ $project: { verbal: 1 } }," +
        "{ $unwind: '$verbal' }," +
        "{ $replaceRoot: { newRoot: '$verbal' } }," +
        "{ $sort: { start_time: 1 } }" +
      "]" +
    "}}",
    "{ $project: {" +
      "labeling_all: 1," +
      "meta: { $arrayElemAt: ['$meta', 0] }" +
    "} }"
  })
  ComplexConversation findLabelingByProjectIdAndUserId(ObjectId projectId, ObjectId userId, int step);

  @Aggregation(pipeline = {
    "{ $match: { 'jobs.job_id': ?0 } }",
    "{ $unwind: '$jobs' }",
    "{ $match: {" +
      "'jobs.job_id': ?0 " +
    "} }",
    "{ $project: { jobs: 1 } }",
    "{ $replaceRoot: { newRoot: '$jobs' } }",
    "{ $facet: {" +
      "my_work: [ {$match: { 'works.worker_id': ?1 } } ]," + // 본인이 하던 work가 있는지
      "others_work: []" +
    "} }",
    "{ $project: {" +
      "job: {" +
        "$cond: [" +
          "{$gt: [ {$size: '$my_work' } ,0]}" +
            ", '$my_work'" + // 본인이 하던 work가 있을 경우
            ", '$others_work'" + // 없을 경우
        "]" +
      "}" +
    "}}",
    "{ $unwind: '$job' }",
    "{ $replaceRoot: { newRoot: '$job' } }",
    "{ $addFields: { 'works.job_id': '$job_id' } }",
    "{ $unwind: '$works' }",
    "{ $replaceRoot: { newRoot: '$works' } }",
    // speaker 별 정보와 labeling_each_speaker를 합치기 전.
    "{ $facet: {" +
      "meta: [" +
        "{$project: {" +
          "job_id: 1," +
          "step: 1," +
          "check_step: 1," +
          "work_status: 1," +
          "worker_id: 1," +
          "checker_id: 1," +
          "work_id: 1" +
        "} }" +
      "]," +
      "verbal: [" +
        "{ $addFields: {'labelings.step': '$step'} }," +
        "{ $project: { labelings: 1} }," +
        "{ $unwind: '$labelings' }," +
        "{ $replaceRoot: { newRoot: '$labelings' } }," +
        "{ $project: {" +
          "speaker_id: 1," +
          "labeling_id: 1," +
          "data: { $ifNull: ['$verbal', []] }" +
        "} }" +
        ",{ $addFields: {" +
          "'data.speaker_id': '$speaker_id'," +
          "'data.labeling_id': '$labeling_id'" +
        "}}," +
        "{ $unwind: '$data' }," +
        "{ $replaceRoot: { newRoot: '$data' } }," +
        "{ $sort: { start_time: 1 } }" +
      "]" +
      ",labeling_all: [" +
        "{ $addFields: {'labelings.step': '$step'} }" +
        ",{ $project: { labelings: 1 } }" +
        ",{ $unwind: '$labelings' }" +
        ",{ $replaceRoot: { newRoot: '$labelings' } }" +
        ",{ $project: {" +
          "speaker_id: 1," +
          "labeling_id: 1," +
          "data: {$cond: [" +
            "{$or: [{$eq: [?2, 2]}, {$and: [{$eq: [?2, 0]}, {$eq: ['$step', 2]}]} ]}," +
            "'$paraverbal'," +
            "{$cond: [" +
              "{$or: [{$eq: [?2, 3]}, {$and: [{$eq: [?2, 0]}, {$eq: ['$step', 3]}]} ]}," +
              "'$nonverbal'," +
              "[]" +
            "]}" + // 3 $cond
          "]}" + // 2 $cond
        "} }" +
        ",{ $addFields: {" +
            "'data.speaker_id': {$cond: [" +
                "{$gt: [{$size: '$data'}, 0]}," +
                "'$speaker_id'," +
                "'$$REMOVE'" +
            "]}," +
            "'data.labeling_id': {$cond: [" +
                "{$gt: [{$size: '$data'}, 0]}" +
                ",'$labeling_id'" +
                ",'$$REMOVE'" +
            "]}" +
        "}}" +
        ",{ $unwind: '$data' }" +
        ",{ $replaceRoot: { newRoot: '$data' } }" +
        ",{ $sort: { start_time: 1 } }" +
      "]" +
      ",labeling_info: [" +
          "{ $project: { labelings: 1 } }" +
          ",{ $unwind: '$labelings' }" +
          ",{ $replaceRoot: { newRoot: '$labelings' } }" +
          ",{ $project: { labeling_id: 1, speaker_id: 1 } }" +
      "]" +
    "} }"
    ,"{ $project: {" +
        "verbal: 1" +
        ",labeling_all: 1" +
        ",meta: { $arrayElemAt: ['$meta', 0] }" +
        ",labeling_info: 1" +
    "} }"
  })
  // TODO 현재 복합대화에만 맞는 형식으로 return 중
  ComplexConversation findLabelingByJobIdAndUserId(ObjectId jobId, ObjectId userId, int step);

  @Aggregation(pipeline = {
    "{ $match: { 'projects.project_id': ?0 } }"
    ,"{ $project: { _id:0 ,projects:1 } }"
    ,"{ $unwind: '$projects' }"
    ,"{ $replaceRoot: { newRoot: '$projects' } }"
    ,"{ $match: { 'project_id': ?0 } }"
    ,"{$project: {job: { $arrayElemAt: ['$jobs', 0] }}}" // 프로젝트 하위 첫 번째 JOB
    // 다른 사람이 진행한 작업
    ,"{ $replaceRoot: { newRoot: '$job' } }"
    ,"{ $addFields: { 'works.job_id': '$job_id' } }"
    ,"{ $unwind: '$works' }"
    ,"{ $replaceRoot: { newRoot: '$works' } }"
    // speaker 별 정보와 labeling_each_speaker를 합치기 전.
    ,"{ $facet: {" +
      "meta: [" +
        "{$project: {" +
          "job_id: 1" +
          ",step: 1" +
          ",work_status: 1" +
          ",worker_id: 1" +
          ",work_id: 1" +
        "} }" +
      "]" +
      ",labeling_all: [" +
        "{ $project: { labelings: 1 } }" +
        ",{ $unwind: '$labelings' }" +
        ",{ $replaceRoot: { newRoot: '$labelings' } }" +
        ",{ $addFields: { 'verbal.speaker_id': '$speaker_id', 'verbal.labeling_id': '$labeling_id' } }" +
        ",{ $project: { verbal: 1 } }" +
        ",{ $unwind: '$verbal' }" +
        ",{ $replaceRoot: { newRoot: '$verbal' } }" +
        ",{ $sort: { start_time: 1 } }" +
      "]" +
    "} }"
    ,"{ $project: {" +
      "labeling_all: 1" +
      ",meta: { $arrayElemAt: ['$meta', 0] }" +
    "} }"
  })
  ComplexConversation findLabelingOfFirstJob(ObjectId projectId);

  @Aggregation(pipeline = {
    "{ $match: { 'project_id': ?0 } }"
    ,"{ $facet: {" +
      "by_project: [" +
        "{$project: {job: { $arrayElemAt: ['$jobs', 0] }}}" + // 프로젝트 하위 첫 번째 JOB
        ",{ $replaceRoot: { newRoot: '$job' } }" +
      "]" +
      ",by_job: [" +
        "{ $match: { 'jobs.job_id': ?1 } }" +
        ",{ $project: { jobs:1 } }" +
        ",{ $unwind: '$jobs' }" +
        ",{ $replaceRoot: { newRoot: '$jobs' } }" +
        ",{ $match: { 'job_id': ?1 } }" +
      "]" +
    "} }"
    ,"{ $project: {" +
      "job: {" +
        "$cond: [" +
          "{$gt: [{$size: '$by_job'}, 0]}" +
          ", '$by_job'" +
          ", '$by_project'" +
        "]" +
      "}" +
    "} }"
    ,"{ $unwind: '$job' }"
    ,"{ $replaceRoot: { newRoot: '$job' } }"
    ,"{ $facet: {" +
      "meta:[" +
        "{$project: {" +
          "job_id: '$job_id'" +
        "}}" +
      "]" +
    "} }"
    // 최종 출력
    ,"{ $project: { meta: {$arrayElemAt: ['$meta', 0]} } }"
    ,"{ $unwind: '$meta' }"
    ,"{ $replaceRoot: {newRoot: '$meta'} }"
  })
  ComplexConversation.Meta findByProjectIdOrJobId(ObjectId projectId, ObjectId jobId);

  @Aggregation(pipeline = {
    "{ $match: { 'jobs.job_id': ?0 } }"
      ,"{ $unwind: '$jobs' }"
      ,"{ $match: {" +
          "'jobs.job_id': ?0 " +
      "} }"
      ,"{ $project: { jobs: 1 } }"
      ,"{ $replaceRoot: { newRoot: '$jobs' } }"
    ,"{ $facet: {" +
        "my_work: [ {$match: { 'works.worker_id': ?1 } } ]" + // 본인이 하던 work가 있는지
        ",others_work: []" +
      "} }"
      ,"{ $project: {" +
        "job: {" +
          "$cond: [" +
            "{$gt: [ {$size: '$my_work' } ,0]}" +
            ", '$my_work'" + // 본인이 하던 work가 있을 경우
            ", '$others_work'" + // 없을 경우
          "]" +
        "}" +
      "}}"
      ,"{ $unwind: '$job' }"
      ,"{ $replaceRoot: { newRoot: '$job' } }"
      ,"{ $addFields: { 'works.job_id': '$job_id' } }"
      ,"{ $unwind: '$works' }"
      ,"{ $replaceRoot: { newRoot: '$works' } }"
      // speaker 별 정보와 labeling_each_speaker를 합치기 전.
      ,"{ $facet: {" +
        "meta: [" +
          "{$project: {" +
            "job_id: 1" +
            ",step: 1" +
            ",check_step: 1" +
            ",work_status: 1" +
            ",worker_id: 1" +
            ",checker_id: 1" +
            ",work_id: 1" +
          "} }" +
        "]" +
        ",verbal: [" +
          "{ $addFields: {'labelings.step': '$step'} }" +
          ",{ $project: { labelings: 1} }" +
          ",{ $unwind: '$labelings' }" +
          ",{ $replaceRoot: { newRoot: '$labelings' } }" +
          ",{ $project: {" +
            "speaker_id: 1" +
            ",labeling_id: 1" +
            ",data: '$verbal'" +
          "} }" +
          ",{ $addFields: {" +
            "'data.speaker_id': '$speaker_id'" +
            ",'data.labeling_id': '$labeling_id'" +
          "}}" +
          ",{ $unwind: '$data' }" +
          ",{ $replaceRoot: { newRoot: '$data' } }" +
          ",{ $sort: { start_time: 1 } }" +
        "]" +
        ",labeling_all: [" +
          "{ $addFields: {'labelings.step': '$check_step'} }" +
          ",{ $project: { labelings: 1 } }" +
          ",{ $unwind: '$labelings' }" +
          ",{ $replaceRoot: { newRoot: '$labelings' } }" +
          ",{ $project: {" +
            "speaker_id: 1" +
            ",labeling_id: 1" +
            ",data: {$cond: [" +
              "{$or: [{$eq: [?2, 2]}, {$and: [{$eq: [?2, 0]}, {$eq: ['$step', 2]}]} ]}" +
              ",'$paraverbal'" +
              ",{$cond: [" +
                "{$or: [{$eq: [?2, 3]}, {$and: [{$eq: [?2, 0]}, {$eq: ['$step', 3]}]} ]}" +
                ", '$nonverbal'" +
                ", []" +
              "]}" + // 3 $cond
            "]}" + // 2 $cond
          "} }" +
          ",{ $addFields: {" +
            "'data.speaker_id': {$cond: [" +
              "{$gt: [{$size: '$data'}, 0]}" +
              ",'$speaker_id'" +
              ",'$$REMOVE'" +
            "]}" +
            ",'data.labeling_id': {$cond: [" +
              "{$gt: [{$size: '$data'}, 0]}" +
              ",'$labeling_id'" +
              ",'$$REMOVE'" +
            "]}" +
          "}}" +
          ",{ $unwind: '$data' }" +
          ",{ $replaceRoot: { newRoot: '$data' } }" +
          ",{ $sort: { start_time: 1 } }" +
        "]" +
        ",labeling_info: [" +
          "{ $project: { labelings: 1 } }" +
          ",{ $unwind: '$labelings' }" +
          ",{ $replaceRoot: { newRoot: '$labelings' } }" +
          ",{ $project: { labeling_id: 1, speaker_id: 1 } }" +
        "]" +
      "} }"
      ,"{ $project: {" +
        "verbal: 1" +
        ",labeling_all: 1" +
        ",meta: { $arrayElemAt: ['$meta', 0] }" +
        ",labeling_info: 1" +
      "} }"
  })
  ComplexConversation findLabelingByJobIdAndUserIdForChecker(ObjectId jId, ObjectId userId, int step);

  @Aggregation(pipeline = {
    "{ $match: { 'jobs.job_id': ?0 } }"
    ,"{ $unwind: '$jobs' }"
    ,"{ $match: {" +
        "'jobs.job_id': ?0 " +
    "} }"
    ,"{ $project: { jobs: 1 } }"
    ,"{ $replaceRoot: { newRoot: '$jobs' } }"
    ,"{ $addFields: { work: { $first: '$works' } }}"
    ,"{ $addFields: { 'work.job_id': '$job_id' } }"
    ,"{ $project: { work: 1 } }"
    ,"{ $replaceRoot: { newRoot: '$work' } }"
    ,"{ $facet: {" +
      "meta: [" +
        "{$project: {" +
        "job_id: 1" +
        ",step: 1" +
        ",check_step: 1" +
        ",work_status: 1" +
        ",worker_id: 1" +
        ",checker_id: 1" +
        ",work_id: 1" +
        "} }" +
      "]" +
      ",verbal: [" +
        "{ $addFields: {'labelings.step': '$step'} }" +
        ",{ $project: { labelings: 1} }" +
        ",{ $unwind: '$labelings' }" +
        ",{ $replaceRoot: { newRoot: '$labelings' } }" +
        ",{ $project: {" +
          "speaker_id: 1" +
          ",labeling_id: 1" +
          ",data: '$verbal'" +
        "} }" +
        ",{ $addFields: {" +
          "'data.speaker_id': '$speaker_id'" +
          ",'data.labeling_id': '$labeling_id'" +
        "}}" +
        ",{ $unwind: '$data' }" +
        ",{ $replaceRoot: { newRoot: '$data' } }" +
        ",{ $sort: { start_time: 1 } }" +
      "]" +
      ",labeling_all: [" +
        "{ $addFields: {'labelings.step': '$check_step'} }" +
        ",{ $project: { labelings: 1 } }" +
        ",{ $unwind: '$labelings' }" +
        ",{ $replaceRoot: { newRoot: '$labelings' } }" +
        ",{ $project: {" +
          "speaker_id: 1" +
          ",labeling_id: 1" +
          ",data: {$cond: [" +
            "{$or: [{$eq: [?1, 2]}, {$and: [{$eq: [?1, 0]}, {$eq: ['$step', 2]}]} ]}" +
            ",'$paraverbal'" +
            ",{$cond: [" +
              "{$or: [{$eq: [?1, 3]}, {$and: [{$eq: [?1, 0]}, {$eq: ['$step', 3]}]} ]}" +
              ", '$nonverbal'" +
              ", []" +
            "]}" + // 3 $cond
          "]}" + // 2 $cond
        "} }" +
        ",{ $addFields: {" +
          "'data.speaker_id': {$cond: [" +
            "{$gt: [{$size: '$data'}, 0]}" +
            ",'$speaker_id'" +
            ",'$$REMOVE'" +
          "]}" +
          ",'data.labeling_id': {$cond: [" +
            "{$gt: [{$size: '$data'}, 0]}" +
            ",'$labeling_id'" +
            ",'$$REMOVE'" +
          "]}" +
        "}}" +
        ",{ $unwind: '$data' }" +
        ",{ $replaceRoot: { newRoot: '$data' } }" +
        ",{ $sort: { start_time: 1 } }" +
      "]" +
      ",labeling_info: [" +
        "{ $project: { labelings: 1 } }" +
        ",{ $unwind: '$labelings' }" +
        ",{ $replaceRoot: { newRoot: '$labelings' } }" +
        ",{ $project: { labeling_id: 1, speaker_id: 1 } }" +
      "]" +
    "} }"
    ,"{ $project: {" +
      "verbal: 1" +
      ",labeling_all: 1" +
      ",meta: { $arrayElemAt: ['$meta', 0] }" +
      ",labeling_info: 1" +
    "} }"
  })
  ComplexConversation findLabelingByJobIdAndUserIdForViewer(ObjectId jId, int step);

  @Aggregation(pipeline = {
    "{ $match: { project_id: ?0 } }",
    "{ $unwind: '$jobs' }",
    "{ $match: { 'jobs.job_id': ?1 } }",
    "{ $project: { jobs: 1 } }",
    "{ $replaceRoot: { newRoot: '$jobs' } }",
    "{ $addFields: { work: { $first: '$works' } } }",
    "{ $addFields: { 'work.job_id': '$job_id' } }",
    "{ $project: { work: 1 } }",
    "{ $replaceRoot: { newRoot: '$work' } }",
    "{ $project: { labelings: 1 } }",
    "{ $unwind: '$labelings' }",
    "{ $replaceRoot: { newRoot: '$labelings' } }",

    // 1. Speaker ID별 데이터 추출 및 합치기 ($map을 사용하여 각 배열에 speaker_id 추가)
    "{ $project: { " +
      "_id: 0, " +
      "speaker_id: '$speaker_id', " +
      "verbals: { " +
        "$map: { " +
          "input: '$verbal', " +
          "as: 'v', " +
          "in: { $mergeObjects: ['$$v', { speaker_id: '$speaker_id', labeling_id: '$labeling_id' }] } " +/**/
        "} " +
      "}, " +
      "paraverbals: { " +
        "$map: { " +
          "input: '$paraverbal', " +
          "as: 'pv', " +
          "in: { $mergeObjects: ['$$pv', { speaker_id: '$speaker_id', labeling_id: '$labeling_id' }] } " +
        "} " +
      "}, " +
      "nonverbals: { " +
        "$map: { " +
          "input: '$nonverbal', " +
          "as: 'nv', " +
          "in: { $mergeObjects: ['$$nv', { speaker_id: '$speaker_id', labeling_id: '$labeling_id' }] } " +
        "} " +
      "}" +
    "} }",

    // 2. Speaker ID를 기준으로 최종 그룹화
    "{ $group: { " +
      "_id: '$speaker_id', " +
      "verbals: { $push: '$verbals' }, " +
      "paraverbals: { $push: '$paraverbals' }, " +
      "nonverbals: { $push: '$nonverbals' }" +
    "} }",

    // 3. 최종 Projection: _id를 speakerId로 변경 및 중첩 배열 평탄화 (Flatten)
    "{ $project: { " +
      "_id: 0, " +
      "speaker: { speaker_id: '$_id' }, " +
      "verbals: { " +
        "$reduce: { " +
          "input: '$verbals', " +
          "initialValue: [], " +
          "in: { $concatArrays: ['$$value', '$$this'] } " +
        "} " +
      "}, " +
      "paraverbals: { " +
        "$reduce: { " +
          "input: '$paraverbals', " +
          "initialValue: [], " +
          "in: { $concatArrays: ['$$value', '$$this'] } " +
        "} " +
      "}, " +
      "nonverbals: { " +
        "$reduce: { " +
          "input: '$nonverbals', " +
          "initialValue: [], " +
          "in: { $concatArrays: ['$$value', '$$this'] } " +
        "} " +
      "}" +
    "} }",

    "{ $sort: { 'speaker.speaker_id': 1 } }"
  })
  List<ViewerJobDetailResponse> findLabelingForViewer(ObjectId projectId, ObjectId jobId);
}
