package ai.maum.data.edit.common.config.serialization.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import org.bson.types.ObjectId;

import java.io.IOException;

/**
 * ObjectId를 문자열로 직렬화하는 Serializer
 */
public class ObjectIdSerializer extends JsonSerializer<ObjectId> {
  @Override
  public void serialize(ObjectId value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
    gen.writeString(value.toString());
  }
}
