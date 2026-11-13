package ai.maum.data.edit.api.workspace.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;

import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;

import ai.maum.data.edit.api.admin.user.dto.response.AdminUserListForWorkspaceAssignmentResponse;
import ai.maum.data.edit.api.admin.user.vo.DeletableUserListValue;
import ai.maum.data.edit.api.admin.workspace.dto.response.AdminWorkspaceListResponse;
import ai.maum.data.edit.api.admin.workspace.dto.response.AdminWorkspaceResponse;
import ai.maum.data.edit.api.admin.workspace.vo.AdminWorkspaceJoinedProjectsValue;
import ai.maum.data.edit.api.admin.workspace.vo.AdminWorkspaceWithProjectsValue;
import ai.maum.data.edit.api.user.dto.response.UserTypeResponse;
import ai.maum.data.edit.api.workspace.WorkspaceEntity;

public interface WorkspaceRepository extends MongoRepository<WorkspaceEntity, ObjectId>, WorkspaceCustomRepository {
    boolean existsByClientIdAndWorkspaceId(String clientId, ObjectId workspaceId);
    boolean existsByClientIdAndWorkspaceNameAndIsDeleted(String clientId, String workspaceName, Boolean isDeleted);

    WorkspaceEntity findByClientIdAndWorkspaceId(String clientId, ObjectId workspaceId);

    @Query(value = "{ client_id: ?0, workspace_id: ?1 }",
            fields = "{ '_id': 0, client_id: 0, workspace_id: 0, projects: 0, created: 0, updated: 0 }")
    AdminWorkspaceResponse findWorkspace(String clientId, ObjectId workspaceId);

    @Aggregation({
      "{ $match: { client_id: ?0, workspace_id: ?1 } }",
      "{ $lookup: {" +
        "from: 'project'," +
        "localField: 'workspace_id'," +
        "foreignField: 'workspace_id'," +
        "as: 'projects' } }",
      "{ $project: {" +
        "workspace_id: 1," +
        "projects: {" +
          "$map: {" +
            "input: '$projects'," +
            "as: 'project'," +
            "in: {" +
              "project_id: '$$project.project_id'," +
              "is_deleted: '$$project.is_deleted'," +
              "post_status: '$$project.post_status'}}}}}"
    })
    AdminWorkspaceWithProjectsValue findWsWithPjByClientIdAndWorkspaceId(String clientId, ObjectId workspaceId);

    @Aggregation({
      "{ $match: { client_id: ?0, workspace_id: { $in: ?1 } } }",
      "{ $lookup: {" +
          "from: 'project'," +
          "localField: 'workspace_id'," +
          "foreignField: 'workspace_id'," +
          "as: 'projects' } }",
        "{ $project: {" +
          "workspace_id: 1," +
          "projects: {" +
            "$map: {" +
              "input: '$projects'," +
              "as: 'project'," +
              "in: {" +
                "project_id: '$$project.project_id'," +
                "is_deleted: '$$project.is_deleted'," +
                "post_status: '$$project.post_status'}}}}}"
    })
    List<AdminWorkspaceWithProjectsValue> findWsWithPjsByClientIdAndWorkspaceIdIn(String clientId, List<ObjectId> workspaceIds);

