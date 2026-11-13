package ai.maum.data.edit.api.category.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(title = "카테고리 유형 조회 응답")
public class CategoryResponse {

  @Field("ctg_id")
  private ObjectId ctgId;
  @Field("ctg_name")
  private String ctgName;
  @Field("ctg_depth")
  private List<CategoryResponse> ctgDepth;
  @Field("parent_ctg_id")
  private ObjectId parentCtgId;
}
