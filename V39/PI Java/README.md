# Projet Java

Ce projet est une application Java développée avec Maven.

## Prérequis

- Java 11 ou supérieur
- Maven 3.6 ou supérieur

## Structure du projet

```
src/
├── main/
│   ├── java/        # Code source Java
│   └── resources/   # Fichiers de ressources
└── test/
    ├── java/        # Tests unitaires
    └── resources/   # Ressources pour les tests
```

## Comment compiler le projet

```bash
mvn clean install
```

## Comment exécuter le projet

```bash
mvn exec:java -Dexec.mainClass="com.example.Main"
``` 