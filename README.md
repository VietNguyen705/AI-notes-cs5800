# AI-Enhanced Notes Application

A smart note-taking web application with AI-powered organization, automatic to-do generation, and dark mode interface. Built with Spring Boot backend and modern JavaScript frontend.

## Project Information

- **Course:** CS 5800 - Software Engineering
- **Institution:** Cal Poly Pomona
- **Project:** Full-stack implementation of Object-Oriented Analysis & Design (OOA/OOD)

## Features

- **Create & Edit Notes** - Rich text editor with formatting, colors, and pinning
- **AI Auto-Organize** - Automatic tag suggestions and categorization
- **Task Generation** - Extract actionable to-dos from note content using NLP
- **Search & Filter** - Real-time search with multi-criteria filtering
- **Task Management** - Always-visible sidebar with task tracking and inline editing
- **Dark Mode** - Default dark theme with smooth transitions
- **Reminders** - Scheduled notifications with multiple channels (Email, Push, SMS, In-App)

## Technology Stack

### Backend
- **Java 21** with Spring Boot 3.1.5
- **Spring Data JPA** for database ORM
- **H2 Database** (embedded, no installation required)
- **Maven** for build management

### Frontend
- **HTML5/CSS3/JavaScript** with Vanilla JS
- **Tailwind CSS 3.x** for styling
- **Quill.js 1.3.6** for rich text editing

### AI Integration
- **OpenAI API** (optional) for advanced features
- **Fallback keyword-based logic** works without API key

## Installation & Setup

### Prerequisites
- Java 17 or higher ([Download here](https://www.oracle.com/java/technologies/downloads/))
- Git

### Quick Start

**1. Clone or navigate to project:**
```bash
cd "/path/to/project/main"
```

**2. (Optional) Configure OpenAI API:**
Edit `src/main/resources/application.properties`:
```properties
openai.api.key=YOUR_API_KEY_HERE
```

**3. Run the application:**

**Windows:**
```powershell
.\mvnw.cmd spring-boot:run
```

**Linux/Mac:**
```bash
./mvnw spring-boot:run
```

**4. Open browser:**
```
http://localhost:8000
```

The app will start on port 8000. First run downloads dependencies (2-3 minutes).

## Design Patterns

This project implements two **Gang of Four (GoF)** design patterns:

### 1. Decorator Pattern (Structural)

**Purpose:** Dynamically add AI enrichment features to notes without modifying the Note entity.

**Implementation:**
```
src/main/java/com/notesapp/decorators/
├── NoteEnrichment.java                    # Component interface
├── BaseNoteEnrichment.java                # Concrete component
├── TagEnrichmentDecorator.java            # Adds AI-suggested tags
├── CategoryEnrichmentDecorator.java       # Adds AI categorization
└── SentimentEnrichmentDecorator.java      # Adds sentiment analysis
```

**Usage Example:**
```java
// Stack decorators to enrich a note with multiple AI features
NoteEnrichment tagEnrichment = new TagEnrichmentDecorator(note, aiOrganizer, userId);
Note enrichedNote = tagEnrichment.enrich();

NoteEnrichment categoryEnrichment = new CategoryEnrichmentDecorator(enrichedNote, aiOrganizer, userId);
enrichedNote = categoryEnrichment.enrich();

NoteEnrichment sentimentEnrichment = new SentimentEnrichmentDecorator(enrichedNote);
enrichedNote = sentimentEnrichment.enrich();
```

**Benefits:**
- ✅ Open/Closed Principle - Add new enrichments without modifying existing code
- ✅ Single Responsibility - Each decorator has one specific task
- ✅ Flexibility - Stack decorators in any order or combination

---

### 2. Mediator Pattern (Behavioral)

**Purpose:** Coordinate notification delivery across multiple channels without tight coupling.

**Implementation:**
```
src/main/java/com/notesapp/mediator/
├── NotificationMediator.java              # Mediator coordinator
├── NotificationChannel.java               # Colleague interface
├── EmailNotificationChannel.java          # Email delivery
├── PushNotificationChannel.java           # Push notifications
├── SMSNotificationChannel.java            # SMS delivery
└── InAppNotificationChannel.java          # In-app notifications
```

**Before (Switch Statement):**
```java
switch (reminder.getChannel()) {
    case PUSH: sendPushNotification(message); break;
    case EMAIL: sendEmailNotification(message); break;
    // ... more cases
}
```

**After (Mediator Pattern):**
```java
// NotificationScheduler.java - simplified delivery
mediator.sendNotification(reminder);
```

**Benefits:**
- ✅ Reduced Coupling - Channels don't know about each other
- ✅ Open/Closed Principle - Add new channels without modifying scheduler
- ✅ Single Source of Truth - All routing logic centralized
- ✅ Scalability - Easy to add broadcast and multi-channel features

---

## Additional Resources

- **Database Console:** `http://localhost:8000/h2-console` (JDBC URL: `jdbc:h2:file:./data/notesapp`)
- **API Base:** `http://localhost:8000/api`

---

**Built with Spring Boot & AI** 🚀
