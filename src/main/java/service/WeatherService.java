package service;
import models.WeatherData;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class WeatherService {
    private static final String API_KEY = "46167c265a30ecb3e516940e55614809";
    // API gratuite pour les données météo actuelles
    private static final String CURRENT_WEATHER_API = "https://api.openweathermap.org/data/2.5/weather";
    // API gratuite pour les prévisions météo (5 jours / 3 heures)
    private static final String FORECAST_API = "https://api.openweathermap.org/data/2.5/forecast";

    // Ajout d'un cache
    private final Map<String, WeatherData> cache = new HashMap<>();

    public WeatherData getWeatherForDate(LocalDate date, String city) throws IOException {
        if (city == null || city.trim().isEmpty()) {
            System.err.println("Erreur: Nom de ville non spécifié");
            return null;
        }

        String cacheKey = date.toString() + "-" + city;

        // Vérifier si les données sont en cache
        if (cache.containsKey(cacheKey)) {
            System.out.println("Données météo récupérées du cache pour " + city + " le " + date);
            return cache.get(cacheKey);
        }

        System.out.println("Appel API pour " + city + " le " + date);
        
        LocalDate today = LocalDate.now();
        
        // Utiliser l'API météo actuelle pour aujourd'hui
        if (date.isEqual(today)) {
            return getCurrentWeather(city, cacheKey);
        } 
        // Utiliser l'API de prévision pour les dates futures (jusqu'à 5 jours)
        else if (date.isAfter(today) && date.isBefore(today.plusDays(6))) {
            return getForecastWeather(city, date, cacheKey);
        } 
        // Pour les dates au-delà de 5 jours ou les dates passées, fournir des données estimées
        else {
            System.out.println("Date " + date + " hors de la plage disponible pour les prévisions. Génération de données estimées.");
            return getEstimatedWeather(city, date, cacheKey);
        }
    }
    
    // Obtenir la météo actuelle
    private WeatherData getCurrentWeather(String city, String cacheKey) {
        String urlString = String.format("%s?q=%s&units=metric&lang=fr&appid=%s",
                CURRENT_WEATHER_API,
                URLEncoder.encode(city, StandardCharsets.UTF_8),
                API_KEY);
        
        System.out.println("URL (météo actuelle): " + urlString);
        
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(urlString).openConnection();
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            
            int responseCode = conn.getResponseCode();
            System.out.println("Code de réponse HTTP: " + responseCode);
            
            if (responseCode != 200) {
                System.err.println("Erreur API: " + responseCode);
                try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream()))) {
                    String errorMessage = errorReader.lines().collect(Collectors.joining());
                    System.err.println("Message d'erreur: " + errorMessage);
                } catch (Exception e) {
                    System.err.println("Impossible de lire le message d'erreur: " + e.getMessage());
                }
                return null;
            }
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String jsonResponse = reader.lines().collect(Collectors.joining());
                System.out.println("Réponse JSON reçue, longueur: " + jsonResponse.length());
                
                JSONObject response = new JSONObject(jsonResponse);
                
                WeatherData result = new WeatherData();
                JSONObject main = response.getJSONObject("main");
                JSONArray weatherArray = response.getJSONArray("weather");
                JSONObject weather = weatherArray.getJSONObject(0);
                
                result.setTemperature(main.getDouble("temp"));
                result.setCondition(weather.getString("description"));
                result.setHumidity(main.getDouble("humidity"));
                result.setDate(LocalDateTime.now());
                
                System.out.println("Données météo actuelles récupérées pour " + city);
                
                // Ajouter au cache
                cache.put(cacheKey, result);
                return result;
            }
        } catch (Exception e) {
            System.err.println("Exception lors de l'appel API: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
    
    // Obtenir la météo de prévision
    private WeatherData getForecastWeather(String city, LocalDate date, String cacheKey) {
        String urlString = String.format("%s?q=%s&units=metric&lang=fr&appid=%s",
                FORECAST_API,
                URLEncoder.encode(city, StandardCharsets.UTF_8),
                API_KEY);
        
        System.out.println("URL (prévision): " + urlString);
        
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(urlString).openConnection();
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            
            int responseCode = conn.getResponseCode();
            System.out.println("Code de réponse HTTP: " + responseCode);
            
            if (responseCode != 200) {
                System.err.println("Erreur API: " + responseCode);
                try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream()))) {
                    String errorMessage = errorReader.lines().collect(Collectors.joining());
                    System.err.println("Message d'erreur: " + errorMessage);
                } catch (Exception e) {
                    System.err.println("Impossible de lire le message d'erreur: " + e.getMessage());
                }
                return null;
            }
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String jsonResponse = reader.lines().collect(Collectors.joining());
                System.out.println("Réponse JSON reçue, longueur: " + jsonResponse.length());
                
                JSONObject response = new JSONObject(jsonResponse);
                JSONArray forecastList = response.getJSONArray("list");
                
                // Format de la date pour la comparaison
                String targetDateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                
                WeatherData result = null;
                double totalTemp = 0;
                double totalHumidity = 0;
                String condition = null;
                int count = 0;
                
                // Parcourir les prévisions et trouver celles correspondant à la date cible
                for (int i = 0; i < forecastList.length(); i++) {
                    JSONObject forecast = forecastList.getJSONObject(i);
                    String dtTxt = forecast.getString("dt_txt");
                    String forecastDate = dtTxt.split(" ")[0]; // Format: "YYYY-MM-DD HH:MM:SS"
                    
                    if (forecastDate.equals(targetDateStr)) {
                        JSONObject main = forecast.getJSONObject("main");
                        totalTemp += main.getDouble("temp");
                        totalHumidity += main.getDouble("humidity");
                        
                        // Utiliser la condition météo de midi ou la première disponible
                        if (condition == null || dtTxt.contains("12:00")) {
                            JSONArray weatherArray = forecast.getJSONArray("weather");
                            JSONObject weather = weatherArray.getJSONObject(0);
                            condition = weather.getString("description");
                            
                            if (dtTxt.contains("12:00")) {
                                // Si on trouve la prévision de midi, on la garde
                                result = new WeatherData();
                                result.setTemperature(main.getDouble("temp"));
                                result.setCondition(condition);
                                result.setHumidity(main.getDouble("humidity"));
                                
                                // Convertir la date-heure de la prévision
                                LocalDateTime forecastDateTime = LocalDateTime.parse(dtTxt, 
                                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                                result.setDate(forecastDateTime);
                                
                                // Sortir de la boucle si on a trouvé la prévision de midi
                                break;
                            }
                        }
                        
                        count++;
                    }
                }
                
                // Si aucune prévision de midi n'a été trouvée mais qu'il y a des prévisions pour cette date
                if (result == null && count > 0) {
                    result = new WeatherData();
                    result.setTemperature(totalTemp / count);
                    result.setCondition(condition);
                    result.setHumidity(totalHumidity / count);
                    result.setDate(LocalDateTime.of(date, java.time.LocalTime.NOON));
                }
                
                if (result != null) {
                    System.out.println("Données de prévision trouvées pour " + city + " le " + date);
                    // Ajouter au cache
                    cache.put(cacheKey, result);
                    return result;
                } else {
                    System.out.println("Aucune prévision trouvée pour " + city + " le " + date);
                    return getEstimatedWeather(city, date, cacheKey);
                }
            }
        } catch (Exception e) {
            System.err.println("Exception lors de l'appel API: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
    
    // Obtenir des données météo estimées pour les dates hors plage de prévision
    private WeatherData getEstimatedWeather(String city, LocalDate date, String cacheKey) {
        // Pour les dates futures lointaines ou les dates passées, on utilise une estimation
        WeatherData result = new WeatherData();
        
        // Obtenir les données météo actuelles pour avoir une base d'estimation
        WeatherData currentWeather = getCurrentWeather(city, "current-" + city);
        
        if (currentWeather != null) {
            // Estimation basée sur les données actuelles avec une légère variation
            double tempVariation = Math.random() * 4 - 2; // Variation entre -2 et +2 degrés
            result.setTemperature(currentWeather.getTemperature() + tempVariation);
            result.setCondition("Prévision estimée : " + currentWeather.getCondition());
            result.setHumidity(currentWeather.getHumidity());
            result.setDate(LocalDateTime.of(date, java.time.LocalTime.NOON));
            
            // Ajouter au cache
            cache.put(cacheKey, result);
            System.out.println("Données météo estimées générées pour " + city + " le " + date);
            return result;
        } else {
            // Si même les données actuelles ne sont pas disponibles, créer des données par défaut
            result.setTemperature(20.0); // Température par défaut
            result.setCondition("Données non disponibles");
            result.setHumidity(50.0); // Humidité par défaut
            result.setDate(LocalDateTime.of(date, java.time.LocalTime.NOON));
            
            // Ajouter au cache
            cache.put(cacheKey, result);
            System.out.println("Données météo par défaut générées pour " + city + " le " + date);
            return result;
        }
    }

    // Méthode pour vider le cache si nécessaire
    public void clearCache() {
        cache.clear();
    }
}