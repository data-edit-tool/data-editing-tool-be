package ai.maum.data.edit.common.repository;

import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AddFieldsOperation;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationExpression;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ArithmeticOperators;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.data.mongodb.core.aggregation.BooleanOperators;
import org.springframework.data.mongodb.core.aggregation.ComparisonOperators;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators;
import org.springframework.data.mongodb.core.aggregation.FacetOperation;
import org.springframework.data.mongodb.core.aggregation.LimitOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.SkipOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.aggregation.StringOperators;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import ai.maum.data.edit.common.util.ParsingUtil;
import lombok.Getter;

/**
 * @author bhr
 * @link <a href="https://mindslab.atlassian.net/wiki/spaces/4BIZBACKEN/pages/9587949587/QueryUtil#Aggregation%EC%9D%84-%EC%82%AC%EC%9A%A9%ED%95%98%EA%B8%B0-%EC%9C%84%ED%95%9C-%EA%B8%B0%EB%B3%B8-%EC%84%B8%ED%8C%85">Aggregation을 사용하기 위한 기본 세팅</a>
 */
public class CustomAggregation {

  public static ArithmeticOperators.Add add(String field, Integer value) {
    return ArithmeticOperators.Add.valueOf(field).add(value);
  }

  public static ArithmeticOperators.Add add(AggregationExpression expression, Integer value) {
    return ArithmeticOperators.Add.valueOf(expression).add(value);
  }

  public static BooleanOperators.And and(Object... expressions) {
    return BooleanOperators.And.and(expressions);
  }

  public static Criteria and(Criteria... criteria) {
    return new Criteria().andOperator(criteria);
  }

  /**
   * ============ Aggregation 표현 모음 영역 ============
   **/
  public static ArrayOperators.ArrayElemAt arrayElemAt(String arrayField, int index) {
    return ArrayOperators.ArrayElemAt.arrayOf(arrayField).elementAt(index);
  }

  public static ConditionalOperators.Cond cond(AggregationExpression when, Object then, Object otherwise) {
    return ConditionalOperators.Cond.newBuilder()
        .when(
            when
        )
        .then(
            then
        )
        .otherwise(
            otherwise
        );
  }

  public static ConditionalOperators.Cond cond(Criteria when, Object then, Object otherwise) {
    return ConditionalOperators.Cond.newBuilder()
        .when(when)
        .then(then)
        .otherwise(otherwise);
  }

  public static ConditionalOperators.Cond.WhenBuilder cond() {
    return ConditionalOperators.Cond.newBuilder();
  }

  public static ArithmeticOperators.Divide divide(String field, Integer value) {
    return ArithmeticOperators.Divide.valueOf(field).divideBy(value);
  }

  public static ComparisonOperators.Eq eq(String field, Object value) {
    return ComparisonOperators.Eq.valueOf(field).equalToValue(value);
  }

  public static ComparisonOperators.Eq eq(AggregationExpression expression, Object value) {
    return ComparisonOperators.Eq.valueOf(expression).equalToValue(value);
  }

  public static Criteria expr(Document document) {
    return Criteria.where("$expr").is(document);
  }

  public static ArrayOperators.Filter filter(String input, String as, AggregationExpression cond) {
    return ArrayOperators.Filter.filter(input)
        .as(as)
        .by(cond);
  }

  public static ComparisonOperators.Gt gt(String field, Object greaterThanValue) {
    return ComparisonOperators.Gt.valueOf(field).greaterThanValue(greaterThanValue);
  }

  public static ComparisonOperators.Gte gte(String field, Object greaterThanEqualToValue) {
    return ComparisonOperators.Gte.valueOf(field).greaterThanEqualToValue(greaterThanEqualToValue);
  }

  public static Facet initFacet() {
    return new Facet();
  }

  public static ComparisonOperators.Lt lt(String field, Object lessThanValue) {
    return ComparisonOperators.Lt.valueOf(field).lessThanValue(lessThanValue);
  }

  public static ComparisonOperators.Lte lte(String field, Object lessThanEqualToValue) {
    return ComparisonOperators.Lte.valueOf(field).lessThanEqualToValue(lessThanEqualToValue);
  }

  public static ArithmeticOperators.Mod mod(String field, Integer value) {
    return ArithmeticOperators.Mod.valueOf(field).mod(value);
  }

  public static ComparisonOperators.Ne ne(String field, Object value) {
    return ComparisonOperators.Ne.valueOf(field).notEqualToValue(value);
  }

  public static BooleanOperators.Or or(Object... expressions) {
    return BooleanOperators.Or.or(expressions);
  }

  public static Criteria or(Criteria... criteria) {
    return new Criteria().orOperator(criteria);
  }

  public static Criteria regex(String field, Pattern pattern) {
    return Criteria.where(field).regex(pattern);
  }

  public static StringOperators.RegexMatch regexMatch(String field, String regex) {
    return StringOperators.RegexMatch.valueOf(field).regex(regex);
  }

