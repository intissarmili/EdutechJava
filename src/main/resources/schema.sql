-- Création de la base de données
CREATE DATABASE IF NOT EXISTS app;
USE app;

-- Table des publications
CREATE TABLE IF NOT EXISTS feed (
    id INT AUTO_INCREMENT PRIMARY KEY,
    publication TEXT NOT NULL,
    last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table des commentaires
CREATE TABLE IF NOT EXISTS commentaire (
    id INT AUTO_INCREMENT PRIMARY KEY,
    contenu TEXT NOT NULL,
    feed_id INT NOT NULL,
    up_votes INT DEFAULT 0,
    down_votes INT DEFAULT 0,
    FOREIGN KEY (feed_id) REFERENCES feed(id) ON DELETE CASCADE
);

-- Table de l'historique des publications
CREATE TABLE IF NOT EXISTS feed_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    feed_id INT NOT NULL,
    old_content TEXT,
    new_content TEXT,
    action_type VARCHAR(20) NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (feed_id) REFERENCES feed(id) ON DELETE CASCADE
); 