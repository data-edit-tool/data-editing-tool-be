package ai.maum.data.edit.api.category.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

import ai.maum.data.edit.api.admin.category.dto.response.AdminCategoryResponse;
import ai.maum.data.edit.api.category.CategoryEntity;
import ai.maum.data.edit.api.category.dto.response.CategoryResponse;

public interface CategoryRepository extends MongoRepository<CategoryEntity, ObjectId> {

  // FIXME 현재 depth3만 sorting해놨음..
  @Aggregation(pipeline = {
      "{$match: {client_ctg_id: ?0}}"
      , "{ $unwind: '$categories' }"
      , "{ $project: { _id: 0, categories: 1 } }"
      , "{ $replaceRoot: { newRoot: '$categories' } }"
      , "{ $match: { 'ctg_name': ?1 } }"
      , "{ $project: { ctg_depth: 1 } }"
      , "{ $unwind: '$ctg_depth' }"
      , "{ $replaceRoot: { newRoot: '$ctg_depth' } }"
      // 밑에 $facet - depth3에서 unwind하여 sorting한 후 grouping할 때, ctg_depth가 비어있으면 2depth의 값이 사라지기 때문에, 빈 객체{}라도 넣어줘야 함.
      , "{$addFields: {" +
      "ctg_depth: {" +
      "$cond: {" +
      "if: {$eq: [{$size: '$ctg_depth'}, 0]}" +
      ",then: [{}]" +
      ",else: '$ctg_depth'" +
      "}}" +
      "}}"
      // 3depth의 카테고리를 정렬하기 위한 facet
      , "{$facet: {" +
      // 2depth 그대로 가져가기 - 3depth 데이터가 없을 경우 2depth의 데이터를 보여줘야 함.
      "depth2: [ {$sort: {sort: 1}} ]" + // 2depth 정렬
      // 3depth에 대해서 정렬한 후 2depth의 ctg_depth에 다시 세팅하기
      ",depth3: [" +
      // 3depth에 2depth(verbal) 카테고리 정보 넣기
      "{$addFields: {'ctg_depth.up_ctg_id': '$$ROOT.ctg_id', 'ctg_depth.up_ctg_name': '$$ROOT.ctg_name', 'ctg_depth.up_sort': '$$ROOT.sort', 'ctg_depth.up_parent_ctg_id': '$$ROOT.parent_ctg_id'}}" +
      // 3depth 카테고리 풀어헤쳐서 정렬
      ",{$unwind: '$ctg_depth'}" +
      ",{$replaceRoot: {newRoot: '$ctg_depth'}}" +
      ",{$sort: {sort: 1}}" +
      // 2depth의 ctg_depth가 정렬된 3depth의 카테고리로 되도록 그루핑
      ",{$group: {" +
      "_id: '$up_ctg_id'" + // 2depth ctg_id를 기준으로 3depth 그루핑
      ",ctg_id: {$first: '$$ROOT.up_ctg_id'}" + // 2단계 카테고리 id
      ",ctg_name: {$first: '$$ROOT.up_ctg_name'}" + // 2단계 카테고리 name
      ",sort: {$first: '$$ROOT.up_sort'}" +
      ",parent_ctg_id: {$first: '$$ROOT.up_parent_ctg_id'}" +
      ",ctg_depth: {$push: {ctg_id: '$$ROOT.ctg_id', ctg_name: '$$ROOT.ctg_name', ctg_depth: '$$ROOT.ctg_depth'}}" +
      // 3단계 카테고리 설정
      "}}" +
      ",{$sort: {sort: 1}}" + // 2depth 정렬
      "]" +
      "}}"
      // 3depth가 있을 경우 3depth의 카테고리 목록을, 없을 경우 2depth의 카테고리 목록을 보여준다.
      , "{$project:" +
      "{ctg_depth: {" +
      "$cond: {" +
      "if: {$gt: [{$size: '$depth3'}, 0]}" +
      ",then: '$depth3'" +
      ",else: '$depth2'" +
      "}" +
      "} }" +
      "}"
      , "{ $unwind: '$ctg_depth' }"
      , "{ $replaceRoot: {newRoot: '$ctg_depth'} }"
      , "{ $project: {_id: 0, ctg_depth: 1, ctg_id: 1, ctg_name: 1, parent_ctg_id: 1} }"
  })
  List<CategoryResponse> findByClientCtgIdAndCtgName(ObjectId clientCtgId, String ctgName);

  @Aggregation(pipeline = {
      "{ $match: { client_id: ?0, tool_types: { $elemMatch: { $eq: ?1 } } } }",
      "{ $project: { _id: 0, client_ctg_id: 1, client_ctg_name: 1 } }"
  })
  List<AdminCategoryResponse> findCategoryListByClient(String clientId, String toolTypeCode);

  @Aggregation(pipeline = {
      "{$match : { client_ctg_id: ?0 } }"
      , "{ $unwind: '$categories' }"
      , "{ $project: { _id: 0, categories: 1 } }"
      , "{ $replaceRoot: { newRoot: '$categories' } }"
      , "{ $match: { 'ctg_depth.ctg_id': ?1 } }"
      , "{ $unwind: '$ctg_depth' }"
      , "{ $replaceRoot: { newRoot: '$ctg_depth' } }"
      , "{ $match: { 'ctg_id': ?1 } }"
      , "{ $project: { ctg_depth: 1 } }"
      , "{ $unwind: '$ctg_depth' }"
      , "{ $replaceRoot: { newRoot: '$ctg_depth' } }"
  })
  List<CategoryResponse> findChildCategoryByParentCtgId(ObjectId clientCtgId, ObjectId ctgId);
}