    @Aggregation(pipeline = {
            "{ $match: { client_id: ?0, is_deleted: ?6 } }",
            "{ $sort: { created: -1 } }",
            "{ $project: { _id: 0, workspace_id: 1, workspace_name: 1, incharge: 1, start_date: 1, end_date: 1, workspace_intro: 1, access_users: 1, 'projects.is_deleted': 1 } }",
            "{ $facet: {" +
                "workspace_count: [{ $count: 'value' }]," +
                "result: [" +
                    "{ $project: { info: '$$ROOT' } }," +
                    "{ $replaceRoot: { newRoot: '$info' } }," +
                    "{ $match: {" +
                        "?1: ?2," +
                        "$expr: {" +
                            "$cond: {" +
                                "if: {" +
                                    "$cond: {" +
                                        "if: { $and: [{ $ne: [?4, null] }, { $ne: [?5, null] }] }," +
                                        "then: { $and: [{ $gte: ['$start_date', ?4] }, { $lt: ['$end_date', { $dateAdd: { startDate: ?5, amount: 1, unit: 'day' } }] }] }," +
                                        "else: {" +
                                            "$cond: {" +
                                                "if: { $ne: [?4, null] }," +
                                                "then: { $gte: ['$start_date', ?4] }," +
                                                "else: {" +
                                                    "$cond: {" +
                                                        "if: { $ne: [?5, null] }," +
                                                        "then: { $lt: ['$end_date', { $dateAdd: { startDate: ?5, amount: 1, unit: 'day' } }] }," +
                                                        "else: true } } } } } }," +
                                "then: true," +
                                "else: false } } } }] } }",
            "{ $addFields: { result: { $cond: { if: { $eq: [{ $size: '$result' }, 0] }, then: [{}], else: '$result' } } } }",
            "{ $unwind: '$result' }",
            "{ $facet: {" +
                "workspace_count: [" +
                    "{ $project: { " +
                        "value: { " +
                            "$ifNull: [" +
                                "{ $arrayElemAt: ['$workspace_count.value', 0] }, " +
                                "0" +
                            "]" +
                        "} } }]," +
                "workspaces: [" +
                    "{ $project: { info: '$result' } }," +
                    "{ $replaceRoot: { newRoot: '$info' } }," +
                    "{ $lookup: {" +
                        "from: 'project'," +
                        "localField: 'workspace_id'," +
                        "foreignField: 'workspace_id'," +
                        "as: 'projects' } }," +
                    "{ $addFields: {" +
                        "project_count: {" +
                            "$let: {" +
                                "vars: {" +
                                    "used_projects: {" +
                                        "$filter: {" +
                                            "input: '$projects'," +
                                            "cond: { $eq: ['$$this.is_deleted', false] } } } }," +
                                "in: {" +
                                    "$cond: {" +
                                        "if: { $or: [{ $eq: [{ $ifNull: ['$$used_projects', null] }, null] }, { $eq: [{ $size: '$$used_projects' }, 0] }] }," +
                                        "then: '-'," +
                                        "else: { $toString: { $size: '$$used_projects' } } } } } }," +
                        "member_count: { " +
                            "$cond: {" +
                                "if: { $eq: [{ $ifNull: ['$access_users', null] }, null] }," +
                                "then: '-'," +
                                "else: {" +
                                    "worker: { $toString: { $size: { $filter: { input: '$access_users', cond: { $eq: ['$$this.type', 'W'] } } } } }," +
                                    "checker: { $toString: { $size: { $filter: { input: '$access_users', cond: { $eq: ['$$this.type', 'C'] } } } } }," +
                                    "viewer: { $toString: { $size: { $filter: { input: '$access_users', cond: { $eq: ['$$this.type', 'V'] } } } } } } } } } }," +
                    "{ $addFields: {" +
                        "worker_count: { $ifNull: ['$member_count.worker', '-'] }," +
                        "checker_count: { $ifNull: ['$member_count.checker', '-'] }," +
                        "viewer_count: { $ifNull: ['$member_count.viewer', '-'] }," +
                        "status: {" +
                            "$cond: {" +
                                "if: { $lt: ['$$NOW', '$start_date'] }," +
                                "then: '공개 전'," +
                                "else: {" +
                                    "$cond: {" +
                                        "if: { $and: [" +
                                            "{ $gte: ['$$NOW', '$start_date'] }," +
                                            "{ $lt: ['$$NOW', { $dateAdd: { startDate: '$end_date', amount: 1, unit: 'day' } }] }] }," +
                                        "then: '공개 중'," +
                                        "else: '기간만료' } } } } } }," +
                    "{ $match: {" +
                        "$expr: {" +
                            "$cond: {" +
                                "if: {" +
                                    "$or: [" +
                                        "{ $eq: [?3, 'FILTER_WS_ST_ALL'] }," +
                                        "{ $and: [" +
                                            "{ $eq: ['$status', '공개 전'] }," +
                                            "{ $eq: [?3, 'FILTER_WS_ST_PENDING'] }] }," +
                                        "{ $and: [" +
                                            "{ $eq: ['$status', '공개 중'] }," +
                                            "{ $eq: [?3, 'FILTER_WS_ST_PROGRESS'] }] }," +
                                        "{ $and: [" +
                                            "{ $eq: ['$status', '기간만료'] }," +
                                            "{ $eq: [?3, 'FILTER_WS_ST_EXPIRE'] }] }] }," +
                                "then: true," +
                                "else: false } } } }," +
                    "{ $project: { member_count: 0, projects: 0 } }] } }",
            "{ $facet: {" +
                "workspace_count: [{ $project: { value: '$workspace_count' } }]," +
                "workspaces: [" +
                    "{ $project: { value: '$workspaces' } }," +
                    "{ $unwind: '$value' }," +
                    "{ $replaceRoot: { newRoot: '$value' } }," +
                    "{ $skip: ?7 }," +
                    "{ $limit: ?8 }," +
                    "{ $match: { workspace_id: { $exists: true } } }]," +
                "total_page: [" +
                    "{ $project: { info: '$workspaces' } }," +
                    "{ $unwind: '$info' }," +
                    "{ $replaceRoot: { newRoot: '$info' } }," +
                    "{ $match: { workspace_id: { $exists: true } } }," +
                    "{ $count: 'value' }," +
                    "{ $project: { value: { $cond: { if: { $eq: [{ $mod: ['$value', 10] }, 0] }, then: { $divide: ['$value', 10] }, else: { $add: [{$trunc: {$divide: ['$value', 10]} }, 1] } } } } }]," +
                "result_count: [" +
                    "{ $project: { info: '$workspaces' } }," +
                    "{ $unwind: '$info' }," +
                    "{ $replaceRoot: { newRoot: '$info' } }," +
                    "{ $match: { workspace_id: { $exists: true } } }," +
                    "{ $count: 'value' }] } }",
            "{ $addFields: {" +
                "total_page: { $cond: { if: { $eq: [{ $size: '$total_page' }, 0] }, then: [{ value: 0 }], else: '$total_page' } }," +
                "result_count: { $cond: { if: { $eq: [{ $size: '$result_count' }, 0] }, then: [{ value: 0 }], else: '$result_count' } } } }",
            "{ $unwind: '$workspace_count' }",
            "{ $unwind: '$total_page' }",
            "{ $unwind: '$result_count' }",
            "{ $project: { workspace_count: { $ifNull: [ { $arrayElemAt:  ['$workspace_count.value.value', 0] }, 0 ] }, total_page: '$total_page.value', result_count: '$result_count.value', workspaces: 1 } }"
    })
    AdminWorkspaceListResponse findWorkspaceList(String clientId,
                                                 String filter,
                                                 Pattern query,
                                                 String status,
                                                 LocalDate startDate,
                                                 LocalDate endDate,
                                                 Boolean isDeleted,
                                                 int offset,
                                                 int limit);

