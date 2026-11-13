package ai.maum.data.edit.api.admin.project.vo;

import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;

@Data
public class UploadResultNotSuccessValue {

  @Field("job_upload_result")
  private int jobUploadResult;
  @Field("speaker_upload_result")
  private int speakerUploadResult;
}
