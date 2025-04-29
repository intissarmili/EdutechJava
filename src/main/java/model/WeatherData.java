package model;

import java.time.LocalDateTime;

public class WeatherData {
    private double temperature;
    private String condition;
    private double humidity;
    private LocalDateTime date;

    public WeatherData() {
        // Constructeur par défaut
    }

    public WeatherData(double temperature, String condition, double humidity) {
        this.temperature = temperature;
        this.condition = condition;
        this.humidity = humidity;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return String.format("%.1f°C, %s, Humidité: %.0f%%", temperature, condition, humidity);
    }
}