  public static StringOperators.RegexMatch regexMatch(AggregationExpression expression, String regex) {
    return StringOperators.RegexMatch.valueOf(expression).regex(regex);
  }

  public static Pipeline setAggregation(String collection) {
    return new Pipeline(collection);
  }

  public static Pipeline setPipline() {
    return new Pipeline();
  }

  public static StringOperators.ToLower toLower(String field) {
    return StringOperators.ToLower.lowerValueOf(field);
  }

  public static StringOperators.ToUpper toUpper(String field) {
    return StringOperators.ToUpper.upperValueOf(field);
  }

  public static ArithmeticOperators.Trunc trunc(AggregationExpression expression) {
    return ArithmeticOperators.Trunc.truncValueOf(expression);
  }

  /**
   * aggregation facet pipeline을 만들기 위한 class
   *
   * @author bhr
   */
  @Getter
  public static class Facet {
    private AggregationOperation[] aggregationOperation; // facet pipeline
    private String as; // facet 명칭
    private List<Facet> facetList = new ArrayList<>(); // 작성한 facet 모음

    private Facet() {
      this.aggregationOperation = null;
      this.as = null;
    }

    private Facet(String as, AggregationOperation[] aggregationOperation) {
      this.aggregationOperation = aggregationOperation;
      this.as = as;
    }

    public Facet setFacet(String as, AggregationOperation[] a) {
      facetList.add(new Facet(as, a));
      return this;
    }
  } // Facet class

  /**
   * aggregation expr pipeline을 작성하기 위한 Builder class
   */
  public static class Expr {
    public static Document and(Object... values) {
      return new Document("$and", Arrays.asList(values));
    }

    public static Document cond(Object... values) {
      return new Document("$cond", Arrays.asList(values));
    }

    public static Document eq(Object field, Object value) {
      return new Document("$eq", Arrays.asList(field, value));
    }

    public static Document gt(Object field, Object value) {
      return new Document("$gt", Arrays.asList(field, value));
    }

    public static Document gte(Object field, Object value) {
      return new Document("$gte", Arrays.asList(field, value));
    }

    public static Document lt(Object field, Object value) {
      return new Document("$lt", Arrays.asList(field, value));
    }

    public static Document lte(Object field, Object value) {
      return new Document("$lte", Arrays.asList(field, value));
    }

    public static Document ne(Object field, Object value) {
      return new Document("$ne", Arrays.asList(field, value));
    }

    public static Document or(Object... values) {
      return new Document("$or", Arrays.asList(values));
    }

    public static Document regex(String field, Object pattern) {
      return new Document(field, new Document("$regex", pattern));
    }
  }

  /**
   * aggregation pipeline을 작성하기 위한 Builder class
   *
   * @author bhr
   */
  public static class Pipeline {
    private List<AggregationOperation> pipeline = new ArrayList<>(); // aggregation pipeline
    private final String collection; // 조회할 collection name

    private Pipeline() { // facet pipeline 작성 시 호출.
      this.collection = null;
    }

    private Pipeline(String collection) { // aggregation을 하기 위해 호출.
      this.collection = collection;
    }

    public Pipeline addFields(String field, Object value) {
      AddFieldsOperation addFields = Aggregation.addFields().addField(field).withValueOf(value).build();
      pipeline.add(addFields);
      return this;
    }

    public <T> T aggregation(Class<T> entity, MongoTemplate mongoTemplate) {
      Aggregation aggregation = Aggregation.newAggregation(pipeline);
      AggregationResults<T> result = mongoTemplate.aggregate(aggregation, collection, entity);
      List<T> s = result.getMappedResults();
      return s == null || s.isEmpty() ? null : s.get(0);
    }

    public <T> List<T> aggregationList(Class<T> entity, MongoTemplate mongoTemplate) {
      Aggregation aggregation = Aggregation.newAggregation(pipeline);
      AggregationResults<T> result = mongoTemplate.aggregate(aggregation, collection, entity);
      return result.getMappedResults();
    }

    public AggregationOperation[] build() {
      return pipeline.toArray(new AggregationOperation[0]);
    }

    public Pipeline count(String as) {
      pipeline.add(Aggregation.count().as(as));
      return this;
    }

    /**
     * @param f
     * @return
     * @link <a href="https://mindslab.atlassian.net/wiki/spaces/4BIZBACKEN/pages/9587949587/QueryUtil#facet">facet</a>
     * @see Facet
     */
    public Pipeline facet(Facet f) {
      FacetOperation result = null;
      List<Facet> facets = f.getFacetList();
      if (facets != null && facets.size() > 0) {
        Facet facet = facets.get(0);

        result = Aggregation.facet(facet.getAggregationOperation()).as(facet.getAs());
        for (int i = 1; i < facets.size(); i++) {
          facet = facets.get(i);
          result = result.and(facet.getAggregationOperation()).as(facet.getAs());
        }
      }

      pipeline.add(result);
      return this;
    }

