
# Edutech – Plateforme e-learning interactive

Ce projet a été réalisé dans le cadre du module **PIDEV 3A** à **Esprit School of Engineering**.  
Edutech est une plateforme d'apprentissage numérique qui facilite la communication et la gestion des ressources pédagogiques entre étudiants, tuteurs et administrateurs.

---

## 📚 Sommaire

- [🎯 Description du projet](#description-du-projet)
- [✨ Fonctionnalités](#fonctionnalités)
- [🛠️ Technologies utilisées](#technologies-utilisées)
- [📁 Structure du projet](#structure-du-projet)
- [🚀 Installation](#installation)
- [💡 Utilisation](#utilisation)
- [🤝 Contribution](#contribution)
- [📄 Licence](#licence)
- [🙏 Remerciements](#remerciements)

---

## 🎯 Description du projet

Edutech est une plateforme **e-learning** complète qui couvre plusieurs aspects essentiels de la gestion pédagogique moderne :

- Favoriser l’apprentissage autonome et assisté.
- Mettre en relation tuteurs et étudiants via des sessions planifiées.
- Gérer les abonnements, les certifications et les ressources éducatives.
- Offrir une expérience utilisateur fluide et intuitive à travers une application desktop en **JavaFX** et une API backend en **Symfony**.

---

## ✨ Fonctionnalités

- 👤 **Gestion des utilisateurs** : inscription, connexion, rôles (étudiant, tuteur, administrateur).
- 📅 **Réservation de meet** : prise de rendez-vous avec les tuteurs.
- 📰 **Feed d’actualité** : publication d’articles, commentaires, notifications.
- 📚 **Gestion des cours** : accès aux contenus, progression, certificats.
- 🎓 **Certifications & Quiz** : création de quiz, génération de certificats.
- 🛒 **Abonnements & Panier** :
  - Ajout de certifications au panier.
  - Paiement sécurisé via Stripe.
  - Gestion de la **wishlist**.
- 📆 **Événements pédagogiques** : création, affichage et filtrage par nom.

---

## 🛠️ Technologies utilisées

### Backend
- Symfony (PHP)
- API RESTful

### Frontend
- JavaFX (interface utilisateur)

### Base de données
- MySQL

### Paiement
- Stripe API

### Autres outils
- Git & GitHub (gestion de version)
- Postman (tests API)

---

## 📁 Structure du projet

```
/backend
  ├── src/
  ├── controllers/
  ├── services/
  └── config/
  
/frontend
  ├── controllers/
  ├── views/
  ├── models/
  └── PanierView.fxml
  
README.md
```

---

## 🚀 Installation

### Backend Symfony

1. Cloner le dépôt :
```bash
git clone https://github.com/votre-utilisateur/edutech.git
cd edutech/backend
```

2. Installer les dépendances :
```bash
composer install
```

3. Configurer `.env` avec vos identifiants de base de données.

4. Lancer le serveur Symfony :
```bash
symfony server:start
```

### Frontend JavaFX

1. Ouvrir le dossier `frontend` dans un IDE compatible Java (IntelliJ, NetBeans, Eclipse).
2. Compiler et exécuter l’application.
3. S’assurer que l’API backend est en marche pour l’interaction.

---

## 💡 Utilisation

- Les étudiants peuvent s'inscrire, consulter des cours, réserver des meets, passer des quiz, obtenir des certificats et gérer leur panier.
- Les tuteurs peuvent publier des cours, créer des quiz, voir les réservations et interagir via le feed.
- L’administrateur supervise tout le système et peut créer des abonnements ou gérer les événements.

---

## 🤝 Contribution

Les contributions sont les bienvenues !  
Veuillez suivre les étapes suivantes pour proposer des modifications :

1. Fork le projet.
2. Créez une branche : `git checkout -b feature/nom-feature`.
3. Commitez vos modifications : `git commit -m "Ajout de la fonctionnalité X"`.
4. Pushez la branche : `git push origin feature/nom-feature`.
5. Ouvrez une Pull Request.

---

## 📄 Licence

Ce projet est sous licence MIT.  
Vous êtes libre de l’utiliser, le modifier et le redistribuer à condition de conserver la notice de licence.

---

## 🙏 Remerciements

Ce projet a été encadré par [Nom de l’encadrant] dans le cadre du module **PIDEV 3A** à **Esprit School of Engineering**.

---
