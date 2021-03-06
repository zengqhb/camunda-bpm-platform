/*
 * Copyright © 2013-2018 camunda services GmbH and various authors (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.impl.util;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.impl.json.JsonObjectConverter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.internal.LazilyParsedNumber;
import com.google.gson.reflect.TypeToken;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;

/**
 * @author Tassilo Weidner
 */
public final class JsonUtil {

  private static final EngineUtilLogger LOG = ProcessEngineLogger.UTIL_LOGGER;

  protected static final Type MAP_TYPE_TOKEN = new TypeToken<Map<String, Object>>() {}.getType();

  protected static Gson gsonMapper = createGsonMapper();

  public static void addFieldRawValue(JsonObject jsonObject, String memberName, Object rawValue) {
    if (rawValue != null) {
      JsonElement jsonNode = null;

      try {
        jsonNode = getGsonMapper().toJsonTree(rawValue);

      } catch (JsonIOException e) {
        LOG.logJsonException(e);
      }

      if (jsonNode != null) {
        jsonObject.add(memberName, jsonNode);
      }
    }
  }

  public static <T> void addField(JsonObject jsonObject, String name, JsonObjectConverter<T> converter, T value) {
    if (jsonObject != null && name != null && converter != null && value != null) {
      jsonObject.add(name, converter.toJsonObject(value));
    }
  }

  public static void addListField(JsonObject jsonObject, String name, List<String> list) {
    if (jsonObject != null && name != null && list != null) {
      jsonObject.add(name, asArray(list));
    }
  }

  public static void addArrayField(JsonObject jsonObject, String name, String[] array) {
    if (jsonObject != null && name != null && array != null) {
      addListField(jsonObject, name, Arrays.asList(array));
    }
  }

  public static void addDateField(JsonObject jsonObject, String name, Date date) {
    if (jsonObject != null && name != null && date != null) {
      jsonObject.addProperty(name, date.getTime());
    }
  }

  public static <T> void addElement(JsonArray jsonObject, JsonObjectConverter<T> converter, T value) {
    if (jsonObject != null && converter != null && value != null) {
      JsonObject jsonElement = converter.toJsonObject(value);

      if (jsonElement != null) {
        jsonObject.add(jsonElement);
      }
    }
  }

  public static <T> void addListField(JsonObject jsonObject, String name, JsonObjectConverter<T> converter, List<T> list) {
    if (jsonObject != null && name != null && converter != null && list != null) {
      JsonArray arrayNode = createArray();

      for (T item : list) {
        if (item != null) {
          JsonObject jsonElement = converter.toJsonObject(item);
          if (jsonElement != null) {
            arrayNode.add(jsonElement);
          }
        }
      }

      jsonObject.add(name, arrayNode);
    }
  }

  public static <T> T asJavaObject(JsonObject jsonObject, JsonObjectConverter<T> converter) {
    if (jsonObject != null && converter != null) {
      return converter.toObject(jsonObject);

    } else {
      return null;

    }
  }

  public static void addNullField(JsonObject jsonObject, String name) {
    if (jsonObject != null && name != null) {
      jsonObject.add(name, JsonNull.INSTANCE);
    }
  }

  public static void addField(JsonObject jsonObject, String name, JsonArray value) {
    if (jsonObject != null && name != null && value != null) {
      jsonObject.add(name, value);
    }
  }

  public static void addField(JsonObject jsonObject, String name, String value) {
    if (jsonObject != null && name != null && value != null) {
      jsonObject.addProperty(name, value);
    }
  }

  public static void addField(JsonObject jsonObject, String name, Boolean value) {
    if (jsonObject != null && name != null && value != null) {
      jsonObject.addProperty(name, value);
    }
  }

  public static void addField(JsonObject jsonObject, String name, Integer value) {
    if (jsonObject != null && name != null && value != null) {
      jsonObject.addProperty(name, value);
    }
  }

  public static void addField(JsonObject jsonObject, String name, Short value) {
    if (jsonObject != null && name != null && value != null) {
      jsonObject.addProperty(name, value);
    }
  }

  public static void addField(JsonObject jsonObject, String name, Long value) {
    if (jsonObject != null && name != null && value != null) {
      jsonObject.addProperty(name, value);
    }
  }

  public static void addField(JsonObject jsonObject, String name, Double value) {
    if (jsonObject != null && name != null && value != null) {
      jsonObject.addProperty(name, value);
    }
  }

  public static void addDefaultField(JsonObject jsonObject, String name, boolean defaultValue, Boolean value) {
    if (jsonObject != null && name != null && value != null && !value.equals(defaultValue)) {
      addField(jsonObject, name, value);
    }
  }

  public static byte[] asBytes(JsonElement jsonObject) {
    String jsonString = null;

    if (jsonObject != null) {
      try {
        jsonString = getGsonMapper().toJson(jsonObject);

      } catch (JsonIOException e) {
        LOG.logJsonException(e);
      }
    }

    if (jsonString == null) {
      jsonString = "";
    }

    return StringUtil.toByteArray(jsonString);
  }

