package ai.maum.data.edit.common.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Field;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.lang.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import ai.maum.data.edit.common.exception.BaseException;
import ai.maum.data.edit.common.util.ParsingUtil;
import ai.maum.data.edit.common.util.StringUtil;

public interface BasicCustomRepository {

    <T> T findOneByObjectId(Class<T> entity, String key, String value);

    <T> T findOneBy(Class<T> entity, Map<String, Object> where);

    <T> int getSizeBy(Class<T> entity, Map<String, Object> where);

    <T> List<T> findAllByObjectId(Class<T> entity, String key, String value);

    <T> List<T> findAllByObjectId(Class<T> entity, List<String> key, List<String> fields, String value);

    <T> List<T> findAllWithFieldsBy(Class<T> entity, Map<String, Object> where, List<String> fields, @Nullable List<String> prefix);

    <T> T findOneWithFieldsBy(Class<T> entity, Map<String, Object> where, List<String> fields);

    <T> T findOneWithFieldsBy(Class<T> entity, Map<String, Object> where, List<String> fields, @Nullable List<String> prefix);

    <T> boolean updateOneWithFilters(Class<T> entity, Map<String, Object> where, @Nullable  Map<String, Object> arrayFilters, @Nullable Map<String, Object> set , @Nullable Map<String, Object> push);

    <T> boolean updateManyWithFilters(Class<T> entity, Map<String, Object> where, @Nullable  Map<String, Object> arrayFilters, @Nullable Map<String, Object> set , @Nullable Map<String, Object> push);

    <T> boolean updateOneWithFilters(Class<T> entity, Query query, @Nullable Map<String, Object> arrayFilters, @Nullable Map<String, Object> set, @Nullable Map<String, Object> push);

    <T> boolean updateManyWithFilters(Class<T> entity, Query query, @Nullable Map<String, Object> arrayFilters, @Nullable Map<String, Object> set, @Nullable Map<String, Object> push);

    <T> int updateBulk(Class<T> entity, Map<String, Object> where, @Nullable Map<String, Map<String, Object>> arrayFilters, @Nullable Map<String, Map<String, Object>> set);

    <T> boolean unsetField(Class<T> entity, Query query, String fieldName);

    // *****************************************************//
    // **** Query condition을 조작할 수 있는 method들이다. **** //
    /**
     * find할 때 가져올 field의 prefix를 세팅한다.
     *
     * @param prefixs
     * @return
     */
    private String setFindFieldPrefix(List<String> prefixs) {
        StringBuilder prefix = new StringBuilder();
        for(String str : prefixs) {
            prefix.append(str).append(".");
        }
        return prefix.toString();
    }

    /**
     * find할 때 여러 field를 가져오기 위해 사용한다.
     * @param prefixOfFields
     * @param fields
     * @return
     */
    private void setFindFieldsWithPrefix(Field f, List<String> fields, List<String> prefixOfFields) {
        if(prefixOfFields != null && prefixOfFields.size() > 0) {
            for(String field : fields) {
                f.include( setFindFieldPrefix(prefixOfFields) + field );
            }
        }
        if (fields != null && fields.size() > 0) {
            for(String field : fields) {
                f.include(field);
            }
        }
    }

    /**
     * find할 때 특정 field 하나만 가져오고 싶을 때 사용한다.
     * @param fieldDepth
     * @return
     */
    private String setFindField(List<String> fieldDepth) {
        StringBuilder field = new StringBuilder();
        for(String str : fieldDepth) {
            field.append(str).append(".");
        }
        field.deleteCharAt(field.lastIndexOf("."));

        return field.toString();
    }

    /**
     * Update 전 update 형식을 세팅하기 위해 사용한다.
     *
     * @param arrayFilters
     * @param set
     * @param push
     * @return
     */
    default Update setUpdate(Map<String, Object> arrayFilters, Map<String, Object> set , Map<String, Object> push) {
        Update update = new Update();

        if(set != null)
            update = setBeforeUpdate(set);
        if(push != null)
            update = pushBeforeUpdate(push);
        if(arrayFilters != null)
            update = setUpdateWithFilters(update, arrayFilters);

        return update;
    }

    /**
     * MongoDB $eq
     * ObjectId를 이용하여 조회해야 할 때 사용한다.
     *
     * 아래 쿼리와 같다.
     * db.workspace.find({"workspace_id": ObjectId('64901693000000000088dcf1')});
     *
     * @param key
     * @param value
     * @return
     */
    default Query findByObjectId(String key, String value) {
        Query query = new Query();
        if( !StringUtil.valueOf(value).equals("") ) {
            query.addCriteria(Criteria.where(key).is( new ObjectId(value) ));
        }
        return query;
    }

    /**
     * MongoDB $eq
     * find할 때 array형식의 field를 가져올 때 사용한다.
     *
     * 아래 쿼리와 같다.
     * db.workspace.find(
     *     { "projects.project_id": ObjectId('64949750000000000071b9d8') }
     *     ,{_id: 0, "projects.jobs.$": 1}
     * )
     *
     * @param key : field depth
     * @param fields : 출력할 field 목록
     * @param value : 비교 값
     * @return
     */
    default Query findByObjectId(List<String> key, List<String> fields, String value) {
        Query query = new Query();
        if((key != null && key.size() > 0) && value != null) {
            query.addCriteria(Criteria.where( setFindField(key) ).is( new ObjectId(value) ));
            query.fields().exclude("_id").include( setFindField(fields)+".$" );
        }
        return query;
    }

