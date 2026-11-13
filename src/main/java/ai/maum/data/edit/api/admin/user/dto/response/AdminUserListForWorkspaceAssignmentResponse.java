package ai.maum.data.edit.api.admin.user.dto.response;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(title = "관리자 워크스페이스 작업자 지정 사용자 목록 조회 응답")
public class AdminUserListForWorkspaceAssignmentResponse {

  @Field("total_page")
  @Schema(title = "전체 페이지 수", example = "1")
  private int totalPage;
  @Field("result_count")
  @Schema(title = "검색 결과 수", example = "3")
  private int resultCount;
  @Schema(title = "사용자 정보 목록")
  private List<User> users;

  @Data
  @Builder
  public static class User {
    @Field("user_id")
    @Schema(title = "사용자 ID", example = "507f1f77bcf86cd799439011")
    private ObjectId userId;
    @Schema(title = "사용자 계정", example = "worker001")
    private String account;
    @Schema(title = "사용자 설명", example = "worker001 입니다.")
    private String desc;
  }
}
