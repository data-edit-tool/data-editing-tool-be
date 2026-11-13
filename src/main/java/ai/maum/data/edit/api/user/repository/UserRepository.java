package ai.maum.data.edit.api.user.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.regex.Pattern;

import ai.maum.data.edit.api.admin.user.dto.response.AdminUserListForWorkspaceAssignmentResponse;
import ai.maum.data.edit.api.user.UserEntity;

public interface UserRepository extends MongoRepository<UserEntity, ObjectId>, UserCustomRepository {
  void deleteByUserIdIn(List<ObjectId> userIds);

  boolean existsByAccount(String account);

  boolean existsByAccountAndIsAdmin(String account, Boolean isAdmin);

  boolean existsByIsAdmin(Boolean isAdmin);

  @Aggregation(pipeline = {
      "{ $match: { client_id: ?0, is_admin: false } }",
      "{ $lookup: {" +
          "from: 'workspace'," +
          "let: { user_id: '$user_id' }," +
          "pipeline: [" +
          "{ $match: { workspace_id: ?1 } }," +
          "{ $unwind: '$access_users' }," +
          "{ $project: { _id: 0, access_users: 1 } }," +
          "{ $replaceRoot: { newRoot: '$access_users' } }," +
          "{ $match: { $expr: { $eq: ['$user_id', '$$user_id'] } } }]," +
          "as: 'user_info' } }",
      "{ $project: { _id: 0, user_id: 1, account: 1, desc: 1, user_info: 1 } }",
      "{ $match: { $expr: { $eq: [{ $size: '$user_info' }, 0] } } }",
      "{ $match: {" +
          "$expr: {" +
          "$or: [" +
          "{ $regexMatch: { input: '$account', regex: ?2 } }," +
          "{ $regexMatch: { input: '$desc', regex: ?2 } }] } } }",
      "{ $project: { user_info: 0 } }",
      "{ $addFields: { users: { $arrayToObject: { $objectToArray: '$$ROOT' } } } }",
      "{ $sort: { account: 1 } }",
      "{ $facet: {" +
          "total_page: [" +
          "{ $count: 'value' }," +
          "{ $project: { value: { $cond: { if: { $eq: [{ $mod: ['$value', 5] }, 0] }, then: { $divide: ['$value', 5] }, else: { $add: [{$trunc: {$divide: ['$value', 5]} }, 1] } } } } }]," +
          "result_count: [{ $count: 'value' }]," +
          "users: [" +
          "{ $project: { info: '$$ROOT' } }," +
          "{ $skip: ?3 }," +
          "{ $limit: ?4 }," +
          "{ $replaceRoot: { newRoot: '$info' } }] } }",
      "{ $addFields: {" +
          "total_page: { $ifNull: [{ $arrayElemAt: ['$total_page', 0] }, { value: 0 }] }," +
          "result_count: { $ifNull: [{ $arrayElemAt: ['$result_count', 0] }, { value: 0 }] }," +
          "users: '$users' } }",
      "{ $project: { total_page: '$total_page.value', result_count: '$result_count.value', users: '$users' } }",
      "{ $unwind: '$total_page' }",
      "{ $unwind: '$result_count' }"
  })
  AdminUserListForWorkspaceAssignmentResponse findAvailableUserListForWorkspaceAssignment(String clientId,
      ObjectId workspaceId, Pattern account, int offset, int limit);

  UserEntity findByAccount(String account);

  UserEntity findByAccountAndIsAdmin(String account, Boolean isAdmin);

  UserEntity findByAccountAndPassword(String account, String password);

  UserEntity findByClientIdAndUserId(String clientId, ObjectId userId);

  List<UserEntity> findByClientIdAndUserIdIn(String clientId, List<ObjectId> userIds);

  UserEntity findByIsAdmin(Boolean isAdmin);

  UserEntity findByIsAdminAndRecoveryCode(Boolean isAdmin, String recoveryCode);

  //  @Aggregation(pipeline = {
  //    "{ $match: { client_id: ?0, is_admin: false } }",
  //    "{ $sort: { created: -1 } }",
  //    "{ $project: { _id: 0, client_id: 0, updated: 0 } }",
  //    "{ $facet: {" +
  //      "member_count: [{ $count: 'value' }]," +
  //      "users: [" +
  //        "{ $project: { info: '$$ROOT' } }," +
  //        "{ $replaceRoot: { newRoot: '$info' } }," +
  //        "{ $match: {" +
  //          "?1: ?2," +
  //          "$expr: {" +
  //            "$cond: {" +
  //              "if: { $and: [{ $ne: [?3, null] }, { $ne: [?4, null] }] }," +
  //              "then: { $and: [{ $gte: ['$created', ?3] }, { $lt: ['$created', ?4] }] }," +
  //              "else: {" +
  //                "$cond: {" +
  //                  "if: { $ne: [?3, null] }," +
  //                  "then: { $gte: ['$created', ?3] }," +
  //                  "else: {" +
  //                    "$cond: {" +
  //                      "if: { $ne: [?4, null] }," +
  //                      "then: { $lt: ['$created', ?4] }," +
  //                      "else: true } } } } } } } }] } }",
  //    "{ $project: { member_count: { $arrayElemAt: ['$member_count.value', 0] }, users: 1 } }",
  //    "{ $facet: {" +
  //      "member_count: [{ $project: { value: '$member_count' } }]," +
  //      "total_page: [" +
  //        "{ $project: { value: { $size: '$users' } } }," +
  //        "{ $project: { value: { $cond: { if: { $eq: [{ $mod: ['$value', 10] }, 0] }, then: { $divide: ['$value', 10] }, else: { $add: [{$trunc: {$divide: ['$value', 10]} }, 1] } } } } }]," +
  //      "result_count: [{ $project: { value: { $size: '$users' } } }]," +
  //      "users: [" +
  //        "{ $project: { info: '$users' } }," +
  //        "{ $unwind: '$info' }," +
  //        "{ $replaceRoot: { newRoot: '$info' } }," +
  //        "{ $skip: ?5 }," +
  //        "{ $limit: ?6 }] } }",
  //    "{ $project: {" +
  //      "member_count: { $arrayElemAt: ['$member_count.value', 0] }," +
  //      "total_page: { $arrayElemAt: ['$total_page.value', 0] }," +
  //      "result_count: { $arrayElemAt: ['$result_count.value', 0] }," +
  //      "users: 1 } }"
  //  })
  //  AdminUserListResponse findUserList(String clientId,
  //                                     String filter,
  //                                     Pattern query,
  //                                     LocalDate startDate,
  //                                     LocalDate endDate,
  //                                     int offset,
  //                                     int limit);

  UserEntity findByUserId(ObjectId userId);
}