    /**
     * MongoDB $eq
     * find할 때 여러 field를 가져오기 위해 사용한다.
     * 단, field 앞에 붙을 prefix가 필요할 때 사용한다.
     *
     * 아래 쿼리와 같다.
     * db.workspace.find(
     *     {
     *          "projects.project_id": ObjectId('64949750000000000071b9d8')
     *          ...
     *      }
     *     ,{ _id: 0
     *          , "projects.jobs.job_id": 1
     *          ...
     *      }
     * )
     * @param where : [field, value] 한 쌍의 where 조건
     * @param fields : 출력할 field 목록(위 예제에서 'job_id', 'job_name'에 속한다.)
     * @param prefixOfFields : 출력할 field의 상위 field 명칭(위 예제에서 'projects.jobs'에 속한다.)
     * @return
     */
    default Query findWithFieldsBy(Map<String, Object> where, List<String> fields, List<String> prefixOfFields) {
        Query query = new Query();
        if(where != null && where.size() > 0) {
            where.forEach( (k, v) -> {
                query.addCriteria(Criteria.where( k ).is( ParsingUtil.convertType(v) ));
            });
            setFindFieldsWithPrefix( query.fields().exclude("_id"), fields, prefixOfFields);
        }
        return query;
    }


    /**
     * MongoDB $eq
     * find 조건에 null 비교가 필요할 때 사용한다.
     *
     * 아래 쿼리와 같다.
     * db.workspace.find(
     *     { "projects.project_id": ObjectId('64949750000000000071b9d8') }
     *     ,{ "projects.jobs.job_id": null }
     *     ...
     * )
     *
     * @param where : [field, value] 한 쌍의 where 조건
     * @return
     */
    default Query findBy(Map<String, Object> where) {
        Query query = new Query();
        if(where != null) {
            where.forEach((k, v) -> {
                if (v != null)
                    query.addCriteria(Criteria.where(k).is(ParsingUtil.convertType(v)));
                else
                    query.addCriteria(Criteria.where(k).isNull());
            });
        }
        return query;
    }

    /**
     * MongoDB $eq
     * where 조건이 하나일 때 사용한다.
     *
     * 아래 쿼리와 같다.
     * db.workspace.find({"field_name": "문자열")});
     *
     * @param key
     * @param value
     * @return
     */
    default Query findBy(String key, Object value) {
        Query query = new Query();
        if(value != null) {
            query.addCriteria(Criteria.where(key).is( ParsingUtil.convertType(value) ));
        }
        return query;
    }

    /**
     * MongoDB $eq
     * where 조건이 여러 개일 때 사용한다.
     *
     * 아래 쿼리와 같다.
     * db.workspace.find({
     *      "field_name1": "문자열"
     *      , "field_name2": "문자열"
     *      });
     *
     * @param where : where 조건 key, value Map
     */
    default Query findBy(Query query, Map<String, Object> where) {
        where.forEach((k,v) -> {
            if(v!=null && !StringUtil.valueOf(v).equals("")) {
                query.addCriteria(Criteria.where(k).is( ParsingUtil.convertType(v) ));
            }
        });

        return query;
    }

    /**
     * MongoDB $eq
     * Date type의 field를 조건으로 조회할 때 사용한다.
     *
     * 아래 쿼리와 같다.
     * db.workspace.find({ start_date: new Date('2023-06-20') });
     *
     * @param query
     * @param where
     */
    default Query addCriteriaIsDate(Query query, Map<String, Object> where) {
        where.forEach((k,v) -> {
            if(v != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

                Date value = null;
                try {
                    value = dateFormat.parse( StringUtil.valueOf(v) );
                } catch (ParseException e) {
                    throw BaseException.of(e);
                }

                query.addCriteria(Criteria.where(k).is(value));
            }
        });

        return query;
    }

    /**
     * MongoDB $regex
     *
     * 아래 쿼리와 같다.
     * db.workspace.find({workspace_name: {$regex : '페이스명2'}});
     *
     * @param query
     * @param where
     */
    default Query addCriteriaRegex(Query query, Map<String, Object> where) {
        where.forEach((k,v) -> {
            String val = StringUtil.valueOf(v);
            if(!val.equals("")) {
                query.addCriteria(Criteria.where(k).regex(val));
            }
        });

        return query;
    }

    /**
     * update할 때 사용한다.
     *
     * db.workspace.updateOne(
     *     {'field': ObjectId('6494279c538ef1332cb2b2bf')}
     *     ,{$set: {'key': 'value'}}
     * )
     * @param set : update할 field와 value 세트
     * @return
     */
    default Update setBeforeUpdate(Map<String, Object> set) {
        Update update = new Update();
        set.forEach( (k, v) -> {
            update.set(k, ParsingUtil.convertType(v));
        });
        return update;
    }

    /**
     * 배열 field에 값을 추가할 때 사용한다.
     *
     * db.workspace.updateOne(
     *     {'workspace_id': ObjectId('6491330e000000000067a9e8')}
     *     ,{$push:
     *         {'projects': {
     *                 'key': 'value'
     *                 ...
 *             }}
     *     }
     * )
     * @param push : 배열 field에 추가할 값
     * @return
     */
    default Update  pushBeforeUpdate(Map<String, Object> push) {
        Update update = new Update();
        push.forEach( (k, v) -> {
            update.push(k, ParsingUtil.convertType(v));
        });
        return update;
    }

    /**
     * arrayFilters를 추가할 때 사용한다.
     *
     * @param arrayFilters : arrayFilters로써 사용될 field와 value 셋
     * @return
     */
    default Update setUpdateWithFilters(Update update, Map<String, Object> arrayFilters) {
        if(arrayFilters != null) {
            arrayFilters.forEach((k, v) -> {
                if (v != null && !StringUtil.valueOf(v).equals("")) {
                    update.filterArray(Criteria.where(k).is( ParsingUtil.convertType(v) ));
                } else {
                    update.filterArray(Criteria.where(k).isNull());
                }
            });
        }

        return update;
    }
    // *****************************************************//
}
