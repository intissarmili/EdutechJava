package service;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
 import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.net.HttpURLConnection;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.net.URL;
import java.util.logging.Logger;
import java.util.Properties;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public class DescriptionGeneratorService {
    private final ExecutorService executorService;
    // Utilisation du modèle Mistral pour une meilleure génération de texte
    private final String DEEPINFRA_API_URL = "https://api.deepinfra.com/v1/inference/mistralai/Mistral-7B-Instruct-v0.2";
    private String apiKey;
    private final String DEEPINFRA_MODEL = "mistralai/Mistral-7B-Instruct-v0.2";
    
    // Amélioration du prompt pour des descriptions plus personnalisées
    private static final String PROMPT_TEMPLATE = 
            "Génère une description captivante et informative pour un événement intitulé \"{event_title}\". " + 
            "Analyse d'abord le sujet mentionné dans le titre: " +
            "- S'il s'agit d'un lieu géographique (comme Le Kef, Tunis, Paris, etc.), décris ce lieu, ses attractions et son histoire. " +
            "- S'il s'agit d'une technologie (comme PHP, Java, Symfony), décris les aspects techniques et les bénéfices d'apprentissage. " +
            "- S'il s'agit d'un domaine académique (comme mathématiques, histoire, etc.), décris l'intérêt de ce domaine et ses applications. " +
            "- S'il s'agit d'un événement culturel (comme concert, exposition), décris l'expérience et l'ambiance à attendre. " +
            "Adapte ton style au type d'événement. La description doit être originale et en français. " +
            "Utilise 3-4 phrases maximum et environ 300 caractères.";

    // Map pour générer des descriptions locales en cas d'échec de l'API
    private final Map<String, String> localDescriptionTemplates = new HashMap<>();
    private final Random random = new Random();
    
    private static final Logger LOGGER = Logger.getLogger(DescriptionGeneratorService.class.getName());

    public DescriptionGeneratorService() {
        this.executorService = Executors.newFixedThreadPool(2);
        
        // Initialiser des templates de description pour différents types d'événements
        localDescriptionTemplates.put("formation", 
            "Cet événement offre une opportunité unique d'apprentissage et de développement de compétences. " +
            "Les participants bénéficieront d'une formation pratique et théorique dispensée par des experts reconnus du domaine. " +
            "Ne manquez pas cette chance d'améliorer vos connaissances et d'échanger avec d'autres professionnels.");
            
        localDescriptionTemplates.put("conférence", 
            "Une conférence exceptionnelle réunissant des intervenants de renom et des participants passionnés. " +
            "Au programme : présentations inspirantes, ateliers interactifs et opportunités de networking. " +
            "Élargissez votre réseau professionnel tout en découvrant les dernières tendances du secteur.");
            
        localDescriptionTemplates.put("atelier", 
            "Un atelier pratique conçu pour développer vos compétences de manière interactive et engageante. " +
            "Vous travaillerez sur des projets concrets sous la guidance d'experts chevronnés. " +
            "Repartez avec des connaissances directement applicables à votre contexte professionnel.");
            
        localDescriptionTemplates.put("concert", 
            "Un concert unique qui promet une expérience musicale inoubliable. " +
            "Les artistes vous transporteront à travers un répertoire varié et émotionnel. " +
            "Venez vivre des moments de partage et d'émotions dans une ambiance exceptionnelle.");
            
        localDescriptionTemplates.put("exposition", 
            "Une exposition fascinante qui présente des œuvres remarquables et inspirantes. " +
            "Les visiteurs découvriront une sélection minutieuse de créations innovantes. " +
            "Une expérience culturelle enrichissante à ne pas manquer.");
            
            // Ajouter des templates pour les technologies web et programmation
            localDescriptionTemplates.put("php", 
                "Découvrez les dernières avancées et meilleures pratiques en développement PHP. " +
                "Cette session est conçue pour les développeurs de tous niveaux souhaitant perfectionner leurs compétences. " +
                "Venez explorer des techniques avancées et échanger avec d'autres passionnés de PHP.");
            
            localDescriptionTemplates.put("symfony", 
                "Une immersion dans l'univers du framework Symfony et ses fonctionnalités innovantes. " +
                "Apprenez à maîtriser les outils et concepts qui font de Symfony une référence pour les développeurs PHP. " +
                "Idéal pour ceux qui veulent développer des applications web robustes et maintenables.");
            
            localDescriptionTemplates.put("java", 
                "Une exploration approfondie de l'écosystème Java et de ses applications modernes. " +
                "Découvrez comment tirer parti de cette technologie polyvalente dans vos projets de développement. " +
                "Une opportunité unique d'échanger avec des experts Java et d'améliorer vos compétences techniques.");
            
            localDescriptionTemplates.put("myadmin", 
                "Une session dédiée à l'administration et à la gestion efficace des bases de données MySQL. " +
                "Apprenez à optimiser vos requêtes et à maintenir vos bases de données avec les meilleures pratiques. " +
                "Indispensable pour les administrateurs et développeurs travaillant avec des données structurées.");
            
            localDescriptionTemplates.put("mathématique", 
                "Plongez dans l'univers fascinant des mathématiques appliquées et théoriques. " +
                "Cette session combine rigueur académique et applications pratiques dans divers domaines. " +
                "Une occasion d'approfondir vos connaissances mathématiques et de découvrir leur impact dans le monde moderne.");
            
            localDescriptionTemplates.put("default", 
                "Un événement exclusif soigneusement organisé pour offrir une expérience exceptionnelle aux participants. " +
                "Le programme promet d'être à la fois informatif, engageant et interactif. " +
                "Rejoignez-nous pour une occasion unique de partage et de découverte.");
            
            // Ajouter des templates pour les lieux géographiques en Tunisie
            localDescriptionTemplates.put("kef", 
                "Découvrez Le Kef, l'une des plus belles régions montagneuses du nord-ouest de la Tunisie. " +
                "Explorez son patrimoine historique riche, ses monuments romains et byzantins impressionnants. " +
                "Profitez de ses paysages spectaculaires et de la générosité de ses habitants.");
            
            localDescriptionTemplates.put("tunis", 
                "Vivez une expérience culturelle riche au cœur de Tunis, capitale de la Tunisie. " +
                "Découvrez la médina classée au patrimoine mondial de l'UNESCO et ses souks animés. " +
                "Laissez-vous séduire par la gastronomie locale et l'hospitalité tunisienne légendaire.");
            
            localDescriptionTemplates.put("hammamet", 
                "Évadez-vous à Hammamet, la célèbre station balnéaire tunisienne aux plages de sable fin. " +
                "Profitez du soleil méditerranéen et des activités nautiques dans un cadre idyllique. " +
                "Explorez la médina historique et ses jardins parfumés pour une expérience authentique.");
            
            localDescriptionTemplates.put("sousse", 
                "Partez à la découverte de Sousse, perle du Sahel tunisien riche d'histoire et de culture. " +
                "Émerveillez-vous devant sa médina fortifiée et ses monuments historiques préservés. " +
                "Profitez de ses plages magnifiques et de l'ambiance chaleureuse de cette ville millénaire.");
            
            localDescriptionTemplates.put("carthage", 
                "Plongez dans l'histoire fascinante de Carthage, site archéologique majeur de la Méditerranée. " +
                "Explorez les vestiges de cette puissante civilisation antique qui a marqué l'histoire. " +
                "Une visite incontournable pour les passionnés d'histoire et d'archéologie.");
            
            localDescriptionTemplates.put("djerba", 
                "Évadez-vous sur l'île paradisiaque de Djerba, joyau de la Méditerranée au sud de la Tunisie. " +
                "Découvrez ses villages authentiques, ses plages idylliques et son architecture traditionnelle. " +
                "Imprégnez-vous de la richesse culturelle de cette île où se mêlent influences berbères, arabes et méditerranéennes.");
            
            localDescriptionTemplates.put("kairouan", 
                "Explorez Kairouan, quatrième ville sainte de l'Islam et trésor du patrimoine tunisien. " +
                "Admirez sa Grande Mosquée, l'un des plus importants monuments de l'Islam en Afrique du Nord. " +
                "Déambulez dans ses ruelles pour découvrir l'artisanat traditionnel et l'architecture authentique.");
            
            localDescriptionTemplates.put("douz", 
                "Vivez l'aventure à Douz, la porte du désert tunisien et point de départ vers le Sahara. " +
                "Émerveillez-vous devant les dunes dorées et l'immensité du Grand Erg Oriental. " +
                "Une destination incontournable pour découvrir la vie nomade et les traditions sahariennes.");
            
            // Ajouter la catégorie "gouvernorat" pour la détection prioritaire
            localDescriptionTemplates.put("gouvernorat", "temp");
    }
    
    /**
     * Définit la clé API pour DeepInfra
     * @param apiKey La clé API
     */
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
        System.out.println("Clé API DeepInfra configurée: " + (apiKey != null && !apiKey.isEmpty() ? "Oui" : "Non"));
    }

    /**
     * Génère une description pour un événement de manière asynchrone
     * @param eventTitle Le titre de l'événement
     * @return Une CompletableFuture contenant la description générée
     */
    public CompletableFuture<String> generateDescription(final String eventTitle) {
        LOGGER.info("Génération asynchrone de description pour l'événement: '" + eventTitle + "'");
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Nettoyer le titre de l'événement pour améliorer les résultats
                String cleanedTitle = TextCleaner.cleanText(eventTitle);
                LOGGER.info("Titre nettoyé: '" + cleanedTitle + "'");
                
                if (cleanedTitle.isEmpty()) {
                    LOGGER.warning("Titre vide après nettoyage, utilisation de la description par défaut");
                    return generateLocalDescription(cleanedTitle);
                }
                
                // Appel à l'API DeepInfra pour générer une description
                String apiDescription = generateDescriptionFromDeepInfra(cleanedTitle);
                LOGGER.info("Réponse de l'API pour '" + cleanedTitle + "': " + 
                           (apiDescription != null ? "'" + apiDescription + "'" : "null"));
                
                // Si la description générée par l'API est vide ou null, utiliser la génération locale
                if (apiDescription == null || apiDescription.trim().isEmpty()) {
                    LOGGER.warning("Description API vide ou null, utilisation de la description locale");
                    return generateLocalDescription(cleanedTitle);
                }
                
                return apiDescription;
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Erreur lors de la génération de description pour '" + 
                          eventTitle + "': " + e.getMessage(), e);
                // En cas d'erreur, utiliser la génération locale
                return generateLocalDescription(eventTitle);
            }
        });
    }
    
    /**
     * Génère une description pour un événement de manière synchrone
     * @param eventTitle Le titre de l'événement
     * @return La description générée
     */
    public String generateDescriptionSync(final String eventTitle) {
        LOGGER.info("Génération synchrone de description pour l'événement: '" + eventTitle + "'");
        
        try {
            // Nettoyer le titre de l'événement pour améliorer les résultats
            String cleanedTitle = TextCleaner.cleanText(eventTitle);
            LOGGER.info("Titre nettoyé: '" + cleanedTitle + "'");
            
            if (cleanedTitle.isEmpty()) {
                LOGGER.warning("Titre vide après nettoyage, utilisation de la description par défaut");
                return generateLocalDescription(cleanedTitle);
            }
            
            // Appel à l'API DeepInfra pour générer une description
            String apiDescription = generateDescriptionFromDeepInfra(cleanedTitle);
            LOGGER.info("Réponse de l'API pour '" + cleanedTitle + "': " + 
                       (apiDescription != null ? "'" + apiDescription + "'" : "null"));
            
            // Si la description générée par l'API est vide ou null, utiliser la génération locale
            if (apiDescription == null || apiDescription.trim().isEmpty()) {
                LOGGER.warning("Description API vide ou null, utilisation de la description locale");
                return generateLocalDescription(cleanedTitle);
            }
            
            return apiDescription;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la génération de description pour '" + 
                      eventTitle + "': " + e.getMessage(), e);
            // En cas d'erreur, utiliser la génération locale
            return generateLocalDescription(eventTitle);
        }
    }
    
    /**
     * Génère une description localement sans appel API externe
     * 
     * @param title Le titre de l'événement
     * @return La description générée
     */
    private String generateLocalDescription(String title) {
        if (title == null || title.isEmpty()) {
            return localDescriptionTemplates.get("default");
        }
        
        String lowerTitle = title.toLowerCase();
        
        // Liste des gouvernorats tunisiens pour la détection prioritaire
        String[] gouvernorats = {"kef", "tunis", "sousse", "sfax", "hammamet", "bizerte", "nabeul", 
                               "carthage", "djerba", "kairouan", "tozeur", "douz", "monastir", "mahdia", 
                               "gabès", "gafsa", "jendouba", "béja", "kébili", "tataouine", "zaghouan", "ben arous"};
        
        // Vérifier d'abord si le titre contient un gouvernorat (priorité élevée)
        for (String gouvernorat : gouvernorats) {
            if (lowerTitle.contains(gouvernorat)) {
                System.out.println("Lieu géographique détecté: " + gouvernorat);
                // Si nous avons un template spécifique pour ce gouvernorat
                if (localDescriptionTemplates.containsKey(gouvernorat)) {
                    return localDescriptionTemplates.get(gouvernorat);
                } else {
                    // Description générique pour un lieu sans template spécifique
                    return "Découvrez " + title + ", une destination fascinante en Tunisie. " +
                           "Explorez la richesse culturelle, historique et naturelle de cette région unique. " +
                           "Une occasion parfaite de vivre une expérience authentique au cœur de la Tunisie.";
                }
            }
        }
        
        // Si ce n'est pas un lieu géographique, continuer avec la logique existante
        Map<String, String> matches = new HashMap<>();
        
        // Chercher des mots-clés dans le titre pour déterminer le type d'événement
        for (Map.Entry<String, String> entry : localDescriptionTemplates.entrySet()) {
            // Ignorer le template temporaire "gouvernorat"
            if (!entry.getKey().equals("default") && !entry.getKey().equals("gouvernorat") && lowerTitle.contains(entry.getKey())) {
                matches.put(entry.getKey(), entry.getValue());
                System.out.println("Correspondance trouvée pour le mot-clé: " + entry.getKey());
            }
        }
        
        // Si plusieurs correspondances, prendre la plus longue (plus spécifique)
        if (!matches.isEmpty()) {
            String bestMatch = matches.keySet().stream()
                .sorted((a, b) -> Integer.compare(b.length(), a.length()))
                .findFirst()
                .orElse(null);
                
            System.out.println("Meilleure correspondance: " + bestMatch + " pour le titre: " + title);
            return matches.get(bestMatch);
        }
        
        // Si aucune correspondance, créer une description dynamique basée sur le titre
        System.out.println("Aucun mot-clé correspondant, génération dynamique pour: " + title);
        
        // Tableau de phrases d'introduction
        String[] introductions = {
            "Découvrez " + title + ", un événement incontournable pour les passionnés du domaine. ",
            title + " est une opportunité exceptionnelle de développer vos compétences et connaissances. ",
            "Ne manquez pas " + title + ", l'événement référence qui rassemble experts et novices. ",
            "Participez à " + title + " et explorez les dernières tendances et innovations du secteur. "
        };
        
        // Tableau de phrases de description
        String[] descriptions = {
            "Au programme : présentations, démonstrations et échanges enrichissants. ",
            "Cette expérience unique vous permettra d'élargir votre réseau professionnel. ",
            "Les intervenants de renom partageront leur expertise et vision du domaine. ",
            "Vous repartirez avec des connaissances pratiques directement applicables. "
        };
        
        // Tableau de phrases de conclusion
        String[] conclusions = {
            "Une occasion parfaite de rester à la pointe de l'innovation.",
            "Rejoignez-nous pour cet événement qui s'annonce passionnant et instructif.",
            "Inscrivez-vous dès maintenant pour garantir votre place à cet événement exclusif.",
            "Une expérience professionnelle et conviviale à ne pas manquer."
        };
        
        // Générer une description unique en sélectionnant aléatoirement des phrases
        Random random = new Random(title.hashCode()); // Utiliser le hashcode du titre comme seed pour avoir la même description pour le même titre
        String dynamicDescription = 
            introductions[random.nextInt(introductions.length)] +
            descriptions[random.nextInt(descriptions.length)] +
            conclusions[random.nextInt(conclusions.length)];
            
        return dynamicDescription;
    }
    
    /**
     * Génère une description en utilisant l'API DeepInfra
     * @param eventTitle Le titre de l'événement
     * @return La description générée ou null en cas d'échec
     */
    private String generateDescriptionFromDeepInfra(String eventTitle) {
        LOGGER.info("Tentative de génération via l'API DeepInfra pour: '" + eventTitle + "'");
        
        try {
            // Essayer d'utiliser l'API avec une clé valide
            if (this.apiKey == null || this.apiKey.trim().isEmpty()) {
                LOGGER.warning("Clé API DeepInfra non configurée");
                // Chercher dans les propriétés système ou variables d'environnement
                this.apiKey = System.getProperty("deepinfra.api.key");
                if (this.apiKey == null || this.apiKey.trim().isEmpty()) {
                    this.apiKey = System.getenv("DEEPINFRA_API_KEY");
                }
                // Utiliser une clé par défaut si aucune n'est trouvée
                if (this.apiKey == null || this.apiKey.trim().isEmpty()) {
                    this.apiKey = "46167c265a30ecb3e516940e55614809"; // Clé par défaut
                }
                LOGGER.info("Tentative avec clé API: " + this.apiKey.substring(0, 5) + "...");
            }
            
            // Essayer une approche locale d'analyse intelligente
            String smartLocalDescription = generateSmartLocalDescription(eventTitle);
            
            // Construire l'URL de l'API
            if (DEEPINFRA_API_URL == null || DEEPINFRA_API_URL.trim().isEmpty()) {
                LOGGER.warning("URL de l'API DeepInfra non configurée");
                return smartLocalDescription;
            }
            
            // Préparer le prompt pour l'API
            String prompt = PROMPT_TEMPLATE.replace("{event_title}", eventTitle);
            
            // Construire le corps de la requête
            String requestBody = String.format(
                "{\"model\":\"%s\",\"input\":\"%s\",\"max_tokens\":200,\"temperature\":0.7}",
                this.DEEPINFRA_MODEL,
                prompt.replace("\"", "\\\"")
            );
            
            LOGGER.info("Requête API DeepInfra: " + requestBody);
            
            // Configurer la connexion HTTP
            URL url = new URL(DEEPINFRA_API_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + this.apiKey);
            connection.setDoOutput(true);
            connection.setConnectTimeout(10000); // 10 secondes de timeout
            connection.setReadTimeout(15000);    // 15 secondes de timeout
            
            // Envoyer la requête
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            // Lire la réponse
            int responseCode = connection.getResponseCode();
            LOGGER.info("Code de réponse API: " + responseCode);
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Lire le corps de la réponse
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    
                    String jsonResponse = response.toString();
                    LOGGER.info("Réponse API brute: " + jsonResponse);
                    
                    // Extraire la description générée de la réponse JSON
                    String generatedText = extractGeneratedTextFromResponse(jsonResponse);
                    LOGGER.info("Texte extrait de la réponse: '" + generatedText + "'");
                    
                    if (generatedText != null && !generatedText.trim().isEmpty()) {
                        return generatedText;
                    } else {
                        LOGGER.warning("Texte généré vide, utilisation de la description locale intelligente");
                        return smartLocalDescription;
                    }
                }
            } else {
                // Lire le message d'erreur
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    LOGGER.warning("Erreur API DeepInfra: " + response.toString());
                }
                
                return smartLocalDescription;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception lors de l'appel à l'API DeepInfra: " + e.getMessage(), e);
            return generateSmartLocalDescription(eventTitle);
        }
    }
    
    /**
     * Génère une description localement de manière intelligente en analysant le sujet
     * Cette méthode est utilisée comme fallback quand l'API n'est pas disponible
     *
     * @param title Le titre de l'événement
     * @return Une description intelligente générée localement
     */
    private String generateSmartLocalDescription(String title) {
        if (title == null || title.isEmpty()) {
            return localDescriptionTemplates.get("default");
        }
        
        String lowerTitle = title.toLowerCase();
        LOGGER.info("Génération intelligente locale pour: " + title);
        
        // Analyse sémantique du titre pour déterminer sa catégorie
        Map<String, Double> categoryScores = new HashMap<>();
        
        // 1. Lieux géographiques
        String[] lieuGeographique = {"kef", "tunis", "sousse", "sfax", "hammamet", "bizerte", "nabeul", 
                                  "carthage", "djerba", "kairouan", "tozeur", "douz", "monastir", "mahdia", 
                                  "gabès", "gafsa", "jendouba", "béja", "kébili", "tataouine", "zaghouan", "ben arous",
                                  "paris", "marseille", "lyon", "nice", "ville", "pays", "région", "montagne", "plage"};
        
        // 2. Technologies informatiques
        String[] technologieInfo = {"php", "java", "python", "javascript", "html", "css", "symfony", "laravel", 
                                 "angular", "react", "vue", "node", "mysql", "mongodb", "postgresql", "docker", 
                                 "kubernetes", "git", "devops", "cloud", "api", "développement", "programmation",
                                 "myadmin", "database", "web", "mobile", "app", "application"};
        
        // 3. Académique et éducation
        String[] academique = {"mathématique", "physique", "chimie", "biologie", "histoire", "géographie", 
                            "philosophie", "littérature", "langues", "économie", "droit", "médecine", 
                            "ingénierie", "architecture", "éducation", "formation", "cours", "atelier",
                            "conférence", "séminaire", "workshop", "étude", "recherche", "science"};
        
        // 4. Culture et arts
        String[] cultureArts = {"musique", "concert", "festival", "exposition", "théâtre", "cinéma", "danse", 
                             "peinture", "sculpture", "photo", "littérature", "poésie", "art", "culturel",
                             "spectacle", "performance", "show", "gallery", "musée"};
        
        // 5. Business et entrepreneuriat
        String[] business = {"business", "entreprise", "startup", "entrepreneuriat", "management", "marketing", 
                          "finance", "commerce", "vente", "négociation", "leadership", "innovation", 
                          "stratégie", "projet", "produit", "service", "client", "marché", "économie"};
        
        // Calculer les scores pour chaque catégorie
        categoryScores.put("lieu", calculateCategoryScore(lowerTitle, lieuGeographique));
        categoryScores.put("tech", calculateCategoryScore(lowerTitle, technologieInfo));
        categoryScores.put("academique", calculateCategoryScore(lowerTitle, academique));
        categoryScores.put("culture", calculateCategoryScore(lowerTitle, cultureArts));
        categoryScores.put("business", calculateCategoryScore(lowerTitle, business));
        
        // Trouver la catégorie avec le score le plus élevé
        String topCategory = categoryScores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("default");
        
        double topScore = categoryScores.get(topCategory);
        LOGGER.info("Catégorie détectée: " + topCategory + " avec un score de " + topScore);
        
        // Si le score est trop faible, utiliser une description générale
        if (topScore < 0.1) {
            topCategory = "default";
        }
        
        // Générer une description en fonction de la catégorie détectée
        switch (topCategory) {
            case "lieu":
                // Vérifier si nous avons un template spécifique pour ce lieu
                for (String lieu : lieuGeographique) {
                    if (lowerTitle.contains(lieu) && localDescriptionTemplates.containsKey(lieu)) {
                        return localDescriptionTemplates.get(lieu);
                    }
                }
                // Sinon, générer une description générique pour un lieu
                return "Découvrez " + title + ", une destination fascinante à explorer. " +
                       "Profitez de son patrimoine culturel, de ses attractions et de son ambiance unique. " +
                       "Une occasion parfaite pour vivre une expérience authentique dans un cadre exceptionnel.";
                
            case "tech":
                // Vérifier si nous avons un template spécifique pour cette technologie
                for (String tech : technologieInfo) {
                    if (lowerTitle.contains(tech) && localDescriptionTemplates.containsKey(tech)) {
                        return localDescriptionTemplates.get(tech);
                    }
                }
                // Sinon, générer une description générique pour une technologie
                return "Plongez dans l'univers de " + title + " et maîtrisez cette technologie essentielle. " +
                       "Cette session vous permettra d'acquérir des compétences pratiques et des connaissances approfondies. " +
                       "Une opportunité idéale pour développer votre expertise et échanger avec d'autres professionnels du domaine.";
                
            case "academique":
                return "Explorez le domaine passionnant de " + title + " lors de cet événement académique enrichissant. " +
                       "Bénéficiez d'un enseignement de qualité et découvrez les dernières avancées dans ce champ d'étude. " +
                       "Une occasion unique d'enrichir vos connaissances et d'échanger avec des experts reconnus.";
                
            case "culture":
                return "Vivez une expérience culturelle inoubliable autour de " + title + ". " +
                       "Laissez-vous transporter par la créativité et l'expression artistique dans une ambiance conviviale. " +
                       "Un moment privilégié pour nourrir votre sensibilité artistique et partager des émotions authentiques.";
                
            case "business":
                return "Participez à cet événement professionnel sur " + title + " et développez votre réseau d'affaires. " +
                       "Découvrez des stratégies innovantes et des opportunités pour faire évoluer votre entreprise. " +
                       "Une plateforme idéale pour rencontrer des partenaires potentiels et explorer de nouvelles perspectives commerciales.";
                
            default:
                // Utiliser la méthode précédente de génération dynamique
                String[] introductions = {
                    "Découvrez " + title + ", un événement incontournable pour les passionnés du domaine. ",
                    title + " est une opportunité exceptionnelle de développer vos compétences et connaissances. ",
                    "Ne manquez pas " + title + ", l'événement référence qui rassemble experts et novices. ",
                    "Participez à " + title + " et explorez les dernières tendances et innovations du secteur. "
                };
                
                String[] descriptions = {
                    "Au programme : présentations, démonstrations et échanges enrichissants. ",
                    "Cette expérience unique vous permettra d'élargir votre réseau professionnel. ",
                    "Les intervenants de renom partageront leur expertise et vision du domaine. ",
                    "Vous repartirez avec des connaissances pratiques directement applicables. "
                };
                
                String[] conclusions = {
                    "Une occasion parfaite de rester à la pointe de l'innovation.",
                    "Rejoignez-nous pour cet événement qui s'annonce passionnant et instructif.",
                    "Inscrivez-vous dès maintenant pour garantir votre place à cet événement exclusif.",
                    "Une expérience professionnelle et conviviale à ne pas manquer."
                };
                
                Random random = new Random(title.hashCode());
                return introductions[random.nextInt(introductions.length)] +
                       descriptions[random.nextInt(descriptions.length)] +
                       conclusions[random.nextInt(conclusions.length)];
        }
    }
    
    /**
     * Calcule un score de correspondance entre un texte et un ensemble de mots-clés
     * 
     * @param text Le texte à analyser
     * @param keywords Les mots-clés à rechercher
     * @return Un score entre 0 et 1 indiquant la correspondance
     */
    private double calculateCategoryScore(String text, String[] keywords) {
        double score = 0;
        int matches = 0;
        
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                // Donner plus de poids aux mots-clés plus longs
                double keywordScore = 0.5 + (0.5 * keyword.length() / 15.0);
                score += keywordScore;
                matches++;
            }
        }
        
        // Normaliser le score en fonction du nombre de mots-clés et de la longueur du texte
        if (matches > 0) {
            // Donner un bonus pour les correspondances multiples
            score = score * (1 + Math.log10(matches) / 3.0);
            // Normaliser par rapport à la longueur du texte pour éviter de biaiser les titres longs
            score = score / Math.sqrt(1 + text.length() / 50.0);
        }
        
        return score;
    }

    /**
     * Extrait le texte généré de la réponse JSON de l'API DeepInfra
     * @param jsonResponse La réponse JSON brute
     * @return Le texte généré ou null si l'extraction échoue
     */
    private String extractGeneratedTextFromResponse(String jsonResponse) {
        try {
            // Utiliser GSON pour parser la réponse JSON
            if (jsonResponse == null || jsonResponse.isEmpty()) {
                LOGGER.warning("Réponse JSON vide");
                return null;
            }
            
            Gson gson = new Gson();
            com.google.gson.JsonObject responseObj = gson.fromJson(jsonResponse, com.google.gson.JsonObject.class);
            
            // Extraire le texte généré selon la structure de la réponse de l'API
            if (responseObj.has("results")) {
                com.google.gson.JsonArray results = responseObj.getAsJsonArray("results");
                if (results.size() > 0) {
                    com.google.gson.JsonObject firstResult = results.get(0).getAsJsonObject();
                    if (firstResult.has("generated_text")) {
                        String generatedText = firstResult.get("generated_text").getAsString();
                        return cleanGeneratedText(generatedText);
                    }
                }
            }
            
            LOGGER.warning("Structure de réponse JSON inattendue: " + jsonResponse);
            return null;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'extraction du texte généré: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Nettoie le texte généré par l'API en supprimant les parties inutiles.
     */
    private String cleanGeneratedText(String rawText) {
        if (rawText == null || rawText.isEmpty()) {
            return "Description non disponible";
        }

        // Supprimer les sauts de ligne au début
        String cleaned = rawText.trim();
        
        // Supprimer les balises Title: et Description:
        Pattern titlePattern = Pattern.compile("(?i)^\\s*Title:\\s*'?([^']*)'?\\s*Description:", Pattern.DOTALL);
        Matcher titleMatcher = titlePattern.matcher(cleaned);
        if (titleMatcher.find()) {
            // Si on trouve le pattern Title: ... Description:, on prend ce qui suit Description:
            cleaned = cleaned.substring(cleaned.toLowerCase().indexOf("description:") + "description:".length());
        } else {
            // Chercher seulement Title: au début
            Pattern onlyTitlePattern = Pattern.compile("(?i)^\\s*Title:\\s*'?([^']*)'?", Pattern.DOTALL);
            Matcher onlyTitleMatcher = onlyTitlePattern.matcher(cleaned);
            if (onlyTitleMatcher.find()) {
                cleaned = cleaned.substring(cleaned.toLowerCase().indexOf("title:") + "title:".length());
            }
            
            // Chercher Description: n'importe où
            if (cleaned.toLowerCase().contains("description:")) {
                cleaned = cleaned.substring(cleaned.toLowerCase().indexOf("description:") + "description:".length());
            }
        }

        // Supprimer les préfixes communs
        String[] prefixesToRemove = {
                "Voici une description:",
                "Description:",
                "Réponse:",
                "Voici la description:",
                "Voici ma description:",
                "Description de l'événement:",
                "Voici une suggestion de description:"
        };

        for (String prefix : prefixesToRemove) {
            if (cleaned.toLowerCase().startsWith(prefix.toLowerCase())) {
                cleaned = cleaned.substring(prefix.length()).trim();
            }
        }

        // Supprimer les guillemets au début et à la fin
        if (cleaned.startsWith("'") && cleaned.endsWith("'")) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
        }
        if (cleaned.startsWith("\"") && cleaned.endsWith("\"")) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
        }

        // Extraire seulement la partie pertinente si le modèle a inclus des instructions
        if (cleaned.contains("\n\n")) {
            String[] parts = cleaned.split("\n\n", 2);
            cleaned = parts.length > 1 ? parts[1] : parts[0];
        }
        
        // Supprimer les marqueurs d'instruction du modèle souvent présents dans les sorties Mistral
        cleaned = cleaned.replaceAll("(?i)<instructions>.*?</instructions>", "");
        cleaned = cleaned.replaceAll("(?i)<\\/?assistant>", "");
        cleaned = cleaned.replaceAll("(?i)<\\/?user>", "");
        
        // Remplacer les sauts de ligne multiples par un espace
        cleaned = cleaned.replaceAll("\\n+", " ");
        
        // Supprimer les caractères spéciaux souvent présents dans les sorties de l'API
        cleaned = cleaned.replaceAll("^[\\s\\p{Punct}]+", ""); // Supprime ponctuation au début
        cleaned = cleaned.replaceAll("[\\s\\p{Punct}]+$", ""); // Supprime ponctuation à la fin
        
        // Supprimer les balises de formatage et les mentions comme 📌 au début
        cleaned = cleaned.replaceAll("^\\s*[\\p{Punct}\\p{InEmoticons}\\p{InDingbats}\\p{InMiscellaneousSymbols}]+\\s*", "");

        // Limiter à une longueur raisonnable
        int maxLength = 450;
        if (cleaned.length() > maxLength) {
            // Trouver la fin de la dernière phrase complète dans la limite
            int lastSentenceEnd = cleaned.substring(0, maxLength).lastIndexOf('.');
            if (lastSentenceEnd > 0) {
                cleaned = cleaned.substring(0, lastSentenceEnd + 1);
            } else {
                cleaned = cleaned.substring(0, maxLength) + "...";
            }
        }

        // Vérification finale d'un contenu minimal
        if (cleaned.length() < 15) {
            return "Description non disponible";
        }

        return cleaned.trim();
    }

    /**
     * Lit le contenu d'un flux d'entrée et le convertit en chaîne de caractères
     */
    private String readInputStream(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return "";
        }
        
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        
        return content.toString().trim();
    }

    /**
     * Arrête proprement l'ExecutorService lorsqu'il n'est plus nécessaire.
     */
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}

// Classe utilitaire pour nettoyer le texte
class TextCleaner {
    private static final Logger LOGGER = Logger.getLogger(TextCleaner.class.getName());
    
    /**
     * Nettoie un texte pour le rendre plus présentable
     * 
     * @param text Le texte à nettoyer
     * @return Le texte nettoyé
     */
    public static String cleanText(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        
        try {
            // Enlever les caractères spéciaux qui ne sont pas nécessaires
            String cleaned = text.trim()
                    .replaceAll("\\s+", " ")
                    .replaceAll("[\\\\\\n\\r]+", " ")
                    .replaceAll("\\s+", " ");
            
            // Retirer les guillemets au début et à la fin s'ils sont présents
            if ((cleaned.startsWith("\"") && cleaned.endsWith("\"")) || 
                (cleaned.startsWith("'") && cleaned.endsWith("'"))) {
                cleaned = cleaned.substring(1, cleaned.length() - 1);
            }
            
            return cleaned;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erreur lors du nettoyage du texte: " + e.getMessage(), e);
            return text;
        }
    }
}