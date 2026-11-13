package ai.maum.data.edit.api.job.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;

import java.util.List;
import java.util.Map;

import ai.maum.data.edit.api.job.dto.response.JobFileResponse;
import ai.maum.data.edit.common.repository.BasicCustomRepositoryImpl;
import ai.maum.data.edit.common.repository.CustomAggregation;

public class JobFileCustomRepositoryImpl extends BasicCustomRepositoryImpl implements JobFileCustomRepository {

  private String collection = "project";

  public JobFileCustomRepositoryImpl(MongoTemplate mongoTemplate) {
    super(mongoTemplate);
  }

  /**
   * @param workspaceId
   * @param projectId
   * @param jobId       쿼리
   *                    db.workspace.aggregate(
   *                    {$match: {project_id: ObjectId('64eed1215a6f5b5208a48a3c')}}
   *                    ,{$unwind: '$jobs'}
   *                    ,{$replaceRoot: {newRoot: '$jobs'}}
   *                    ,{$match: {job_id: ObjectId('64eed1215a6f5b5208a48a4b')}}
   *                    <p>
   *                    ,{$unwind: '$job_files'}
   *                    ,{$replaceRoot: {newRoot: '$job_files'}}
   *                    ,{$match: {'file_ext': 'wav'}}
   *                    <p>
   *                    ,{$facet: {
   *                    count: [ {$count: 'count'} ]
   *                    ,files: [
   *                    {$project: {
   *                    file_id: 1
   *                    ,file_path: 1
   *                    ,file_name: 1
   *                    }}
   *                    ]
   *                    }}
   *                    <p>
   *                    ,{$project: {
   *                    count: { $arrayElemAt: ['$count', 0] }
   *                    ,files: 1
   *                    }}
   *                    ,{$project: {
   *                    files: {$cond: {
   *                    if: { $gt: ['$count.count', 2] }
   *                    ,then: {$filter: {
   *                    input: '$files'
   *                    ,as: 'file'
   *                    ,cond: {
   *                    $regexMatch: {
   *                    input: { $toUpper: '$$file.file_name' },
   *                    regex: /^((?!MIX).)*P/
   *                    }
   *                    }
   *                    }} // 발화자별 wav
   *                    ,else: {$filter: {
   *                    input: '$files'
   *                    ,as: 'file'
   *                    ,cond: {
   *                    $regexMatch: {
   *                    input: { $toUpper: '$$file.file_name' },
   *                    regex: /MIX.*PIN/
   *                    }
   *                    }
   *                    }} // MIX . Pin wav 하나
   *                    }} // files
   *                    }}
   *                    ,{$unwind: '$files'}
   *                    ,{$replaceRoot: {newRoot: '$files'}}
   *                    );
   */
  @Override
  public List<JobFileResponse> getFiles(ObjectId workspaceId, ObjectId projectId, ObjectId jobId) {
    return CustomAggregation.setAggregation(collection)
        .match("project_id", projectId)
        .unwindAndReplaceRoot("jobs")
        .match("job_id", jobId)
        .unwindAndReplaceRoot("job_files")
        .match("file_ext", "wav")
        .facet(CustomAggregation.initFacet()
            .setFacet("count"
                , CustomAggregation.setPipline()
                    .count("count")
                    .build()
            )
            .setFacet("files"
                , CustomAggregation.setPipline()
                    .project("file_id", "file_path", "file_name")
                    .build()
            )

        )
        .project(
            Map.of("count", CustomAggregation.arrayElemAt("count", 0))
            , "files"
        )
        .project( // 발화자별 파일 존재 여부에 따라 추출할 파일 선택
            Map.of("files",
                CustomAggregation.cond(
                    CustomAggregation.gt("count.count", 2)
                    , regexMatch("^((?!MIX).)*P")
                    , regexMatch("MIX.*PIN")
                )
            )
        )
        .unwindAndReplaceRoot("files")
        .aggregationList(JobFileResponse.class, mongoTemplate);
  }

  private ArrayOperators.Filter regexMatch(String regex) {
    return CustomAggregation.filter(
        "files"
        , "file"
        , CustomAggregation.regexMatch(CustomAggregation.toUpper("file.file_name"), regex)
    );
  }
}
