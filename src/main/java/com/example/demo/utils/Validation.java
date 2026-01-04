package com.example.demo.utils;


public class Validation {
    public static void validateXml(String xml) throws Exception {
        if (xml == null || xml.trim().isEmpty()) {
            throw new Exception("XML est vide");
        }
        if (!xml.contains("<") || !xml.contains(">")) {
            throw new Exception("XML invalide");
        }
    }

    public static void validateJson(String json) throws Exception {
        if (json == null || json.trim().isEmpty()) {
            throw new Exception("JSON est vide");
        }
        json = json.trim();
        if (!json.startsWith("{") && !json.startsWith("[")) {
            throw new Exception("JSON doit commencer par { ou [");
        }
    }
}