package ai.maum.data.edit.api.workspace.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.maum.data.edit.api.user.enums.UserType;
import ai.maum.data.edit.api.workspace.dto.response.WorkspaceListResponse;
import ai.maum.data.edit.common.repository.BasicCustomRepositoryImpl;

public class WorkspaceCustomRepositoryImpl extends BasicCustomRepositoryImpl implements WorkspaceCustomRepository {
  public WorkspaceCustomRepositoryImpl(MongoTemplate mongoTemplate) {
    super(mongoTemplate);
  }

  @Override
  public boolean containsAccessUser(ObjectId workspaceId, ObjectId userId, UserType userType) {
    Criteria criteria = Criteria.where("workspace_id").is(workspaceId)
        .and("access_users").elemMatch(
            Criteria.where("user_id").is(userId)
                .and("type").is(userType.getCode())
        );

    return mongoTemplate.exists(new Query(criteria), "workspace");
  }

  @Override
  public List<WorkspaceListResponse> findWorkspaceEntityBy(String clientId, ObjectId userId, String workspaceName,
      String workspaceIntro
      , String incharge, String startDate, String endDate) {
    Query query = new Query();
    Map<String, Object> is = new HashMap<>();

    is.put("client_id", clientId);
    is.put("is_deleted", false);
    is.put("access_users.user_id", userId);
    super.findBy(query, is);

    Map<String, Object> regex = new HashMap<>();
    regex.put("workspace_name", workspaceName);
    regex.put("workspace_intro", workspaceIntro);
    regex.put("incharge", incharge);
    super.addCriteriaRegex(query, regex);

    Map<String, Object> date = new HashMap<>();
    date.put("start_date", startDate);
    date.put("end_date", endDate);
    super.addCriteriaIsDate(query, date);

    return mongoTemplate.find(query, WorkspaceListResponse.class);
  }
}
