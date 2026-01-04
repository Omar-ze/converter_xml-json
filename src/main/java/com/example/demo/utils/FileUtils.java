package com.example.demo.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

    public class FileUtils {

        public static void saveToFile(String content, String filePath) throws IOException {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                writer.write(content);
            }
        }

        public static String readFile(String filePath) throws IOException {
            return new String(Files.readAllBytes(Paths.get(filePath)));
        }

        public static String getFileExtension(String fileName) {
            int dotIndex = fileName.lastIndexOf('.');
            return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
        }

        public static boolean isXmlFile(String fileName) {
            String ext = getFileExtension(fileName).toLowerCase();
            return ext.equals("xml");
        }

        public static boolean isJsonFile(String fileName) {
            String ext = getFileExtension(fileName).toLowerCase();
            return ext.equals("json");
        }
    }
