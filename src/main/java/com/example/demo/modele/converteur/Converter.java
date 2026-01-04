package com.example.demo.modele.converteur;

    /**
     * Interface commune pour tous les convertisseurs
     * Utilise le pattern Strategy pour permettre différentes implémentations
     */
    public interface Converter {

        /**
         * Convertit une chaîne XML en JSON
         * @param xml La chaîne XML à convertir
         * @return La chaîne JSON résultante
         * @throws Exception Si la conversion échoue
         */
        String xmlToJson(String xml) throws Exception;

        /**
         * Convertit une chaîne JSON en XML
         * @param json La chaîne JSON à convertir
         * @return La chaîne XML résultante
         * @throws Exception Si la conversion échoue
         */
        String jsonToXml(String json) throws Exception;

        /**
         * Retourne le nom du convertisseur
         * @return Le nom descriptif
         */
        String getName();

        /**
         * Retourne la description du convertisseur
         * @return La description
         */
        default String getDescription() {
            return "Convertisseur XML/JSON";
        }

        /**
         * Retourne la version du convertisseur
         * @return La version
         */
        default String getVersion() {
            return "1.0";
        }
    }
