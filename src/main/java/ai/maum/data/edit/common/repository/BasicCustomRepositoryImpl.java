package ai.maum.data.edit.common.repository;

import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;
import java.util.Map;

/**
 * 사용법 : @see <a href="https://mindslab.atlassian.net/wiki/spaces/4BIZBACKEN/pages/9587949587/QueryUtil#QueryUtil%EC%9D%84-%EC%82%AC%EC%9A%A9%ED%95%98%EA%B8%B0-%EC%9C%84%ED%95%9C-%EA%B8%B0%EB%B3%B8-%EC%84%B8%ED%8C%85">BasicCustomRepositoryImpl</a>
 *
 * @author bhr
 */
public class BasicCustomRepositoryImpl implements BasicCustomRepository {
  protected final MongoTemplate mongoTemplate;

  public BasicCustomRepositoryImpl(MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  @Override
  public <T> List<T> findAllByObjectId(Class<T> entity, String key, String value) {
    Query query = findByObjectId(key, value);
    return mongoTemplate.find(query, entity);
  }

  @Override
  public <T> List<T> findAllByObjectId(Class<T> entity, List<String> key, List<String> fields, String value) {
    Query query = findByObjectId(key, fields, value);
    return mongoTemplate.find(query, entity);
  }

  @Override
  public <T> List<T> findAllWithFieldsBy(Class<T> entity, Map<String, Object> where, List<String> fields,
      List<String> prefixOfFields) {
    Query query = findWithFieldsBy(where, fields, prefixOfFields);
    return mongoTemplate.find(query, entity);
  }

  @Override
  public <T> T findOneBy(Class<T> entity, Map<String, Object> where) {
    Query query = findBy(where);
    return mongoTemplate.findOne(query, entity);
  }

  @Override
  public <T> T findOneByObjectId(Class<T> entity, String key, String value) {
    Query query = findByObjectId(key, value);
    return mongoTemplate.findOne(query, entity);
  }

  @Override
  public <T> T findOneWithFieldsBy(Class<T> entity, Map<String, Object> where, List<String> fields) {
    Query query = findWithFieldsBy(where, fields, null);
    return mongoTemplate.findOne(query, entity);
  }

  /**
   * 출력하고 싶은 field만 포함하여 query할 때 사용한다.
   *
   * @param entity
   * @param where
   * @param fields
   * @param prefixOfFields
   * @param <T>
   * @return
   * @link <a href="https://mindslab.atlassian.net/wiki/spaces/4BIZBACKEN/pages/9587949587/QueryUtil#findByFields">findByFields</a>
   */
  @Override
  public <T> T findOneWithFieldsBy(Class<T> entity, Map<String, Object> where, List<String> fields,
      List<String> prefixOfFields) {
    Query query = findWithFieldsBy(where, fields, prefixOfFields);
    return mongoTemplate.findOne(query, entity);
  }

  @Override
  public <T> int getSizeBy(Class<T> entity, Map<String, Object> where) {
    Query query = findBy(where);
    return mongoTemplate.find(query, entity).size();
  }

  /**
   * 특정 field 제거
   */
  @Override
  public <T> boolean unsetField(Class<T> entity, Query query, String fieldName) {
    Update update = new Update().unset(fieldName);

    return mongoTemplate.updateFirst(query, update, entity).getModifiedCount() > 0;
  }

  /**
   * 주어진 조건에 맞는 여러 Field/Document를 한 번에 Update하기 위해 사용
   *
   * @see <a href="https://mindslab.atlassian.net/wiki/spaces/4BIZBACKEN/pages/9587949587/QueryUtil#updateBulk">updateBulk</a>
   */
  @Override
  public <T> int updateBulk(Class<T> entity, Map<String, Object> where, Map<String, Map<String, Object>> arrayFilters,
      Map<String, Map<String, Object>> set) {
    BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, entity);
    Query query = new Query();
    query = findBy(query, where);

    for (Map.Entry<String, Map<String, Object>> updateEntry : set.entrySet()) {
      Update update = new Update();
      for (Map.Entry<String, Object> fieldEntry : updateEntry.getValue().entrySet()) {
        update.set(fieldEntry.getKey(), fieldEntry.getValue());
      }

      update = setUpdateWithFilters(update, arrayFilters.get(updateEntry.getKey()));
      bulkOps.updateOne(query, update);
    }
    return bulkOps.execute().getModifiedCount();
  }