    public Pipeline limit(long maxElements) {
      pipeline.add(new LimitOperation(maxElements));
      return this;
    }

    /**
     * match 조건 key, value를 번갈아 가며 배열 형식으로 무한 추가 가능하다.
     *
     * @param keyOrValue : key,value,key,value ...
     * @return
     * @link <a href="https://mindslab.atlassian.net/wiki/spaces/4BIZBACKEN/pages/9587949587/QueryUtil#match">match</a>
     */
    public Pipeline match(Object... keyOrValue) {
      Criteria ctr = new Criteria();
      int num = 1;
      for (Object o : keyOrValue) {
        // TODO where 조건 is / regex / date 넣어??? 일단, is만 넣기.
        if ((num % 2) != 0) {
          ctr = ctr.where(o.toString());
        } else {
          ctr = ctr.is(ParsingUtil.convertType(o));
        }
        num++;
      }
      pipeline.add(Aggregation.match(ctr));
      return this;
    }

    public Pipeline match(Criteria criteria) {
      pipeline.add(Aggregation.match(criteria));
      return this;
    }

    public Pipeline match(AggregationExpression expression) {
      pipeline.add(Aggregation.match(expression));
      return this;
    }

    /**
     * 조회할 field를 작성한다.
     *
     * @param fields : 조회 fields
     * @return
     * @link <a href="https://mindslab.atlassian.net/wiki/spaces/4BIZBACKEN/pages/9587949587/QueryUtil#project">project</a>
     */
    public Pipeline project(String... fields) {
      pipeline.add(Aggregation.project(fields));
      return this;
    }

    public Pipeline project(Map<String, AggregationExpression> expressions) {
      project(expressions, null);
      return this;
    }

    public Pipeline project(Map<String, AggregationExpression> expressions, String... fields) {
      ProjectionOperation project = null;

      boolean checkFirst = true;
      if (!expressions.isEmpty() && expressions != null) {
        for (String field : expressions.keySet()) {
          if (checkFirst) {
            project = Aggregation.project(field).and(expressions.get(field)).as(field);
            checkFirst = false;
          } else {
            project = project.and(expressions.get(field)).as(field);
          }
        }
      }

      if (fields != null) {
        for (String field : fields) {
          project = project.and(field).as(field);
        }
      }

      if (project != null) {
        pipeline.add(project);
      }

      return this;
    }

    public Pipeline projectAndReplaceRoot(String field) {
      pipeline.add(Aggregation.project().and("$$ROOT").as(field));
      pipeline.add(Aggregation.replaceRoot(field));
      return this;
    }

    public Pipeline projectAs(String field, String as) {
      pipeline.add(Aggregation.project().and(field).as(as));
      return this;
    }

    public Pipeline projectAs(AggregationExpression expression, String as) {
      pipeline.add(Aggregation.project().and(expression).as(as));
      return this;
    }

    public Pipeline projectExclude(String... fields) {
      pipeline.add(Aggregation.project().andExclude(fields));
      return this;
    }

    /**
     * 조회할 field를 작성한다.
     * 기본 id인 '_id' field를 제외하여 조회하고 싶을 때 사용한다.
     *
     * @param fields : 조회 fields
     * @return
     */
    public Pipeline projectExcluedId(String... fields) {
      pipeline.add(Aggregation.project(fields).andExclude("_id"));
      return this;
    }

    /**
     * @param field
     * @return
     * @link <a href="https://mindslab.atlassian.net/wiki/spaces/4BIZBACKEN/pages/9587949587/QueryUtil#unwind-&-replaceRoot">unwind & replaceRoot</a>
     */
    public Pipeline replaceRoot(String field) {
      pipeline.add(Aggregation.replaceRoot(field));
      return this;
    }

    public Pipeline skip(long skipCount) {
      pipeline.add(new SkipOperation(skipCount));
      return this;
    }

    public Pipeline sort(Sort sort) {
      pipeline.add(new SortOperation(sort));
      return this;
    }

    /**
     * @param field
     * @return
     * @link <a href="https://mindslab.atlassian.net/wiki/spaces/4BIZBACKEN/pages/9587949587/QueryUtil#unwind-&-replaceRoot">unwind & replaceRoot</a>
     */
    public Pipeline unwind(String field) {
      pipeline.add(Aggregation.unwind(field));
      return this;
    }

    /**
     * @param field
     * @return
     * @link <a href="https://mindslab.atlassian.net/wiki/spaces/4BIZBACKEN/pages/9587949587/QueryUtil#unwind-&-replaceRoot">unwind & replaceRoot</a>
     */
    public Pipeline unwindAndReplaceRoot(String field) {
      pipeline.add(Aggregation.unwind(field));
      pipeline.add(Aggregation.replaceRoot(field));
      return this;
    }
  } // Builder class
  /** ============ Aggregation 표현 모음 영역 ============ **/
}
