Nom du Projet : ArtConnect
Slogan : Promouvoir l'art tunisien
Logo du Projet : https://github.com/MohamedalRahali/Java/blob/sadok/src/main/resources/images/logo.png github.com
Objectif Principal : Plateforme de gestion dédiée à la valorisation de l’art tunisien à travers plusieurs modules : utilisateurs, formations, événements, produits , blogs et réclamations.

Acteurs / Utilisateurs Ciblés :

Admin : Gestion complète du contenu et des utilisateurs

Artiste : Création de contenu artistique, gestion de son profil

Utilisateur simple : Consultation, interactions, réclamations

Fonctionnalités
1. Gestion des Utilisateurs
Description : Gestion des comptes utilisateurs avec rôles (admin, artiste, utilisateur).
Sous-fonctionnalités :

Authentification / inscription

Attribution des rôles

Profil utilisateur (modification, suppression)

Vue spécifique selon rôle

2. Gestion des Formations
Description : Les admins ou artistes peuvent proposer des formations.
Sous-fonctionnalités :

Ajout / modification / suppression de formation

Inscription à une formation

Affichage des participants

3. Gestion des Événements
Description : Création et gestion des événements artistiques.
Sous-fonctionnalités :

Calendrier des événements

Création, édition, suppression

Réservation / participation

4. Gestion des Produits
Description : Mise en avant de produits artisanaux tunisiens.
Sous-fonctionnalités :

Catalogue des produits

Ajout / modification / suppression

Système de commande

5. Gestion des Blogs
Description : Partage d’articles autour de l’art et de la culture tunisienne.
Sous-fonctionnalités :

Création d’articles

Commentaires / likes

Filtrage par thématique

6. Gestion des Réclamations
Description : Système de gestion des retours ou problèmes signalés.
Sous-fonctionnalités :

Création de réclamation avec description (et voice-to-text)

Suivi de l’état

Réponse de l’admin

Architecture et Technologies
Technologies utilisées
Côté Web
Langage : PHP

Framework : Symfony 6.4

Frontend : Twig (Symfony), HTML/CSS, Bootstrap

Outils : Composer, GitHub

Côté Desktop
Technologie : JavaFX

Fonction : Interface locale complémentaire à la plateforme web

Base de données
SGBD : MySQL

Sécurité
Authentification avec gestion des rôles

Vérification des autorisations selon rôle

API Externes Utilisées
OpenAI : Pour résumé automatique des réclamations

Google Translate API (optionnel):traduire la page des evenements pour les clients
JavaMailAPI : envoie un mail de confirmation lors de la reservation d'un evenement
AIService: chatbot
JavaMailAPI: envoie d'un mail pour oublier mot de passe
GOOGLEAuth(optionnel): Login avec authentification avec google
RECAPTCHA : utilisateur
VOICE TO TEXT : decrire la reclamation avec l'option VOICE TO TEXT

Structure du projet
Type : Multi-repo (Frontend, Backend, Desktop)

Organisation des dossiers (exemple pour backend Symfony) :


Copier
Modifier
src/
├── Controller/
├── Entity/
├── Repository/
├── Security/
templates/
public/
config/
 Installation
Prérequis :
PHP >= 8.1

Composer

Node.js + npm (si besoin pour outils frontend)

MySQL / MariaDB

Java + Maven (pour la partie JavaFX)

Étapes d’installation :
bash
Copier
Modifier
# Backend Symfony
git clone https://github.com/MohamedalRahali/Java.git
cd Java
composer install
php bin/console doctrine:database:create
php bin/console doctrine:schema:update --force
symfony server:start

# JavaFX
cd src/main/java
mvn clean install
java -jar target/artconnect-desktop.jar

Contact

Email du Projet : support@artconnect.com
Site Web : www.artconnect.com
GitHub : https://github.com/MohamedalRahali/Java


