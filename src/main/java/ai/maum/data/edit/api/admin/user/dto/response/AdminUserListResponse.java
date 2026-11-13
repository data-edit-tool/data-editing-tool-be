package ai.maum.data.edit.api.admin.user.dto.response;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(title = "관리자 사용자 목록 조회 응답")
public class AdminUserListResponse {

  @Field("member_count")
  @Schema(title = "작업자 인원 수", example = "3")
  private int memberCount;
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
    @Field("pw_change_yn")
    @Schema(title = "비밀번호 변경 유무", example = "true")
    private Boolean isPwChanged;
    @Schema(title = "사용자 설명", example = "1번 작업자입니다.")
    private String desc;
    @Schema(title = "생성 일자", example = "2023-05-31 12:00:00")
    private LocalDate created;
  }
}
