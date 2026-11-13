package ai.maum.data.edit.api.category;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.List;

import ai.maum.data.edit.common.domain.BaseField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Document(collection = "client_category")
public class CategoryEntity extends BaseField {

  @MongoId
  private ObjectId id;
  @Field("client_ctg_id")
  @Indexed(unique = true)
  private ObjectId clientCtgId;
  @Field("client_id")
  private String clientId;
  @Field("client_ctg_name")
  private String clientCtgName;
  @Field("tool_types")
  private List<String> toolTypes;
  private List<Category> categories;

  @Data
  public static class Category {
    @Field("ctg_id")
    private ObjectId ctgId;
    @Field("ctg_name")
    private String ctgName;
    @Field("ctg_depth")
    private List<Category> ctgDepth;
  }
}
