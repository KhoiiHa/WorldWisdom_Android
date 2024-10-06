[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/LJ-RdF5R)
# WorldWisdom

### **“WorldWisdom – Entdecke die Weisheit der Welt in einer App.”**

WorldWisdom ist eine Zitat-App für den Alltag, die inspirierende und weise Zitate von bekannten Persönlichkeiten aus aller Welt präsentiert. 
Mit einer einfachen und intuitiven Oberfläche kannst du Zitate entdecken, speichern und mehr über die Autoren und deren Werke erfahren. 
Entdecke täglich Weisheiten, die deinen Alltag bereichern.

### **Für wen ist die App geeignet?**  
WorldWisdom ist für alle, die Inspiration und Weisheit im Alltag suchen.

### **Welches Problem löst sie?**  
Die App bietet eine einfache Möglichkeit, tiefgründige Zitate zu finden und zu speichern, um Motivation und Reflexion zu fördern.

## Geplantes Design
Füge hier einige repräsentative Designs deiner App ein (z.B. aus Figma)
<p>
  <img src="./img/android_template_app_01.png" width="200">
  <img src="./img/android_template_app_02.png" width="200">
  <img src="./img/android_template_app_03.png" width="200">
</p>

## Features

Hier kommen alle geplanten Features der App rein mit dem Status, ob es bereits umgesetzt wurde.

- [x] **Zitate entdecken**: Benutzer können inspirierende Zitate von verschiedenen bekannten Persönlichkeiten entdecken.
- [x] **Speichern & Sammeln:**: Benutzer können Zitate speichern, die ihnen gefallen, und sie in einem CollectionsScreen verwalten.
- [x] **Zwei Suchleisten**: Benutzer können nach Zitaten basierend auf Autoren oder Schlüsselwörtern suchen.
- [x] **Zwei RecyclerViews**: Die Zitate sind in zwei Kategorien unterteilt, um das Durchstöbern zu erleichtern.
- [x] **Detaillierte Autoreninformationen**: Benutzer können mehr über die Autoren der Zitate erfahren, einschließlich ihrer Biografie und anderer Zitate.
- [x] **Einmalige Registrierung**: Eine einmalige Registrierung ermöglicht es Benutzern, in der App angemeldet zu bleiben.
- [ ] **Bilder hinzufügen**: Integration von Bildern zur visuellen Unterstützung der Zitate.
- [ ] **Community-Funktion**: Zukünftig können Benutzer ihre eigenen Zitate oder Gedichte erstellen und in der App hochladen.

---


## Technischer Aufbau

### Projektaufbau
Das Projekt folgt der MVVM-Architektur (Model-View-ViewModel), die eine klare Trennung von Logik und Benutzeroberfläche ermöglicht. Die Ordnerstruktur umfasst:

- **Model**: Enthält die Datenklassen für Zitate und Autoren.
- **View**: Beinhaltet die Benutzeroberflächen-Layouts und Fragmente.
- **SharedViewModel**: Verwaltet die UI-bezogene Logik für mehrere Fragmente und interagiert mit dem Repository.
- **Repository**: Zentrale Stelle, die Daten aus verschiedenen Quellen (Room, API) abruft und bereitstellt.

### Datenspeicherung
Die App speichert Zitate und Autoreninformationen lokal mit Room, einer SQLite-Datenbank, 
und nutzt Firebase Authentication zur Benutzerverwaltung. Daten werden bei der Registrierung und beim Speichern von Zitaten in der App gespeichert.

### API Calls
Die App verwendet eine MockAPI mit nur einem API-Endpunkt, um Zitate von verschiedenen bekannten Persönlichkeiten abzurufen. 
Die API bietet die notwendigen Daten für die Anzeige in der App und wird durch Retrofit angesprochen.


 
