package ai.maum.data.edit.api.job.repository;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ai.maum.data.edit.api.admin.job.request.AdminJobAssignableUserListRequest;
import ai.maum.data.edit.api.admin.job.response.AdminJobAssignableUserResponse;
import ai.maum.data.edit.api.admin.job.response.AdminJobResponse;
import ai.maum.data.edit.api.user.enums.UserType;
import ai.maum.data.edit.common.enums.WorkStatus;
import ai.maum.data.edit.common.response.PageResponse;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class JobCustomRepositoryImpl implements JobCustomRepository {

  private final MongoTemplate template;

  @Override
  public PageResponse<AdminJobAssignableUserResponse> getAssignableUsers(AdminJobAssignableUserListRequest request) {
    ObjectId workspaceId = new ObjectId(request.getWorkspace_id());
    UserType userType = UserType.fromCode(request.getUser_type());

    List<AggregationOperation> operations = new ArrayList<>();

    operations.add(Aggregation.match(Criteria.where("workspace_id").is(workspaceId)));
    operations.add(Aggregation.project("access_users").andExclude("_id"));
    operations.add(Aggregation.unwind("access_users"));
    operations.add(Aggregation.match(Criteria.where("access_users.type").is(userType.getCode())));

    if (StringUtils.isNotBlank(request.getSearch())) {
      operations.add(
          Aggregation.match(Criteria.where("access_users.account")
              .regex(request.getSearch()))
      );
    }

    operations.add(Aggregation.project("access_users.user_id"));

    List<ObjectId> assignableUserIds =
        template.aggregate(Aggregation.newAggregation(operations), "workspace", Document.class)
            .getMappedResults()
            .stream()
            .map(doc -> doc.getObjectId("user_id"))
            .collect(Collectors.toList());

    Query query = Query.query(Criteria.where("user_id").in(assignableUserIds));

    if (StringUtils.isNotBlank(request.getSearch())) {
      query = query.addCriteria(Criteria.where("account").regex(request.getSearch()));
    }

    query.fields().include("user_id", "account", "desc");
    query.skip(request.getOffset()).limit(request.getSize());

    List<AdminJobAssignableUserResponse> list = template.find(query, Document.class, "user")
        .stream()
        .map(doc -> new AdminJobAssignableUserResponse(
            doc.getObjectId("user_id"),
            doc.getString("account"),
            doc.getString("desc")
        ))
        .collect(Collectors.toList());

    long totalElements = template.count(
        Query.query(Criteria.where("user_id").in(assignableUserIds)),
        "user"
    );

    return PageResponse.<AdminJobAssignableUserResponse>builder()
        .request(request)
        .list(list)
        .totalElements(totalElements)
        .build();
  }

  @Override
  public List<AdminJobResponse> getJobs(ObjectId projectId) {
    Aggregation aggregation = Aggregation.newAggregation(
        Aggregation.match(Criteria.where("project_id").is(projectId)),
        Aggregation.project("jobs"),
        Aggregation.unwind("jobs"),
        Aggregation.replaceRoot("jobs"),
        Aggregation.project("job_id", "job_name", "works"),
        context -> new Document("$addFields",
            new Document("work",
                new Document("$cond", Arrays.asList(
                    new Document("$gt", Arrays.asList(
                        new Document("$size", new Document("$ifNull", Arrays.asList("$works", List.of()))),
                        0
                    )),
                    new Document("$first", "$works"),
                    null
                ))
            )
        ),
        context -> new Document("$project",
            new Document("job_id", 1)
                .append("job_name", 1)
                .append("work_status", "$work.work_status")
                .append("worker_id", "$work.worker_id")
                .append("checker_id", "$work.checker_id")
        ),
        Aggregation.sort(Sort.by(Sort.Direction.ASC, "job_name"))
    );

    List<Document> results = template.aggregate(aggregation, "project", Document.class).getMappedResults();

    List<ObjectId> userIds = results.stream()
        .flatMap(doc -> Stream.of(doc.getObjectId("worker_id"), doc.getObjectId("checker_id")))
        .filter(Objects::nonNull)
        .distinct()
        .collect(Collectors.toList());

    Query query = Query.query(Criteria.where("user_id").in(userIds));
    query.fields().include("user_id", "account", "desc");

    Map<ObjectId, Pair<String, String>> userMap = template.find(query, Document.class, "user")
        .stream()
        .collect(Collectors.toMap(
            doc -> doc.getObjectId("user_id"),
            doc -> Pair.of(doc.getString("account"), doc.getString("desc"))
        ));

    return results.stream()
        .map(doc -> {
          ObjectId jobId = doc.getObjectId("job_id");
          String jobName = doc.getString("job_name");
          WorkStatus workStatus = Optional.ofNullable(StringUtils.trimToNull(doc.getString("work_status")))
              .map(WorkStatus::fromCode)
              .orElse(WorkStatus.RD);
          ObjectId workerId = doc.getObjectId("worker_id");
          ObjectId checkerId = doc.getObjectId("checker_id");

          AdminJobResponse.UserInfo worker = Optional.ofNullable(userMap.get(workerId))
              .map(pair -> new AdminJobResponse.UserInfo(workerId, pair.getLeft(), pair.getRight()))
              .orElse(null);

          AdminJobResponse.UserInfo checker = Optional.ofNullable(userMap.get(checkerId))
              .map(pair -> new AdminJobResponse.UserInfo(checkerId, pair.getLeft(), pair.getRight()))
              .orElse(null);

          return new AdminJobResponse(jobId, workStatus, jobName, worker, checker);
        })
        .collect(Collectors.toList());
  }

  @Override
  public void updateWorker(ObjectId projectId, ObjectId jobId, WorkStatus workStatus, ObjectId userId,
      UserType userType) {
    Criteria criteria = Criteria.where("project_id").is(projectId)
        .and("jobs.job_id").is(jobId);

    Update update = new Update();
    if (userType == UserType.WORKER) {
      if (userId == null) {
        // 작업 전 상태로 변경
        update.unset("jobs.$[job].works.0.worker_id")
            .set("jobs.$[job].works.0.work_status", WorkStatus.RD.getCode());
      } else {
        // 작업 중 상태로 변경
        update.set("jobs.$[job].works.0.worker_id", userId);

        // RD 상태일 경우에만 AS 상태로 변경
        if (workStatus == WorkStatus.RD) {
          update.set("jobs.$[job].works.0.work_status", WorkStatus.AS.getCode());
        }
      }
    } else {
      if (userId == null) {
        // 제출 상태로 변경
        update.unset("jobs.$[job].works.0.checker_id")
            .set("jobs.$[job].works.0.work_status", WorkStatus.RQ.getCode());
      } else {
        // 검수 중 상태로 변경
        update.set("jobs.$[job].works.0.checker_id", userId)
            .set("jobs.$[job].works.0.work_status", WorkStatus.IS.getCode());

        // check_step 필드 추가
        Query checkStepQuery = new Query(
            new Criteria().andOperator(
                criteria,
                new Criteria().orOperator(
                    Criteria.where("jobs.works.0.check_step").exists(false),
                    Criteria.where("jobs.works.0.check_step").is(null)
                )
            )
        );

        Update checkStepUpdate = new Update()
            .set("jobs.$[job].works.0.check_step", 1)
            .filterArray(Criteria.where("job.job_id").is(jobId));

        template.updateFirst(checkStepQuery, checkStepUpdate, "project");
      }
    }
    update.filterArray(Criteria.where("job.job_id").is(jobId));

    template.updateFirst(new Query(criteria), update, "project");
  }
}
