package ai.maum.data.edit.api.work.dto.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Schema(title = "라벨링 데이터 저장/수정 요청")
@NoArgsConstructor
public class SaveLabelingRequest {

  @Schema(title = "가공 단계", description = "가공 단계(1:verbal/2:paraverbal/3:nonverbal)", example = "1")
  private int step;
  private int moveStep;
  private String workspaceId;
  private String projectId;
  private String jobId;
  private String workId;

  // TODO ComplexConversationLabelingRequest 공통으로 사용할 수 있어야 함..
  private List<ComplexConversationLabelingRequest> labelings;
}