    @Aggregation(pipeline = {
            "{ $match: { client_id: ?0, workspace_id: ?1 } }",
            "{ $unwind: '$access_users' }",
            "{ $project: { _id: 0, access_users: 1 } }",
            "{ $replaceRoot: { newRoot: '$access_users' } }",
            "{ $match: {" +
                "$expr: {" +
                    "$cond: {" +
                        "if: { $eq: [?2, null] }," +
                        "then: { $in: ['$type', ['W', 'C', 'V']] }," +
                        "else: { $eq: ['$type', ?2] } } } } }",
            "{ $sort: { account: 1 } }",
            "{ $project: { type: 0 } }",
            "{ $lookup: {" +
                "from: 'user'," +
                "localField: 'user_id'," +
                "foreignField: 'user_id'," +
                "as: 'user_info' } }",
            "{ $project: { user_id: 1, account: 1, desc: { $getField: { field: 'desc', input: { $first: '$user_info' } } } } }",
            "{ $match: {" +
                "$expr: {" +
                    "$or: [" +
                        "{ $regexMatch: { input: '$account', regex: ?3 } }," +
                        "{ $regexMatch: { input: '$desc', regex: ?3 } }]}} }",
            "{ $facet: {" +
                "total_page: [" +
                    "{ $count: 'value' }," +
                    "{ $project: { value: { $cond: { if: { $eq: [{ $mod: ['$value', 5] }, 0] }, then: { $divide: ['$value', 5] }, else: { $add: [{$trunc: {$divide: ['$value', 5]} }, 1] } } } } }]," +
                "result_count: [{ $count: 'value' }]," +
                "users: [" +
                    "{ $project: { info: '$$ROOT' } }," +
                    "{ $skip: ?4 }," +
                    "{ $limit: ?5 }," +
                    "{ $replaceRoot: { newRoot: '$info' } }] } }",
            "{ $project: { total_page: '$total_page.value', result_count: '$result_count.value', users: '$users' } }",
            "{ $unwind: '$total_page' }",
            "{ $unwind: '$result_count' }"
    })
    AdminUserListForWorkspaceAssignmentResponse findAssignedUserListForWorkspaceAssignment(String clientId, ObjectId workspaceId, Character type, Pattern query, int offset, int limit);

