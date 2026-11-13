package ai.maum.data.edit.api.admin.project.vo;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;

@Data
public class JobFileForProjectFileUploadValue {

  @Field("job_id")
  private ObjectId jobId;
  @Field("job_file")
  private JobFile jobFile;

  @Data
  public static class JobFile {
    @Field("file_id")
    private ObjectId fileId;
    @Field("file_name")
    private String fileName;
    @Field("file_type")
    private String fileType;
    @Field("file_status")
    private String fileStatus;
    @Field("file_path")
    private String filePath;
    @Field("file_info")
    private ai.maum.data.edit.api.workspace.field.job.JobFile.FileInfo fileInfo;
  }
}