  public static JsonObject asObject(byte[] byteArray) {
    String stringValue = null;

    if (byteArray != null) {
      stringValue = StringUtil.fromBytes(byteArray);
    }

    if (stringValue == null) {
      return createObject();

    }

    JsonObject jsonObject = null;
    try {
      jsonObject = getGsonMapper().fromJson(stringValue, JsonObject.class);

    } catch (JsonParseException e) {
      LOG.logJsonException(e);
    }

    if (jsonObject != null) {
      return jsonObject;

    } else {
      return createObject();

    }
  }

  public static JsonObject asObject(String jsonString) {
    JsonObject jsonObject = null;

    if (jsonString != null) {
      try {
        jsonObject = getGsonMapper().fromJson(jsonString, JsonObject.class);

      } catch (JsonParseException | ClassCastException e) {
        LOG.logJsonException(e);
      }
    }

    if (jsonObject != null) {
      return jsonObject;

    } else {
      return createObject();

    }
  }

  public static JsonObject asObject(Map<String, Object> properties) {
    if (properties != null) {
      JsonObject jsonObject = null;

      try {
        jsonObject = (JsonObject) getGsonMapper().toJsonTree(properties);

      } catch (JsonIOException | ClassCastException e) {
        LOG.logJsonException(e);
      }

      if (jsonObject != null) {
        return jsonObject;

      } else {
        return createObject();

      }
    } else {
      return createObject();

    }
  }

  public static List<String> asList(JsonElement jsonObject) {
    JsonArray jsonArray = null;

    if (jsonObject != null) {
      try {
        jsonArray = jsonObject.getAsJsonArray();

      } catch (IllegalStateException | ClassCastException e) {
        LOG.logJsonException(e);
      }
    }

    if (jsonArray == null) {
      return Collections.emptyList();
    }

    List<String> list = new ArrayList<>();
    for (JsonElement entry : jsonArray) {
      String stringValue = null;

      try {
        stringValue = entry.getAsString();

      } catch (IllegalStateException | ClassCastException e) {
        LOG.logJsonException(e);
      }

      if (stringValue != null) {
        list.add(stringValue);
      }
    }

    return list;
  }

  public static <T> List<T> asList(JsonArray jsonArray, JsonObjectConverter<T> converter) {
    if (jsonArray == null || converter == null) {
      return Collections.emptyList();
    }

    List<T> list = new ArrayList<T>();

    for (JsonElement element : jsonArray) {
      JsonObject jsonObject = null;

      try {
        jsonObject = element.getAsJsonObject();

      } catch (IllegalStateException | ClassCastException e) {
        LOG.logJsonException(e);
      }

      if (jsonObject != null) {

        T rawObject = converter.toObject(jsonObject);
        if (rawObject != null) {
          list.add(rawObject);
        }
      }
    }

    return list;
  }

  public static Map<String, Object> asMap(JsonObject jsonObject) {
    if (jsonObject != null) {
      Map<String, Object> map = null;

      try {
        map = getGsonMapper().fromJson(jsonObject, MAP_TYPE_TOKEN);

      } catch (JsonParseException | ClassCastException e) {
        LOG.logJsonException(e);
      }

      if (map != null) {

        for (Map.Entry<String, Object> entry : map.entrySet()) {
          Object value = entry.getValue();

          if (value != null && value instanceof JsonPrimitive) {

            Object rawObject = asPrimitiveObject((JsonPrimitive) value);
            if (rawObject != null) {
              map.put(entry.getKey(), rawObject);
            }
          }
        }

        return map;

      } else {
        return Collections.emptyMap();

      }
    } else {
      return Collections.emptyMap();
    }
  }

  public static String asString(Map<String, Object> properties) {
    if (properties != null) {

      JsonObject jsonObject = asObject(properties);
      if (jsonObject != null) {

        String stringValue = jsonObject.toString();
        if (stringValue != null) {
          return stringValue;

        } else {
          return "";

        }
      } else {
        return "";

      }

    } else {
      return "";

    }
  }

  public static JsonArray asArray(List<String> list) {
    if (list != null) {
      JsonElement jsonElement = null;

      try {
        jsonElement = getGsonMapper().toJsonTree(list);

      } catch (JsonIOException e) {
        LOG.logJsonException(e);
      }

      if (jsonElement != null) {
        return getArray(jsonElement);

      } else {
        return createArray();

      }
    } else {
      return createArray();

    }
  }

  public static Object getRawObject(JsonObject jsonObject, String memberName) {
    if (jsonObject == null || memberName == null) {
      return null;
    }

    Object rawValue = null;

    if (jsonObject.has(memberName)) {
      JsonPrimitive jsonPrimitive = null;

      try {
        jsonPrimitive = jsonObject.getAsJsonPrimitive(memberName);

      } catch (ClassCastException e) {
        LOG.logJsonException(e);
      }

      if (jsonPrimitive != null) {
        rawValue = asPrimitiveObject(jsonPrimitive);

      }
    }

    if (rawValue != null) {
      return rawValue;

    } else {
      return null;

    }
  }

