package ai.maum.data.edit.api.uploadinfo;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import ai.maum.data.edit.common.repository.BasicCustomRepository;
import ai.maum.data.edit.api.admin.workspace.dto.response.AdminWorkspaceLatestUploadResponse;

public interface UploadInfoRepository extends MongoRepository<UploadInfoEntity, ObjectId>, BasicCustomRepository {
    UploadInfoEntity findByUserId(ObjectId userId);

    @Aggregation(pipeline = {
            "{ $match: { user_id: ?0 } }",
            "{ $unwind: '$info' }",
            "{ $replaceRoot: { newRoot: '$info' } }",
            "{ $sort: { created: -1 } }",
            "{ $limit: 1 }",
            "{ $lookup: {" +
                "from: 'workspace'," +
                "localField: 'workspace_id'," +
                "foreignField: 'workspace_id'," +
                "as: 'workspace_info' } }",
            "{ $project: {" +
                "workspace_id: 1," +
                "workspace_name: { $first: '$workspace_info.workspace_name' } } }"
    })
    AdminWorkspaceLatestUploadResponse findLatestUploadWorkspaceName(ObjectId userId);
}
