package ai.maum.data.edit.api.job.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

import ai.maum.data.edit.api.job.dto.response.JobFileResponse;
import ai.maum.data.edit.api.workspace.field.job.Job;

public interface JobFileRepository extends MongoRepository<Job, ObjectId>, JobFileCustomRepository {
    @Aggregation(pipeline = {
            "{$match: { 'jobs.job_id': ?0 }}"
            ,"{$project: {" +
                "job: {" +
                    "$filter: {" +
                        "input: '$jobs'" +
                        ",as: 'job'" +
                        ",cond: { $eq: ['$$job.job_id', ?0]}" +
                    "}" +
                "}" +
            "}}"
            ,"{$unwind: '$job'}"
            ,"{$replaceRoot: {newRoot: '$job'}}"
            ,"{$project: {" +
                "job_files: {" +
                    "file_id: 1" +
                    ",file_name: 1" +
                    ",file_type: 1" +
                    ",file_url: 1" +
                "}" +
            "}}"
            ,"{$unwind: '$job_files'}"
            ,"{$replaceRoot: {newRoot: '$job_files'}}"
    })
    List<JobFileResponse> findAllJobFileByJobId(ObjectId jobId);

    @Aggregation(pipeline = {
            "{ $match: { 'jobs.job_files.file_id': ?0 } }"
            ,"{ $project: { jobs: 1 } }"
            ,"{ $unwind: '$jobs' }"
            ,"{ $replaceRoot: { newRoot: '$jobs' } }"
            ,"{ $match: { 'job_files.file_id': ?0 } }"
            ,"{ $project: { job_files: 1 } }"
            ,"{ $unwind: '$job_files' }"
            ,"{ $replaceRoot: { newRoot: '$job_files' } }"
            ,"{ $match: { 'file_id': ?0 } }"
            ,"{ $project: { file_url: 1 } }"
    })
    JobFileResponse findJobFileByFileId(ObjectId fileId);
}
