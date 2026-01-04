package com.example.demo.controleur;

import com.example.demo.modele.converteur.ApiConverter;
import com.example.demo.modele.converteur.ManualConverter;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.scene.layout.VBox;

import java.io.File;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class MainController {

    @FXML private TextArea inputTextArea;
    @FXML private TextArea outputTextArea;
    @FXML private ComboBox<String> methodComboBox;
    @FXML private Button convertToJsonBtn;
    @FXML private Button convertToXmlBtn;
    @FXML private Button clearBtn;
    @FXML private Button copyBtn;
    @FXML private Button saveBtn;
    @FXML private Button loadFileBtn;
    @FXML private Button loadExampleBtn;
    @FXML private Button validateBtn;
    @FXML private Label statusLabel;
    @FXML private Label charCountLabel;
    @FXML private ProgressIndicator progressIndicator;

    private Map<String, String> conversionHistory;

    @FXML
    public void initialize() {
        // Initialiser l'historique
        conversionHistory = new HashMap<>();

        // Initialiser la combobox avec les méthodes disponibles
        methodComboBox.getItems().addAll(
                "Avec API (Jackson) - Rapide et robuste",
                "Sans API (Manuelle) - Pédagogique"
        );
        methodComboBox.getSelectionModel().selectFirst();

        // Configurer les Tooltips
        setupTooltips();

        // Configurer les écouteurs d'événements
        setupEventListeners();

        // Style initial
        updateStatus("Prêt à convertir - Sélectionnez une méthode et entrez du XML ou JSON", "info");
        updateCharCount();

        // Masquer l'indicateur de progression initialement
        progressIndicator.setVisible(false);

        // Configurer le drag & drop
        setupDragAndDrop();
    }

    // ============ MÉTHODE 1 : CHARGER FICHIER ============
    @FXML
    private void handleLoadFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner un fichier XML ou JSON");

        // Configurer les filtres d'extension
        FileChooser.ExtensionFilter xmlFilter = new FileChooser.ExtensionFilter(
                "Fichiers XML (*.xml)", "*.xml");
        FileChooser.ExtensionFilter jsonFilter = new FileChooser.ExtensionFilter(
                "Fichiers JSON (*.json)", "*.json");
        FileChooser.ExtensionFilter txtFilter = new FileChooser.ExtensionFilter(
                "Fichiers texte (*.txt)", "*.txt");
        FileChooser.ExtensionFilter allFilter = new FileChooser.ExtensionFilter(
                "Tous les fichiers", "*.*");

        fileChooser.getExtensionFilters().addAll(xmlFilter, jsonFilter, txtFilter, allFilter);

        // Obtenir la fenêtre principale
        Stage stage = (Stage) inputTextArea.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            loadFileContent(selectedFile);
        }
    }

    private void loadFileContent(File file) {
        try {
            // Afficher un indicateur de chargement
            progressIndicator.setVisible(true);
            updateStatus("📂 Chargement du fichier...", "info");

            // Lire le contenu du fichier
            String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);

            // Mettre à jour la zone de texte
            inputTextArea.setText(content);

            // Mettre à jour le compteur de caractères
            updateCharCount();

            // Détection automatique du type de fichier
            autoDetectFileType(file, content);

            // Mettre à jour le statut
            String fileSize = formatFileSize(file.length());
            updateStatus("✅ Fichier chargé: " + file.getName() + " (" + fileSize + ")", "success");

            // Enregistrer dans l'historique
            addToHistory("📂 " + file.getName(), content, "Chargement fichier", "Système");

            // Détection automatique du type pour les boutons
            autoEnableButtons(file, content);

        } catch (Exception e) {
            updateStatus("❌ Erreur de chargement: " + e.getMessage(), "error");
            showAlert("Erreur de chargement",
                    "Impossible de lire le fichier: " + file.getName(),
                    "Détails: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        } finally {
            // Cacher l'indicateur de progression
            progressIndicator.setVisible(false);
        }
    }

    private void autoDetectFileType(File file, String content) {
        String fileName = file.getName().toLowerCase();
        String trimmedContent = content.trim();

        // Détection basée sur l'extension du fichier
        if (fileName.endsWith(".xml")) {
            // Appliquer un style spécial pour XML
            inputTextArea.setStyle("-fx-font-family: 'Consolas', 'Monaco', monospace; " +
                    "-fx-font-size: 13px; " +
                    "-fx-control-inner-background: #e8f4f8; " +
                    "-fx-border-color: #3498db;");

        } else if (fileName.endsWith(".json")) {
            // Appliquer un style spécial pour JSON
            inputTextArea.setStyle("-fx-font-family: 'Consolas', 'Monaco', monospace; " +
                    "-fx-font-size: 13px; " +
                    "-fx-control-inner-background: #f8f0e8; " +
                    "-fx-border-color: #9b59b6;");

        } else {
            // Détection basée sur le contenu
            if (trimmedContent.startsWith("<") && trimmedContent.endsWith(">")) {
                inputTextArea.setStyle("-fx-font-family: 'Consolas', 'Monaco', monospace; " +
                        "-fx-font-size: 13px; " +
                        "-fx-control-inner-background: #e8f4f8; " +
                        "-fx-border-color: #3498db;");
            } else if (trimmedContent.startsWith("{") || trimmedContent.startsWith("[")) {
                inputTextArea.setStyle("-fx-font-family: 'Consolas', 'Monaco', monospace; " +
                        "-fx-font-size: 13px; " +
                        "-fx-control-inner-background: #f8f0e8; " +
                        "-fx-border-color: #9b59b6;");
            }
        }
    }

    private void autoEnableButtons(File file, String content) {
        String fileName = file.getName().toLowerCase();
        String trimmedContent = content.trim();

        boolean isXml = false;
        boolean isJson = false;

        // Détection basée sur l'extension
        if (fileName.endsWith(".xml")) {
            isXml = true;
        } else if (fileName.endsWith(".json")) {
            isJson = true;
        }
        // Détection basée sur le contenu
        else if (trimmedContent.startsWith("<") && trimmedContent.endsWith(">")) {
            isXml = true;
        } else if (trimmedContent.startsWith("{") || trimmedContent.startsWith("[")) {
            isJson = true;
        }

        // Activer/désactiver les boutons en conséquence
        if (isXml) {
            convertToJsonBtn.setDisable(false);
            convertToXmlBtn.setDisable(true);
            convertToJsonBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15;");
            convertToXmlBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-padding: 8 15;");
        } else if (isJson) {
            convertToJsonBtn.setDisable(true);
            convertToXmlBtn.setDisable(false);
            convertToJsonBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-padding: 8 15;");
            convertToXmlBtn.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15;");
        } else {
            // Si le type est inconnu, activer les deux boutons
            convertToJsonBtn.setDisable(false);
            convertToXmlBtn.setDisable(false);
            convertToJsonBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15;");
            convertToXmlBtn.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15;");
        }
    }

    // ============ MÉTHODE 2 : CHARGER EXEMPLE ============
    @FXML
    private void handleLoadExample() {
        // Liste des options avec leurs descriptions
        Map<String, String> options = new LinkedHashMap<>();
        options.put("XML Simple", "Un exemple simple de structure XML (personne)");
        options.put("XML Complexe", "Un exemple complexe de structure XML (bibliothèque)");
        options.put("JSON Simple", "Un exemple simple de structure JSON (personne)");
        options.put("JSON Complexe", "Un exemple complexe de structure JSON (entreprise)");

        // Créer un ChoiceDialog
        ChoiceDialog<String> dialog = new ChoiceDialog<>("XML Simple", options.keySet());
        dialog.setTitle("Charger un exemple");
        dialog.setHeaderText("Sélectionnez le type d'exemple à charger");
        dialog.setContentText("Choisissez:");

        // Personnaliser la boîte de dialogue
        dialog.getDialogPane().setPrefSize(400, 250);

        // Afficher et traiter le résultat
        Optional<String> result = dialog.showAndWait();

        if (result.isPresent()) {
            String choice = result.get();
            String example = "";
            boolean isXml = false;
            boolean isJson = false;

            switch (choice) {
                case "XML Simple":
                    example = getSimpleXmlExample();
                    isXml = true;
                    break;
                case "XML Complexe":
                    example = getComplexXmlExample();
                    isXml = true;
                    break;
                case "JSON Simple":
                    example = getSimpleJsonExample();
                    isJson = true;
                    break;
                case "JSON Complexe":
                    example = getComplexJsonExample();
                    isJson = true;
                    break;
            }

            inputTextArea.setText(example);
            updateCharCount();

            // Appliquer le style approprié
            if (isXml) {
                inputTextArea.setStyle("-fx-font-family: 'Consolas', 'Monaco', monospace; " +
                        "-fx-font-size: 13px; " +
                        "-fx-control-inner-background: #e8f4f8; " +
                        "-fx-border-color: #3498db;");
                convertToJsonBtn.setDisable(false);
                convertToXmlBtn.setDisable(true);
                convertToJsonBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15;");
                convertToXmlBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-padding: 8 15;");
            } else if (isJson) {
                inputTextArea.setStyle("-fx-font-family: 'Consolas', 'Monaco', monospace; " +
                        "-fx-font-size: 13px; " +
                        "-fx-control-inner-background: #f8f0e8; " +
                        "-fx-border-color: #9b59b6;");
                convertToJsonBtn.setDisable(true);
                convertToXmlBtn.setDisable(false);
                convertToJsonBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-padding: 8 15;");
                convertToXmlBtn.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15;");
            }

            updateStatus("✅ Exemple chargé: " + choice + " (" + example.length() + " caractères)", "success");
            addToHistory(example, "", "Chargement exemple: " + choice, "Système");
        }
    }

    // ============ MÉTHODES DE SUPPORT ============
    private void setupDragAndDrop() {
        // Permettre le drag & drop de fichiers sur la zone de texte
        inputTextArea.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles()) {
                event.acceptTransferModes(javafx.scene.input.TransferMode.COPY);
            }
            event.consume();
        });

        inputTextArea.setOnDragDropped(event -> {
            javafx.scene.input.Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                File file = db.getFiles().get(0);
                loadFileContent(file);
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " octets";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f Ko", bytes / 1024.0);
        } else {
            return String.format("%.1f Mo", bytes / (1024.0 * 1024.0));
        }
    }

    // ============ EXEMPLES PRÉDÉFINIS (Sans text blocks) ============
    private String getSimpleXmlExample() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<personne>\n" +
                "    <nom>Jean Dupont</nom>\n" +
                "    <age>30</age>\n" +
                "    <email>jean.dupont@email.com</email>\n" +
                "    <ville>Paris</ville>\n" +
                "    <competences>\n" +
                "        <competence>Java</competence>\n" +
                "        <competence>Python</competence>\n" +
                "        <competence>XML</competence>\n" +
                "    </competences>\n" +
                "</personne>";
    }

    private String getComplexXmlExample() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<bibliothèque>\n" +
                "    <livre id=\"1\">\n" +
                "        <titre>Programmation Java</titre>\n" +
                "        <auteur>\n" +
                "            <nom>Martin Fowler</nom>\n" +
                "            <nationalité>Britannique</nationalité>\n" +
                "        </auteur>\n" +
                "        <année>2022</année>\n" +
                "        <pages>450</pages>\n" +
                "        <genres>\n" +
                "            <genre>Informatique</genre>\n" +
                "            <genre>Programmation</genre>\n" +
                "        </genres>\n" +
                "    </livre>\n" +
                "    <livre id=\"2\">\n" +
                "        <titre>Design Patterns</titre>\n" +
                "        <auteur>\n" +
                "            <nom>Erich Gamma</nom>\n" +
                "            <nationalité>Américaine</nationalité>\n" +
                "        </auteur>\n" +
                "        <année>1994</année>\n" +
                "        <pages>395</pages>\n" +
                "        <genres>\n" +
                "            <genre>Informatique</genre>\n" +
                "            <genre>Conception</genre>\n" +
                "        </genres>\n" +
                "    </livre>\n" +
                "    <statistiques>\n" +
                "        <total-livres>2</total-livres>\n" +
                "        <total-pages>845</total-pages>\n" +
                "        <moyenne-pages>422.5</moyenne-pages>\n" +
                "    </statistiques>\n" +
                "</bibliothèque>";
    }

    private String getSimpleJsonExample() {
        return "{\n" +
                "  \"personne\": {\n" +
                "    \"nom\": \"Marie Curie\",\n" +
                "    \"age\": 45,\n" +
                "    \"email\": \"marie.curie@science.fr\",\n" +
                "    \"ville\": \"Paris\",\n" +
                "    \"competences\": [\"Physique\", \"Chimie\", \"Recherche\"],\n" +
                "    \"prixNobel\": [\n" +
                "      {\n" +
                "        \"année\": 1903,\n" +
                "        \"domaine\": \"Physique\",\n" +
                "        \"partagéAvec\": [\"Pierre Curie\", \"Henri Becquerel\"]\n" +
                "      },\n" +
                "      {\n" +
                "        \"année\": 1911,\n" +
                "        \"domaine\": \"Chimie\",\n" +
                "        \"partagéAvec\": []\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "}";
    }

    private String getComplexJsonExample() {
        return "{\n" +
                "  \"entreprise\": {\n" +
                "    \"nom\": \"TechSolutions Inc.\",\n" +
                "    \"fondation\": 2010,\n" +
                "    \"siegeSocial\": {\n" +
                "      \"ville\": \"San Francisco\",\n" +
                "      \"pays\": \"États-Unis\",\n" +
                "      \"employes\": 250\n" +
                "    },\n" +
                "    \"departements\": [\n" +
                "      {\n" +
                "        \"nom\": \"Développement\",\n" +
                "        \"directeur\": \"Alice Martin\",\n" +
                "        \"employes\": 120,\n" +
                "        \"projets\": [\n" +
                "          {\n" +
                "            \"nom\": \"NovaAI\",\n" +
                "            \"budget\": 500000,\n" +
                "            \"statut\": \"En cours\",\n" +
                "            \"technologies\": [\"Java\", \"Python\", \"TensorFlow\"]\n" +
                "          },\n" +
                "          {\n" +
                "            \"nom\": \"CloudSync\",\n" +
                "            \"budget\": 300000,\n" +
                "            \"statut\": \"Terminé\",\n" +
                "            \"technologies\": [\"React\", \"Node.js\", \"MongoDB\"]\n" +
                "          }\n" +
                "        ]\n" +
                "      },\n" +
                "      {\n" +
                "        \"nom\": \"Marketing\",\n" +
                "        \"directeur\": \"Bob Wilson\",\n" +
                "        \"employes\": 50,\n" +
                "        \"campagnes\": [\n" +
                "          {\n" +
                "            \"nom\": \"Lancement 2024\",\n" +
                "            \"budget\": 100000,\n" +
                "            \"canaux\": [\"Réseaux sociaux\", \"Email\", \"Événements\"]\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    ],\n" +
                "    \"chiffreAffaires\": [\n" +
                "      {\"annee\": 2021, \"montant\": 2500000},\n" +
                "      {\"annee\": 2022, \"montant\": 3200000},\n" +
                "      {\"annee\": 2023, \"montant\": 4100000}\n" +
                "    ]\n" +
                "  }\n" +
                "}";
    }

    // ============ MÉTHODES DE CONVERSION EXISTANTES ============
    @FXML
    private void handleConvertToJson() {
        executeConversion(true); // true pour XML → JSON
    }

    @FXML
    private void handleConvertToXml() {
        executeConversion(false); // false pour JSON → XML
    }

    private void executeConversion(boolean xmlToJson) {
        String input = inputTextArea.getText().trim();

        if (input.isEmpty()) {
            updateStatus("❌ Veuillez entrer du texte à convertir", "error");
            showAlert("Entrée vide",
                    xmlToJson ? "Veuillez entrer du XML à convertir en JSON."
                            : "Veuillez entrer du JSON à convertir en XML.",
                    Alert.AlertType.WARNING);
            return;
        }

        // Afficher l'indicateur de progression
        progressIndicator.setVisible(true);
        updateStatus("Conversion en cours...", "info");

        // Exécuter dans un thread séparé
        new Thread(() -> {
            try {
                // Récupérer le type de conversion
                String selectedMethod = methodComboBox.getSelectionModel().getSelectedItem();
                boolean useApi = selectedMethod.contains("API");

                String result;
                String converterName;

                if (useApi) {
                    // Méthode API
                    ApiConverter converter = new ApiConverter();
                    converterName = converter.getName();
                    result = xmlToJson ? converter.xmlToJson(input) : converter.jsonToXml(input);
                } else {
                    // Méthode manuelle
                    ManualConverter converter = new ManualConverter();
                    converterName = converter.getName();
                    result = xmlToJson ? converter.xmlToJson(input) : converter.jsonToXml(input);
                }

                // Mettre à jour l'interface
                javafx.application.Platform.runLater(() -> {
                    outputTextArea.setText(result);

                    String direction = xmlToJson ? "XML → JSON" : "JSON → XML";
                    updateStatus(String.format(
                            "✅ %s réussie (%s) - %d caractères",
                            direction, converterName, result.length()
                    ), "success");

                    // Ajouter dans l'historique
                    addToHistory(input, result, direction, converterName);

                    // Masquer l'indicateur
                    progressIndicator.setVisible(false);
                });

            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    String direction = xmlToJson ? "XML → JSON" : "JSON → XML";
                    updateStatus("❌ Erreur de conversion " + direction + ": " + e.getMessage(), "error");

                    showAlert("Erreur de conversion",
                            "Détails: " + e.getMessage() +
                                    "\n\nVérifiez que votre " + (xmlToJson ? "XML" : "JSON") + " est correctement formé.",
                            Alert.AlertType.ERROR);

                    progressIndicator.setVisible(false);
                });
            }
        }).start();
    }

    @FXML
    private void handleClear() {
        inputTextArea.clear();
        outputTextArea.clear();
        inputTextArea.setStyle("-fx-font-family: 'Consolas', 'Monaco', monospace; -fx-font-size: 13px;");
        updateStatus("Champs effacés", "info");
        updateCharCount();

        // Réactiver tous les boutons
        convertToJsonBtn.setDisable(false);
        convertToXmlBtn.setDisable(false);
        convertToJsonBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15;");
        convertToXmlBtn.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15;");
    }

    @FXML
    private void handleCopy() {
        String output = outputTextArea.getText();
        if (!output.isEmpty()) {
            javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
            javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
            content.putString(output);
            clipboard.setContent(content);
            updateStatus("✅ Résultat copié dans le presse-papier", "success");
            showAlert("Succès", "Résultat copié dans le presse-papier", Alert.AlertType.INFORMATION);
        } else {
            updateStatus("❌ Aucun résultat à copier", "error");
        }
    }

    @FXML
    private void handleSave() {
        String content = outputTextArea.getText();
        if (content.isEmpty()) {
            showAlert("Aucun contenu", "Il n'y a aucun contenu à enregistrer.", Alert.AlertType.WARNING);
            return;
        }

        // Implémentation d'enregistrement
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le résultat");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Fichier texte", "*.txt"),
                new FileChooser.ExtensionFilter("Fichier XML", "*.xml"),
                new FileChooser.ExtensionFilter("Fichier JSON", "*.json")
        );

        File file = fileChooser.showSaveDialog(outputTextArea.getScene().getWindow());
        if (file != null) {
            try {
                Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);
                updateStatus("✅ Fichier enregistré: " + file.getName(), "success");
                showAlert("Succès", "Fichier enregistré avec succès: " + file.getAbsolutePath(),
                        Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                updateStatus("❌ Erreur d'enregistrement", "error");
                showAlert("Erreur", "Impossible d'enregistrer le fichier: " + e.getMessage(),
                        Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleValidateInput() {
        String input = inputTextArea.getText().trim();

        if (input.isEmpty()) {
            showAlert("Validation", "L'entrée est vide.", Alert.AlertType.INFORMATION);
            return;
        }

        try {
            String selectedMethod = methodComboBox.getSelectionModel().getSelectedItem();
            boolean useApi = selectedMethod.contains("API");

            if (useApi) {
                ApiConverter apiConverter = new ApiConverter();

                // Tente de détecter le type
                boolean isXml = input.trim().startsWith("<");
                boolean isJson = input.trim().startsWith("{") || input.trim().startsWith("[");

                if (isXml) {
                    boolean valid = apiConverter.isValidXml(input);
                    if (valid) {
                        Map<String, Object> stats = apiConverter.analyzeContent(input, "XML");
                        showAlert("Validation XML",
                                "✅ XML valide\n" +
                                        "Élément racine: " + stats.get("rootElement") + "\n" +
                                        "Taille: " + stats.get("size") + " caractères",
                                Alert.AlertType.INFORMATION);
                    } else {
                        showAlert("Validation XML", "❌ XML invalide", Alert.AlertType.ERROR);
                    }
                } else if (isJson) {
                    boolean valid = apiConverter.isValidJson(input);
                    if (valid) {
                        Map<String, Object> stats = apiConverter.analyzeContent(input, "JSON");
                        showAlert("Validation JSON",
                                "✅ JSON valide\n" +
                                        "Type: " + (Boolean.TRUE.equals(stats.get("isArray")) ? "Tableau" : "Objet") + "\n" +
                                        "Taille: " + stats.get("size") + " caractères",
                                Alert.AlertType.INFORMATION);
                    } else {
                        showAlert("Validation JSON", "❌ JSON invalide", Alert.AlertType.ERROR);
                    }
                } else {
                    showAlert("Type inconnu",
                            "⚠️ Impossible de déterminer si c'est du XML ou du JSON.\n" +
                                    "XML doit commencer par '<'\n" +
                                    "JSON doit commencer par '{' ou '['",
                            Alert.AlertType.WARNING);
                }
            } else {
                // Pour la méthode manuelle, validation basique
                if (input.contains("<") && input.contains(">")) {
                    showAlert("Validation",
                            "📄 Détecté: XML (validation basique)\n" +
                                    "Pour une validation complète, utilisez la méthode 'Avec API'",
                            Alert.AlertType.INFORMATION);
                } else if (input.contains("{") || input.contains("[")) {
                    showAlert("Validation",
                            "📄 Détecté: JSON (validation basique)\n" +
                                    "Pour une validation complète, utilisez la méthode 'Avec API'",
                            Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Type inconnu",
                            "❌ Format non reconnu.\n" +
                                    "Doit être XML (commence par '<') ou JSON (commence par '{' ou '[')",
                            Alert.AlertType.WARNING);
                }
            }

        } catch (Exception e) {
            showAlert("Erreur de validation", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void setupTooltips() {
        convertToJsonBtn.setTooltip(new Tooltip("Convertir XML en JSON"));
        convertToXmlBtn.setTooltip(new Tooltip("Convertir JSON en XML"));
        methodComboBox.setTooltip(new Tooltip("Choisir la méthode de conversion"));
        clearBtn.setTooltip(new Tooltip("Effacer tous les champs"));
        copyBtn.setTooltip(new Tooltip("Copier le résultat"));
        saveBtn.setTooltip(new Tooltip("Enregistrer le résultat"));
        loadFileBtn.setTooltip(new Tooltip("Charger un fichier XML ou JSON"));
        loadExampleBtn.setTooltip(new Tooltip("Charger un exemple pré-défini"));
        validateBtn.setTooltip(new Tooltip("Valider la syntaxe"));
    }

    private void setupEventListeners() {
        // Mettre à jour le compteur de caractères
        inputTextArea.textProperty().addListener((observable, oldValue, newValue) -> {
            updateCharCount();
        });
    }

    private void updateStatus(String message, String type) {
        statusLabel.setText(message);

        switch (type) {
            case "success":
                statusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                break;
            case "error":
                statusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                break;
            default:
                statusLabel.setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");
        }
    }

    private void updateCharCount() {
        int count = inputTextArea.getText().length();
        charCountLabel.setText("Caractères: " + count);

        if (count > 10000) {
            charCountLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        } else if (count > 1000) {
            charCountLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
        } else {
            charCountLabel.setStyle("-fx-text-fill: #2c3e50;");
        }
    }

    private void addToHistory(String input, String output, String direction, String method) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String key = direction + "|" + method + "|" + timestamp + "|" + truncate(input, 100);
        conversionHistory.put(key, truncate(output, 100));

        // Limiter l'historique à 10 entrées
        if (conversionHistory.size() > 10) {
            String oldestKey = conversionHistory.keySet().iterator().next();
            conversionHistory.remove(oldestKey);
        }
    }

    private String truncate(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAlert(String title, String header, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }
}