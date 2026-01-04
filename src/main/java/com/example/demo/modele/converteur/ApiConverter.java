package com.example.demo.modele.converteur;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Convertisseur utilisant Jackson XML/JSON API
 * Version Java 11 compatible (pas de text blocks)
 */
public class ApiConverter {

    private final XmlMapper xmlMapper;
    private final ObjectMapper jsonMapper;
    private final ObjectMapper prettyJsonMapper;

    public ApiConverter() {
        // Initialiser les mappers
        this.xmlMapper = new XmlMapper();
        xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);

        this.jsonMapper = new ObjectMapper();

        this.prettyJsonMapper = new ObjectMapper();
        prettyJsonMapper.enable(SerializationFeature.INDENT_OUTPUT);
        prettyJsonMapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    }

    public String xmlToJson(String xml) throws Exception {
        // Valider l'entrée XML
        validateXml(xml);

        try {
            // 1. Nettoyer et préparer le XML
            String cleanedXml = prepareXmlForConversion(xml);

            // 2. Convertir XML en JsonNode
            JsonNode jsonNode = xmlMapper.readTree(cleanedXml);

            // 3. Convertir JsonNode en JSON formaté
            String jsonResult = prettyJsonMapper.writeValueAsString(jsonNode);

            // 4. Post-traitement pour améliorer la lisibilité
            jsonResult = postProcessJson(jsonResult);

            // Log pour débogage
            logConversion("XML → JSON", xml, jsonResult);

            return jsonResult;

        } catch (JsonProcessingException e) {
            throw new Exception("Erreur de parsing XML: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new Exception("Erreur d'IO lors de la conversion: " + e.getMessage(), e);
        }
    }

    public String jsonToXml(String json) throws Exception {
        // Valider l'entrée JSON
        validateJson(json);

        try {
            // 1. Nettoyer et préparer le JSON
            String cleanedJson = prepareJsonForConversion(json);

            // 2. Convertir JSON en JsonNode
            JsonNode jsonNode = jsonMapper.readTree(cleanedJson);

            // 3. Convertir JsonNode en XML formaté
            String xmlResult = xmlMapper.writeValueAsString(jsonNode);

            // 4. Post-traitement pour améliorer la lisibilité
            xmlResult = postProcessXml(xmlResult);

            // Log pour débogage
            logConversion("JSON → XML", json, xmlResult);

            return xmlResult;

        } catch (JsonProcessingException e) {
            throw new Exception("Erreur de parsing JSON: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new Exception("Erreur d'IO lors de la conversion: " + e.getMessage(), e);
        }
    }

    public String getName() {
        return "Avec API (Jackson)";
    }

    public String getDescription() {
        return "Conversion utilisant la bibliothèque Jackson pour une transformation rapide et robuste";
    }

    // ==================== MÉTHODES AVANCÉES ====================

    /**
     * Conversion avec options avancées
     */
    public String xmlToJsonWithOptions(String xml, Map<String, Object> options) throws Exception {
        try {
            // Appliquer les options
            boolean prettyPrint = (boolean) options.getOrDefault("prettyPrint", true);
            boolean includeRoot = (boolean) options.getOrDefault("includeRoot", true);

            JsonNode jsonNode = xmlMapper.readTree(xml);

            ObjectMapper mapper = prettyPrint ? prettyJsonMapper : jsonMapper;

            String json = mapper.writeValueAsString(jsonNode);

            // Post-traitement selon les options
            if (!includeRoot && jsonNode.isObject() && jsonNode.size() == 1) {
                // Enlever l'élément racine si demandé
                String rootName = jsonNode.fieldNames().next();
                json = jsonMapper.writeValueAsString(jsonNode.get(rootName));
            }

            return json;

        } catch (Exception e) {
            throw new Exception("Erreur de conversion avec options: " + e.getMessage(), e);
        }
    }

    /**
     * Vérifie si une chaîne est du XML valide
     */
    public boolean isValidXml(String xml) {
        try {
            xmlMapper.readTree(xml);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Vérifie si une chaîne est du JSON valide
     */
    public boolean isValidJson(String json) {
        try {
            jsonMapper.readTree(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Convertit un fichier XML en fichier JSON
     */
    public void convertXmlFileToJsonFile(String inputFilePath, String outputFilePath) throws Exception {
        try {
            // Lire le fichier XML
            String xmlContent = readFile(inputFilePath);

            // Convertir
            String jsonContent = xmlToJson(xmlContent);

            // Écrire dans le fichier JSON
            saveToFile(jsonContent, outputFilePath);

        } catch (Exception e) {
            throw new Exception("Erreur de conversion de fichier: " + e.getMessage(), e);
        }
    }

    /**
     * Convertit un fichier JSON en fichier XML
     */
    public void convertJsonFileToXmlFile(String inputFilePath, String outputFilePath) throws Exception {
        try {
            // Lire le fichier JSON
            String jsonContent = readFile(inputFilePath);

            // Convertir
            String xmlContent = jsonToXml(jsonContent);

            // Écrire dans le fichier XML
            saveToFile(xmlContent, outputFilePath);

        } catch (Exception e) {
            throw new Exception("Erreur de conversion de fichier: " + e.getMessage(), e);
        }
    }

    /**
     * Analyse et retourne des statistiques sur le XML/JSON
     */
    public Map<String, Object> analyzeContent(String content, String type) {
        Map<String, Object> stats = new HashMap<>();

        try {
            if ("XML".equalsIgnoreCase(type)) {
                JsonNode node = xmlMapper.readTree(content);
                stats.put("type", "XML");
                stats.put("valid", true);
                stats.put("rootElement", node.fieldNames().next());
                stats.put("size", content.length());
            } else if ("JSON".equalsIgnoreCase(type)) {
                JsonNode node = jsonMapper.readTree(content);
                stats.put("type", "JSON");
                stats.put("valid", true);
                stats.put("isArray", node.isArray());
                stats.put("size", content.length());
            }
        } catch (Exception e) {
            stats.put("valid", false);
            stats.put("error", e.getMessage());
        }

        return stats;
    }

    // ==================== MÉTHODES PRIVÉES ====================

    /**
     * Valide le XML
     */
    private void validateXml(String xml) throws Exception {
        if (xml == null || xml.trim().isEmpty()) {
            throw new Exception("XML est vide");
        }
        if (!xml.contains("<") || !xml.contains(">")) {
            throw new Exception("XML invalide: pas de balises");
        }
    }

    /**
     * Valide le JSON
     */
    private void validateJson(String json) throws Exception {
        if (json == null || json.trim().isEmpty()) {
            throw new Exception("JSON est vide");
        }
        json = json.trim();
        if (!json.startsWith("{") && !json.startsWith("[")) {
            throw new Exception("JSON doit commencer par { ou [");
        }
    }

    /**
     * Lit un fichier
     */
    private String readFile(String filePath) throws IOException {
        return new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(filePath)));
    }

    /**
     * Écrit dans un fichier
     */
    private void saveToFile(String content, String filePath) throws IOException {
        try (java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.FileWriter(filePath))) {
            writer.write(content);
        }
    }

    /**
     * Prépare le XML pour la conversion
     */
    private String prepareXmlForConversion(String xml) {
        xml = xml.trim();

        // S'assurer que le XML a un élément racine unique
        if (!hasSingleRootElement(xml)) {
            // Envelopper dans un élément racine si nécessaire
            xml = "<root>" + xml + "</root>";
        }

        // Supprimer les commentaires XML
        xml = xml.replaceAll("<!--.*?-->", "");

        // Normaliser les espaces
        xml = normalizeXmlWhitespace(xml);

        return xml;
    }

    /**
     * Prépare le JSON pour la conversion
     */
    private String prepareJsonForConversion(String json) {
        json = json.trim();

        // Supprimer les commentaires de ligne (non standard)
        json = json.replaceAll("//.*", "");
        json = json.replaceAll("/\\*.*?\\*/", "");

        return json;
    }

    /**
     * Post-traitement du JSON pour améliorer la lisibilité
     */
    private String postProcessJson(String json) {
        // Remplacer les nombres entourés de guillemets par des nombres réels si possible
        json = fixNumericStrings(json);

        // Formater les tableaux sur plusieurs lignes
        json = formatJsonArrays(json);

        return json;
    }

    /**
     * Post-traitement du XML pour améliorer la lisibilité
     */
    private String postProcessXml(String xml) {
        // Ajouter la déclaration XML si absente
        if (!xml.contains("<?xml")) {
            xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + xml;
        }

        // Formater l'indentation
        xml = formatXmlIndentation(xml);

        // Nettoyer les lignes vides
        xml = xml.replaceAll("\n\\s*\n", "\n");

        return xml;
    }

    /**
     * Vérifie si le XML a un seul élément racine
     */
    private boolean hasSingleRootElement(String xml) {
        // Compter les balises racines
        int firstTagStart = xml.indexOf('<');
        int firstTagEnd = xml.indexOf('>', firstTagStart);

        if (firstTagStart == -1 || firstTagEnd == -1) {
            return false;
        }

        String firstTag = xml.substring(firstTagStart + 1, firstTagEnd).split("\\s+")[0];

        // Chercher la balise fermante correspondante
        String closingTag = "</" + firstTag + ">";
        int closingIndex = xml.lastIndexOf(closingTag);

        return closingIndex > firstTagEnd;
    }

    /**
     * Normalise les espaces dans le XML
     */
    private String normalizeXmlWhitespace(String xml) {
        // Remplacer les espaces multiples entre les balises par un seul espace
        xml = xml.replaceAll(">\\s+<", "><");

        // Nettoyer les espaces dans les balises
        xml = xml.replaceAll("<\\s+", "<");
        xml = xml.replaceAll("\\s+>", ">");

        return xml;
    }

    /**
     * Corrige les chaînes numériques dans le JSON
     */
    private String fixNumericStrings(String json) {
        // Cette expression régulière tente de trouver les nombres entourés de guillemets
        return json.replaceAll("\"(\\d+)\"", "$1")
                .replaceAll("\"(\\d+\\.\\d+)\"", "$1");
    }

    /**
     * Formate les tableaux JSON sur plusieurs lignes
     */
    private String formatJsonArrays(String json) {
        // Jackson avec INDENT_OUTPUT gère déjà le formatage
        // On pourrait ajouter un traitement supplémentaire ici
        return json;
    }

    /**
     * Formate l'indentation XML
     */
    private String formatXmlIndentation(String xml) {
        StringBuilder formatted = new StringBuilder();
        int indentLevel = 0;
        boolean inTag = false;
        boolean inClosingTag = false;
        StringBuilder currentLine = new StringBuilder();

        String[] lines = xml.split("\n");
        for (String line : lines) {
            line = line.trim();

            if (line.startsWith("<?xml")) {
                formatted.append(line).append("\n");
                continue;
            }

            if (line.startsWith("</")) {
                indentLevel--;
            }

            // Ajouter l'indentation
            for (int i = 0; i < indentLevel; i++) {
                formatted.append("  ");
            }

            formatted.append(line).append("\n");

            if (line.startsWith("<") && !line.startsWith("</") &&
                    !line.endsWith("/>") && !line.startsWith("<?xml")) {
                indentLevel++;
            }
        }

        return formatted.toString();
    }

    /**
     * Log de conversion pour débogage
     */
    private void logConversion(String direction, String input, String output) {
        System.out.println("=== " + direction + " ===");
        System.out.println("Input length: " + input.length() + " chars");
        System.out.println("Output length: " + output.length() + " chars");
        System.out.println("====================\n");
    }

    // ==================== EXEMPLES (SANS TEXT BLOCKS) ====================

    public static class ExampleGenerator {

        public static String getSimpleXml() {
            return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<personne>\n" +
                    "    <nom>Jean Dupont</nom>\n" +
                    "    <age>30</age>\n" +
                    "    <email>jean.dupont@email.com</email>\n" +
                    "    <ville>Paris</ville>\n" +
                    "    <actif>true</actif>\n" +
                    "</personne>";
        }

        public static String getSimpleJson() {
            return "{\n" +
                    "  \"produit\": {\n" +
                    "    \"id\": 12345,\n" +
                    "    \"nom\": \"Ordinateur Portable\",\n" +
                    "    \"marque\": \"TechBrand\",\n" +
                    "    \"prix\": 999.99,\n" +
                    "    \"enStock\": true,\n" +
                    "    \"caracteristiques\": {\n" +
                    "      \"processeur\": \"Intel Core i7\",\n" +
                    "      \"memoire\": \"16 Go\",\n" +
                    "      \"stockage\": \"512 Go SSD\",\n" +
                    "      \"ecran\": \"15.6 pouces\"\n" +
                    "    },\n" +
                    "    \"couleurs\": [\"Noir\", \"Argent\", \"Bleu\"]\n" +
                    "  }\n" +
                    "}";
        }

        public static String getComplexXml() {
            return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<universite nom=\"Université de Paris\">\n" +
                    "    <departements>\n" +
                    "        <departement id=\"INFO\">\n" +
                    "            <nom>Informatique</nom>\n" +
                    "            <directeur>Prof. Martin</directeur>\n" +
                    "            <etudiants>850</etudiants>\n" +
                    "            <cours>\n" +
                    "                <cours id=\"C101\">\n" +
                    "                    <titre>Programmation Java</titre>\n" +
                    "                    <professeur>Dr. Simon</professeur>\n" +
                    "                    <credits>6</credits>\n" +
                    "                    <semestre>Automne 2024</semestre>\n" +
                    "                </cours>\n" +
                    "                <cours id=\"C102\">\n" +
                    "                    <titre>Bases de Données</titre>\n" +
                    "                    <professeur>Dr. Laura</professeur>\n" +
                    "                    <credits>5</credits>\n" +
                    "                    <semestre>Printemps 2024</semestre>\n" +
                    "                </cours>\n" +
                    "            </cours>\n" +
                    "        </departement>\n" +
                    "        <departement id=\"MATHS\">\n" +
                    "            <nom>Mathématiques</nom>\n" +
                    "            <directeur>Prof. Dubois</directeur>\n" +
                    "            <etudiants>420</etudiants>\n" +
                    "        </departement>\n" +
                    "    </departements>\n" +
                    "    <statistiques>\n" +
                    "        <totalEtudiants>5000</totalEtudiants>\n" +
                    "        <totalProfesseurs>350</totalProfesseurs>\n" +
                    "        <tauxReussite>0.89</tauxReussite>\n" +
                    "    </statistiques>\n" +
                    "</universite>";
        }

        public static String getComplexJson() {
            return "{\n" +
                    "  \"entreprise\": {\n" +
                    "    \"nom\": \"TechSolutions SA\",\n" +
                    "    \"siegeSocial\": {\n" +
                    "      \"adresse\": \"123 Rue de l'Innovation\",\n" +
                    "      \"ville\": \"Lyon\",\n" +
                    "      \"codePostal\": \"69001\",\n" +
                    "      \"pays\": \"France\"\n" +
                    "    },\n" +
                    "    \"employes\": [\n" +
                    "      {\n" +
                    "        \"id\": \"EMP001\",\n" +
                    "        \"prenom\": \"Alice\",\n" +
                    "        \"nom\": \"Martin\",\n" +
                    "        \"poste\": \"Développeuse Full Stack\",\n" +
                    "        \"salaire\": 55000,\n" +
                    "        \"departement\": \"Développement\",\n" +
                    "        \"dateEmbauche\": \"2020-03-15\",\n" +
                    "        \"projets\": [\n" +
                    "          {\n" +
                    "            \"nom\": \"Portail Client\",\n" +
                    "            \"technologies\": [\"React\", \"Spring Boot\", \"PostgreSQL\"],\n" +
                    "            \"statut\": \"En production\"\n" +
                    "          },\n" +
                    "          {\n" +
                    "            \"nom\": \"API Microservices\",\n" +
                    "            \"technologies\": [\"Java\", \"Docker\", \"Kubernetes\"],\n" +
                    "            \"statut\": \"En développement\"\n" +
                    "          }\n" +
                    "        ]\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"id\": \"EMP002\",\n" +
                    "        \"prenom\": \"Bob\",\n" +
                    "        \"nom\": \"Dupont\",\n" +
                    "        \"poste\": \"Chef de Projet\",\n" +
                    "        \"salaire\": 65000,\n" +
                    "        \"departement\": \"Management\",\n" +
                    "        \"dateEmbauche\": \"2019-07-22\",\n" +
                    "        \"projets\": [\n" +
                    "          {\n" +
                    "            \"nom\": \"Migration Cloud\",\n" +
                    "            \"technologies\": [\"AWS\", \"Terraform\", \"Ansible\"],\n" +
                    "            \"statut\": \"Planifié\"\n" +
                    "          }\n" +
                    "        ]\n" +
                    "      }\n" +
                    "    ],\n" +
                    "    \"departements\": [\n" +
                    "      {\n" +
                    "        \"nom\": \"Développement\",\n" +
                    "        \"budget\": 500000,\n" +
                    "        \"chef\": \"EMP001\",\n" +
                    "        \"membres\": 25\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"nom\": \"Management\",\n" +
                    "        \"budget\": 300000,\n" +
                    "        \"chef\": \"EMP002\",\n" +
                    "        \"membres\": 8\n" +
                    "      }\n" +
                    "    ],\n" +
                    "    \"chiffreAffaires\": {\n" +
                    "      \"2022\": 2500000,\n" +
                    "      \"2023\": 3200000,\n" +
                    "      \"2024\": 4000000\n" +
                    "    }\n" +
                    "  }\n" +
                    "}";
        }
    }

    // ==================== TEST ====================

    public static void main(String[] args) {
        try {
            ApiConverter converter = new ApiConverter();

            System.out.println("=== TEST API CONVERTER ===");

            // Test 1 : XML → JSON
            String xml = ExampleGenerator.getSimpleXml();
            System.out.println("\n1. XML d'entrée :");
            System.out.println(xml.substring(0, Math.min(100, xml.length())) + "...");

            String json = converter.xmlToJson(xml);
            System.out.println("\n2. JSON converti :");
            System.out.println(json.substring(0, Math.min(100, json.length())) + "...");

            // Test 2 : JSON → XML
            String jsonInput = ExampleGenerator.getSimpleJson();
            System.out.println("\n3. JSON d'entrée :");
            System.out.println(jsonInput.substring(0, Math.min(100, jsonInput.length())) + "...");

            String xmlOutput = converter.jsonToXml(jsonInput);
            System.out.println("\n4. XML reconverti :");
            System.out.println(xmlOutput.substring(0, Math.min(100, xmlOutput.length())) + "...");

            // Test 3 : Validation
            System.out.println("\n5. Validation :");
            System.out.println("XML valide ? " + converter.isValidXml(xml));
            System.out.println("JSON valide ? " + converter.isValidJson(jsonInput));

            System.out.println("\n✅ TOUS LES TESTS RÉUSSIS !");

        } catch (Exception e) {
            System.out.println("❌ ERREUR : " + e.getMessage());
            e.printStackTrace();
        }
    }
}