  /**
   * arraysFilter와 함께 update할 때 사용한다.
   * 단, 위와 다른 점은 한 번에 여러 개를 update할 때 사용한다.
   * 사용 법은 위와 같다.
   *
   * @param entity
   * @param where
   * @param arrayFilters
   * @param set
   * @param push
   * @param <T>
   * @return
   * @link <a href="https://mindslab.atlassian.net/wiki/spaces/4BIZBACKEN/pages/9587949587/QueryUtil#updateOneWithFilters1">updateOneWithFilters</a>
   */
  @Override
  public <T> boolean updateManyWithFilters(Class<T> entity, Map<String, Object> where, Map<String, Object> arrayFilters,
      Map<String, Object> set, Map<String, Object> push) {
    Query query = new Query();
    query = findBy(query, where);

    return mongoTemplate.updateMulti(
        query
        , setUpdate(arrayFilters, set, push)
        , entity
    ).getModifiedCount() > 0;
  }

  /**
   * Update를 할 때 사용합니다.
   * 단, 위의 'updateManyWithFilters'와 다른 점은, query 조건을 외부에서 세팅한 후 update를 합니다.
   *
   * @param entity
   * @param query
   * @param arrayFilters
   * @param set
   * @param push
   * @param <T>
   * @return
   * @see <a href="https://mindslab.atlassian.net/wiki/spaces/4BIZBACKEN/pages/9587949587/QueryUtil#updateOneWithFilters2">updateOneWithFilters</a>
   */
  @Override
  public <T> boolean updateManyWithFilters(Class<T> entity, Query query, Map<String, Object> arrayFilters,
      Map<String, Object> set, Map<String, Object> push) {
    return mongoTemplate.updateMulti(
        query
        , setUpdate(arrayFilters, set, push)
        , entity
    ).getModifiedCount() > 0;
  }

  /**
   * arraysFilter와 함께 update할 때 사용한다.
   *
   * @param entity
   * @param where
   * @param arrayFilters
   * @param set
   * @param push
   * @param <T>
   * @return
   * @link <a href="https://mindslab.atlassian.net/wiki/spaces/4BIZBACKEN/pages/9587949587/QueryUtil#updateOneWithFilters1">updateOneWithFilters</a>
   */
  @Override
  public <T> boolean updateOneWithFilters(Class<T> entity, Map<String, Object> where, Map<String, Object> arrayFilters,
      Map<String, Object> set, Map<String, Object> push) {
    Query query = new Query();
    query = findBy(query, where);

    return mongoTemplate.updateFirst(
        query
        , setUpdate(arrayFilters, set, push)
        , entity
    ).getModifiedCount() > 0;
  }

  /**
   * Update를 할 때 사용합니다.
   * 단, 위의 'updateManyWithFilters'와 다른 점은, query 조건을 외부에서 세팅한 후 update를 합니다.
   *
   * @param entity
   * @param query
   * @param arrayFilters
   * @param set
   * @param push
   * @param <T>
   * @return
   * @link
   */
  @Override
  public <T> boolean updateOneWithFilters(Class<T> entity, Query query, Map<String, Object> arrayFilters,
      Map<String, Object> set, Map<String, Object> push) {
    return mongoTemplate.updateFirst(
        query
        , setUpdate(arrayFilters, set, push)
        , entity
    ).getModifiedCount() > 0;
  }
}
