package ai.maum.data.edit.api.user.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators;

import java.time.LocalDate;
import java.util.Map;
import java.util.regex.Pattern;

import ai.maum.data.edit.api.admin.user.dto.response.AdminUserListResponse;
import ai.maum.data.edit.common.repository.BasicCustomRepositoryImpl;
import ai.maum.data.edit.common.repository.CustomAggregation;

import static ai.maum.data.edit.common.repository.CustomAggregation.Expr;
import static ai.maum.data.edit.common.repository.CustomAggregation.add;
import static ai.maum.data.edit.common.repository.CustomAggregation.and;
import static ai.maum.data.edit.common.repository.CustomAggregation.cond;
import static ai.maum.data.edit.common.repository.CustomAggregation.divide;
import static ai.maum.data.edit.common.repository.CustomAggregation.eq;
import static ai.maum.data.edit.common.repository.CustomAggregation.expr;
import static ai.maum.data.edit.common.repository.CustomAggregation.initFacet;
import static ai.maum.data.edit.common.repository.CustomAggregation.mod;
import static ai.maum.data.edit.common.repository.CustomAggregation.regex;
import static ai.maum.data.edit.common.repository.CustomAggregation.setPipline;
import static ai.maum.data.edit.common.repository.CustomAggregation.trunc;

public class UserCustomRepositoryImpl extends BasicCustomRepositoryImpl implements UserCustomRepository {

  private final String collection = "user";

  public UserCustomRepositoryImpl(MongoTemplate mongoTemplate) {
    super(mongoTemplate);
  }

  @Override
  public AdminUserListResponse findUserList(String clientId, String filter, Pattern query, LocalDate startDate,
      LocalDate endDate, int offset, int limit) {
    return CustomAggregation.setAggregation(collection)
        .match("client_id", clientId, "is_admin", false)
        .sort(Sort.by(Sort.Order.desc("created")))
        .projectExclude("_id", "client_id", "updated")
        .facet(initFacet()
            .setFacet("member_count", setPipline()
                .count("value")
                .build())
            .setFacet("users", setPipline()
                .projectAndReplaceRoot("info")
                .match(and(
                    regex(filter, query),
                    expr(Expr.cond(
                        Expr.and(Expr.ne(startDate, null), Expr.ne(endDate, null)),
                        Expr.and(Expr.gte("$created", startDate), Expr.lt("$created", endDate)),
                        Expr.cond(
                            Expr.ne(startDate, null),
                            Expr.gte("$created", startDate),
                            Expr.cond(
                                Expr.ne(endDate, null),
                                Expr.lt("$created", endDate),
                                true))
                    ))
                ))
                .build()))
        .project(Map.of("member_count", ArrayOperators.arrayOf("member_count.value").elementAt(0)), "users")
        .facet(initFacet()
            .setFacet("member_count", setPipline()
                .projectAs("member_count", "value")
                .build())
            .setFacet("total_page", setPipline()
                .projectAs(ArrayOperators.Size.lengthOfArray("users"), "value")
                .project(Map.of("value", cond()
                    .when(eq(mod("value", 10), 0))
                    .then(divide("value", 10))
                    .otherwise(add(trunc(divide("value", 10)), 1))))
                .build())
            .setFacet("result_count", setPipline()
                .projectAs(ArrayOperators.Size.lengthOfArray("users"), "value")
                .build())
            .setFacet("users", setPipline()
                .projectAs("users", "info")
                .unwindAndReplaceRoot("info")
                .skip(offset)
                .limit(limit)
                .build()))
        .project(Map.of(
            "member_count",
            ConditionalOperators.ifNull(
                ArrayOperators.arrayOf("member_count.value").elementAt(0)
            ).then(0),

            "total_page",
            ConditionalOperators.ifNull(
                ArrayOperators.arrayOf("total_page.value").elementAt(0)
            ).then(0),

            "result_count",
            ConditionalOperators.ifNull(
                ArrayOperators.arrayOf("result_count.value").elementAt(0)
            ).then(0)
        ), "users")
        .aggregation(AdminUserListResponse.class, mongoTemplate);
  }
}
