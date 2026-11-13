package ai.maum.data.edit.api.work.dto.response;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class WorkStatusReasonResponse {

  private String reason;
  private LocalDateTime created;
}
