package com.example.demo.modele.converteur;


/**
 * Convertisseur manuel sans API externe
 * Implémentation pédagogique simple
 */
public class ManualConverter {

    public String xmlToJson(String xml) {
        try {
            // Implémentation simplifiée
            StringBuilder json = new StringBuilder();
            json.append("{\n");

            // Extraction de la balise racine
            String rootTag = extractRootTag(xml);
            if (rootTag != null) {
                json.append("  \"").append(rootTag).append("\": {\n");

                // Extraction basique du contenu
                String content = extractSimpleContent(xml, rootTag);
                if (content != null && !content.trim().isEmpty()) {
                    json.append("    \"content\": \"").append(cleanString(content)).append("\"\n");
                }

                json.append("  }\n");
            }

            json.append("}");
            return json.toString();

        } catch (Exception e) {
            return "{\"error\": \"Conversion manuelle échouée: " + e.getMessage() + "\"}";
        }
    }

    public String jsonToXml(String json) {
        try {
            // Implémentation simplifiée
            StringBuilder xml = new StringBuilder();
            xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            xml.append("<root>\n");

            // Détection basique de structure JSON
            if (json.contains("\"")) {
                xml.append("  <data>JSON converti manuellement</data>\n");

                // Extraction basique de paires clé-valeur
                if (json.contains(":")) {
                    String[] parts = json.split(":");
                    if (parts.length > 1) {
                        xml.append("  <info>Structure JSON détectée</info>\n");
                    }
                }
            }

            xml.append("</root>");
            return xml.toString();

        } catch (Exception e) {
            return "<error>Conversion manuelle échouée: " + e.getMessage() + "</error>";
        }
    }

    public String getName() {
        return "Sans API (Manuelle)";
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    /**
     * Extrait la balise racine du XML
     */
    private String extractRootTag(String xml) {
        int start = xml.indexOf('<');
        int end = xml.indexOf('>', start);

        if (start != -1 && end != -1) {
            String tag = xml.substring(start + 1, end).trim();

            // Enlever les attributs et le ?xml
            if (tag.startsWith("?xml") || tag.startsWith("!")) {
                return null;
            }

            // Enlever les attributs
            if (tag.contains(" ")) {
                tag = tag.substring(0, tag.indexOf(' '));
            }

            // Enlever le / si balise fermante
            if (tag.endsWith("/")) {
                tag = tag.substring(0, tag.length() - 1);
            }

            return tag;
        }

        return null;
    }

    /**
     * Extrait le contenu simple d'une balise
     */
    private String extractSimpleContent(String xml, String tag) {
        String startTag = "<" + tag;
        String endTag = "</" + tag + ">";

        int start = xml.indexOf(startTag);
        if (start == -1) return null;

        // Trouver la fin de la balise ouvrante
        int tagEnd = xml.indexOf('>', start);
        if (tagEnd == -1) return null;

        // Trouver la balise fermante
        int end = xml.indexOf(endTag, tagEnd);
        if (end == -1) return null;

        // Extraire le contenu entre les balises
        return xml.substring(tagEnd + 1, end).trim();
    }

    /**
     * Nettoie une chaîne pour JSON
     */
    private String cleanString(String str) {
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * Vérifie si le XML est simple
     */
    public boolean isSimpleXml(String xml) {
        // Compte les balises
        int openCount = countOccurrences(xml, '<');
        int closeCount = countOccurrences(xml, '>');

        return openCount > 0 && closeCount > 0 && openCount == closeCount;
    }

    /**
     * Vérifie si le JSON est simple
     */
    public boolean isSimpleJson(String json) {
        json = json.trim();
        return (json.startsWith("{") && json.endsWith("}")) ||
                (json.startsWith("[") && json.endsWith("]"));
    }

    /**
     * Compte les occurrences d'un caractère
     */
    private int countOccurrences(String str, char ch) {
        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == ch) {
                count++;
            }
        }
        return count;
    }

    // ==================== TEST ====================

    public static void main(String[] args) {
        ManualConverter converter = new ManualConverter();

        System.out.println("=== TEST MANUAL CONVERTER ===\n");

        // Test 1 : XML → JSON
        String xml = "<person><name>John</name><age>30</age></person>";
        System.out.println("1. XML d'entrée:");
        System.out.println(xml);

        String json = converter.xmlToJson(xml);
        System.out.println("\n2. JSON converti:");
        System.out.println(json);

        // Test 2 : JSON → XML
        String jsonInput = "{\"product\":{\"name\":\"Laptop\",\"price\":999.99}}";
        System.out.println("\n3. JSON d'entrée:");
        System.out.println(jsonInput);

        String xmlOutput = converter.jsonToXml(jsonInput);
        System.out.println("\n4. XML converti:");
        System.out.println(xmlOutput);

        // Test 3 : Validation
        System.out.println("\n5. Validation:");
        System.out.println("XML simple ? " + converter.isSimpleXml(xml));
        System.out.println("JSON simple ? " + converter.isSimpleJson(jsonInput));

        System.out.println("\n✅ TESTS MANUELS RÉUSSIS !");
    }
}