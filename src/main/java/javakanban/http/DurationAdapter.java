package javakanban.http;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.Duration;

public class DurationAdapter implements JsonSerializer<Duration>, JsonDeserializer<Duration> {
    @Override
    public Duration deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
        return json.isJsonNull() ? Duration.ZERO : Duration.ofMinutes(json.getAsLong());
    }

    @Override
    public JsonElement serialize(Duration src, Type type, JsonSerializationContext context) {
        return src == null ? JsonNull.INSTANCE : new JsonPrimitive(src.toMinutes());
    }
}