package ai.maum.data.edit.api.admin.category.dto.response;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title = "고객사별 카테고리 유형 목록 조회 요청")
public class AdminCategoryResponse {

  @Field("client_ctg_id")
  @Schema(title = "카테고리 유형 ID", example = "64a523400000000000e76889")
  private ObjectId ctgId;
  @Field("client_ctg_name")
  @Schema(title = "카테고리 유형 이름", example = "야생동물")
  private String name;
}
