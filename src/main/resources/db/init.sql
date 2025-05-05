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

-- Ajout de la colonne fingerprint_verified si elle n'existe pas
ALTER TABLE reclamation ADD COLUMN IF NOT EXISTS fingerprint_verified BOOLEAN DEFAULT FALSE; 