  public static Object asPrimitiveObject(JsonPrimitive jsonValue) {
    if (jsonValue == null) {
      return null;
    }

    Object rawObject = null;

    if (jsonValue.isNumber()) {
      LazilyParsedNumber numberValue = null;

      try {
        numberValue = (LazilyParsedNumber) jsonValue.getAsNumber();

      } catch (ClassCastException | NumberFormatException e) {
        LOG.logJsonException(e);
      }

      if (numberValue != null) {

        String numberString = numberValue.toString();
        if (numberString != null) {
          rawObject = parseNumber(numberString);
        }

      }
    } else { // string, boolean
      try {
        rawObject = getGsonMapper().fromJson(jsonValue, Object.class);

      } catch (JsonSyntaxException | JsonIOException e) {
        LOG.logJsonException(e);
      }

    }

    if (rawObject != null) {
      return rawObject;

    } else {
      return null;

    }
  }

  protected static Number parseNumber(String numberString) {
    if (numberString == null) {
      return null;
    }

    try {
      return Integer.parseInt(numberString);

    } catch (NumberFormatException ignored) { }

    try {
      return Long.parseLong(numberString);

    } catch (NumberFormatException ignored) { }

    try {
      return Double.parseDouble(numberString);

    } catch (NumberFormatException ignored) { }

    return null;
  }

  public static boolean getBoolean(JsonObject json, String memberName) {
    if (json != null && memberName != null && json.has(memberName)) {
      try {
        return json.get(memberName).getAsBoolean();

      } catch (ClassCastException | IllegalStateException e) {
        LOG.logJsonException(e);

        return false;

      }
    } else {
      return false;

    }
  }

  public static String getString(JsonObject json, String memberName) {
    if (json != null && memberName != null && json.has(memberName)) {
      return getString(json.get(memberName));

    } else {
      return "";

    }
  }

  public static String getString(JsonElement jsonElement) {
    if (jsonElement == null) {
      return "";
    }

    try {
      return jsonElement.getAsString();

    } catch (ClassCastException | IllegalStateException e) {
      LOG.logJsonException(e);

      return "";

    }
  }

  public static int getInt(JsonObject json, String memberName) {
    if (json != null && memberName != null && json.has(memberName)) {
      try {
        return json.get(memberName).getAsInt();

      } catch (ClassCastException | IllegalStateException e) {
        LOG.logJsonException(e);

        return 0;

      }
    } else {
      return 0;

    }
  }

  public static boolean isNull(JsonObject jsonObject, String memberName) {
    if (jsonObject != null && memberName != null && jsonObject.has(memberName)) {
      return jsonObject.get(memberName).isJsonNull();

    } else {
      return false;

    }
  }

  public static long getLong(JsonObject json, String memberName) {
    if (json != null && memberName != null && json.has(memberName)) {
      try {
        return json.get(memberName).getAsLong();

      } catch (ClassCastException | IllegalStateException e) {
        LOG.logJsonException(e);

        return 0L;

      }
    } else {
      return 0L;

    }
  }

  public static JsonArray getArray(JsonObject json, String memberName) {
    if (json != null && memberName != null && json.has(memberName)) {
      return getArray(json.get(memberName));

    } else {
      return createArray();

    }
  }

  public static JsonArray getArray(JsonElement json) {
    if (json != null && json.isJsonArray()) {
      return json.getAsJsonArray();

    } else {
      return createArray();

    }
  }

  public static JsonObject getObject(JsonObject json, String memberName) {
    if (json != null && memberName != null && json.has(memberName)) {
      return getObject(json.get(memberName));

    } else {
      return createObject();

    }
  }

  public static JsonObject getObject(JsonElement json) {
    if (json != null && json.isJsonObject()) {
      return json.getAsJsonObject();

    } else {
      return createObject();

    }
  }

  public static JsonObject createObject() {
    return new JsonObject();

  }

  public static JsonArray createArray() {
    return new JsonArray();

  }

  public static Gson getGsonMapper() {
    return gsonMapper;
  }

  public static Gson createGsonMapper() {
    return new GsonBuilder()
      .serializeNulls()
      .registerTypeAdapter(Map.class, new JsonDeserializer<Map<String,Object>>() {
        public Map<String, Object> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {

          Map<String, Object> map = new HashMap<>();

          for (Map.Entry<String, JsonElement> entry : getObject(json).entrySet()) {
            if (entry != null) {
              String key = entry.getKey();
              JsonElement jsonElement = entry.getValue();

              if (jsonElement != null && jsonElement.isJsonNull()) {
                map.put(key, null);

              } else if (jsonElement != null && jsonElement.isJsonPrimitive()) {

                Object rawValue = asPrimitiveObject((JsonPrimitive) jsonElement);
                if (rawValue != null) {
                  map.put(key, rawValue);

                }
              }
            }
          }

          return map;
        }
      })
      .create();
  }

}
