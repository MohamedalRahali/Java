-- Création de la table type_reclamation
CREATE TABLE IF NOT EXISTS type_reclamation (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name LONGTEXT
);

-- Insertion de quelques types de réclamation par défaut
INSERT INTO type_reclamation (name) VALUES 
('Bug'),
('Feature Request'),
('Support'),
('Other')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Création de la table reclamation avec toutes ses colonnes
CREATE TABLE IF NOT EXISTS reclamation (
    id INT PRIMARY KEY AUTO_INCREMENT,
    type_reclamation_id INT,
    title VARCHAR(255) NOT NULL,
    description LONGTEXT NOT NULL,
    created_at DATETIME NOT NULL,
    fingerprint_verified BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (type_reclamation_id) REFERENCES type_reclamation(id)
);

-- Création de la table réponse
CREATE TABLE IF NOT EXISTS reponse (
    id INT PRIMARY KEY AUTO_INCREMENT,
    reclamation_id INT NOT NULL,
    message LONGTEXT NOT NULL,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (reclamation_id) REFERENCES reclamation(id) ON DELETE CASCADE
);

-- Ajout de la colonne fingerprint_verified si elle n'existe pas
ALTER TABLE reclamation ADD COLUMN IF NOT EXISTS fingerprint_verified BOOLEAN DEFAULT FALSE; 