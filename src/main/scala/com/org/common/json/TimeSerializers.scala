package com.org.common.json

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalDateTime}

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer

object LocalDateTimeSerializer extends StdSerializer[LocalDateTime](classOf[LocalDateTime]) {
  override def serialize(localDateValue: LocalDateTime, jgen: JsonGenerator, provider: SerializerProvider) {
    jgen.writeString(localDateValue.toString)
  }
}

object LocalDateSerializer extends StdSerializer[LocalDate](classOf[LocalDate]) {
  override def serialize(localDateValue: LocalDate, jgen: JsonGenerator, provider: SerializerProvider) {
    jgen.writeString(localDateValue.format(DateTimeFormatter.ISO_DATE))
  }
}
