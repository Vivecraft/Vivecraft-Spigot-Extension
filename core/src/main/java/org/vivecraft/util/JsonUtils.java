package org.vivecraft.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class JsonUtils {
    public static Map<String, String> readJsonMap(InputStream input) {
        Map<String, String> data = new HashMap<>();
        JsonElement json = new JsonParser().parse(new InputStreamReader(input));
        json.getAsJsonObject().entrySet().forEach(entry -> {
            data.put(entry.getKey(), entry.getValue().getAsString());
        });
        return data;
    }
}