    @Aggregation(pipeline = {
            "{ $match: { client_id: ?0, access_users: { $elemMatch: { user_id: { $in: ?1 } } } } }",
            "{ $project: { _id: 0, access_users: 1, projects: 1 } }",
            "{ $facet: {" +
                    "access_users: [" +
                        "{ $project: { value: '$access_users' } }," +
                        "{ $unwind: '$value' }," +
                        "{ $replaceRoot: { newRoot: '$value' } }," +
                        "{ $project: { user_id: 1 } }," +
                        "{ $group: { _id: '$user_id' } }]," +
                    "targets: [" +
                        "{ $project: { value: '$projects' } }," +
                        "{ $unwind: '$value' }," +
                        "{ $replaceRoot: { newRoot: '$value' } }," +
                        "{ $project: { jobs: 1 } }," +
                        "{ $unwind: '$jobs' }," +
                        "{ $replaceRoot: { newRoot: '$jobs' } }," +
                        "{ $match: { works: { $ne: null } } }," +
                        "{ $project: { works: 1 } }," +
                        "{ $unwind: '$works' }," +
                        "{ $replaceRoot: { newRoot: '$works' } }," +
                        "{ $project: { target_id: { $concatArrays: [['$worker_id'], ['$checker_id']] } } }," +
                        "{ $unwind: '$target_id' }," +
                        "{ $group: { _id: '$target_id' } }] } }",
            "{ $project: { working_users: { $setIntersection: ['$access_users._id', '$targets._id'] } } }",
            "{ $project: {" +
                "deletable_users: {" +
                    "$filter: {" +
                        "input: ?1," +
                        "cond: { $not: { $in: ['$$this', '$working_users'] } } } } } }"
    })
    DeletableUserListValue findDeletableUserList(String clientId, List<ObjectId> userIds);

    @Aggregation(pipeline = {
            "{ $match: { workspace_id: ?0,'access_users.user_id': ?1} }",
            "{ $project: { _id: 0, access_users: 1 } }",
            "{ $unwind: '$access_users' }",
            "{ $match: { 'access_users.user_id': ?1 } }",
            "{ $project: { 'access_users.type': 1 } }",
            "{ $replaceRoot: {newRoot: '$access_users'} }"
    })
    UserTypeResponse getAccessUserType(ObjectId workspaceId, ObjectId userId);

    @Aggregation(pipeline = {
            "{ $match: { client_id: ?0, workspace_id: ?1 } }",
            "{ $unwind: '$projects' }",
            "{ $replaceRoot: { newRoot: '$projects' } }",
            "{ $unwind: '$jobs' }",
            "{ $replaceRoot: { newRoot: '$jobs' } }",
            "{ $unwind: '$works' }",
            "{ $replaceRoot: { newRoot: '$works' } }",
            "{ $facet: {" +
                "data: [" +
                    "{ $match: {" +
                        "$expr: {" +
                            "$and: [" +
                                "{ $not: { $in: ['$work_status', ['CM', 'NU']] } }," +
                                "{ $or: [" +
                                    "{ $in: ['$worker_id', ?2] }," +
                                    "{ $in: ['$checker_id', ?2] }] }] } } }] } }",
            "{ $project: {" +
                "has_any_projects: {" +
                    "$map: {" +
                        "input: ?2," +
                        "as: 'user_id'," +
                        "in: {" +
                            "user_id: '$$user_id'," +
                            "result: {" +
                                "$gt: [" +
                                    "{ $size: {" +
                                        "$filter: {" +
                                            "input: '$data'," +
                                            "cond: {" +
                                                "$or: [" +
                                                    "{ $eq: ['$$this.worker_id', '$$user_id'] }," +
                                                    "{ $eq: ['$$this.checker_id', '$$user_id'] }] } } } }, 0] } } } } } }",
            "{ $unwind: '$has_any_projects' }",
            "{ $replaceRoot: { newRoot: '$has_any_projects' } }"
    })
    List<AdminWorkspaceJoinedProjectsValue> findAnyJoinedProjectsByUserIds(String clientId, ObjectId workspaceId, List<ObjectId> userIds);

    @Query(value = "{ client_id: ?0, workspace_id: ?1 }")
    @Update(value = "{ $pull: { access_users: { user_id: { $in: ?2 } } } }")
    void updateAccessUsersForUnassign(String clientId, ObjectId workspaceId, List<ObjectId> userId);

    @Query(value = "{ client_id: ?0 }")
    @Update(value = "{ $pull: { access_users: { user_id: { $in: ?1 } } } }")
    void updateAccessUsersForDeleteUser(String clientId, List<ObjectId> userId);
}
