package ai.maum.data.edit.common.config.serialization.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import ai.maum.data.edit.common.enums.DateMeta;

/**
 * LocalTime을 시간 형식의 문자열로 직렬화하는 Serializer
 */
public class LocalTimeSerializer extends JsonSerializer<LocalTime> {
  private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DateMeta.TIME_FORMAT);

  @Override
  public void serialize(LocalTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
    gen.writeString(value.format(formatter));
  }
}
