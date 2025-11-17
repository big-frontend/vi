package com.tencent.matrix.apk.model.task.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tencent.matrix.apk.model.job.JobConfig;

import java.util.Map;
import java.util.Set;

public class JSONUtil {
    public static void addR(JobConfig jobConfig, JsonObject jsonObject, String name) {
        String resName = "res/" + name.substring(name.indexOf(".") + 1).replace(".", "/") + ".xml";
        String module = getModule(jobConfig,resName);
        add(jsonObject, module, name);
    }
    public static void addAssets(JobConfig jobConfig, JsonObject jsonObject, String name) {
        String assetsName = "assets/"+name;
        String module = getModule(jobConfig, assetsName);
        add(jsonObject, module, name);
    }
    public static void add(JsonObject jsonObject, String key, String path) {
        JsonArray jsonArray = jsonObject.getAsJsonArray(key);
        if (jsonArray == null) {
            jsonArray = new JsonArray();
            jsonObject.add(key, jsonArray);
        }
        jsonArray.add(path);
    }
    public static void add(JsonObject jsonObject, String key, JsonObject name) {
        JsonArray jsonArray = jsonObject.getAsJsonArray(key);
        if (jsonArray == null) {
            jsonArray = new JsonArray();
            jsonObject.add(key, jsonArray);
        }
        jsonArray.add(name);
    }
    public static String getModule(JobConfig jobConfig, String path) {
        Map<String, Set<String>> resolvedArtifactsMap = jobConfig.getResolvedArtifactsMap();
        if (resolvedArtifactsMap == null || resolvedArtifactsMap.isEmpty()) {
            return "others";
        }
        for (Map.Entry<String, Set<String>> entry : resolvedArtifactsMap.entrySet()) {
            if (entry.getValue().contains(path)) {
                return entry.getKey();
            }
        }
        return "others";
    }
}
