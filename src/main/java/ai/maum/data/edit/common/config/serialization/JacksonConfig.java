package ai.maum.data.edit.common.config.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import org.bson.types.ObjectId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import ai.maum.data.edit.common.config.serialization.deserializer.LocalDateDeserializer;
import ai.maum.data.edit.common.config.serialization.deserializer.LocalDateTimeDeserializer;
import ai.maum.data.edit.common.config.serialization.deserializer.LocalTimeDeserializer;
import ai.maum.data.edit.common.config.serialization.serializer.LocalDateSerializer;
import ai.maum.data.edit.common.config.serialization.serializer.LocalDateTimeSerializer;
import ai.maum.data.edit.common.config.serialization.serializer.LocalTimeSerializer;
import ai.maum.data.edit.common.config.serialization.serializer.ObjectIdSerializer;

/**
 * 직렬화 및 역직렬화 설정
 */
@Configuration
public class JacksonConfig {
  @Bean
  public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
    return builder.build()
        .registerModule(new SimpleModule()
            .addSerializer(ObjectId.class, new ObjectIdSerializer())
            .addSerializer(LocalTime.class, new LocalTimeSerializer())
            .addSerializer(LocalDate.class, new LocalDateSerializer())
            .addSerializer(LocalDateTime.class, new LocalDateTimeSerializer())
            .addDeserializer(LocalTime.class, new LocalTimeDeserializer())
            .addDeserializer(LocalDate.class, new LocalDateDeserializer())
            .addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer()));
  }
}
