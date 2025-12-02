# AI notes cs5800

notes app with ai stuff - auto tags, pulls tasks from your notes, reminders. made for cs5800 at cal poly pomona.

**demo:** https://youtu.be/Mstve0qUzWg

## what it does

- rich text editor (quill.js)
- ai suggests tags & categories (openai api)
- extracts todos from note text
- search and filters
- task sidebar
- dark mode
- reminders via email/push/sms/in-app
- **export notes to pdf** with formatting, images, and metadata
- **rich media support** - upload images and record audio in notes

## tech stack

**backend:** java 21, spring boot 3.1.5, spring data jpa, h2 database, maven

**frontend:** html/css/js, tailwind css, quill.js

**ai:** openai api (optional)

## setup

**1. cd to project folder**

**2. (optional) set up openai key:**
```bash
export OPENAI_API_KEY=your-key
```
or copy `application-local.properties.example` to `application-local.properties` and add your key

**3. run it:**
```bash
./mvnw spring-boot:run          # mac/linux
.\mvnw.cmd spring-boot:run      # windows
```

**4. open browser:** http://localhost:8000

first run takes a bit to download dependencies.

## design patterns

implemented 5 design patterns for the assignment:

### 1. decorator pattern (structural)
adds ai features to notes without changing the Note class. you can stack decorators to add tags, categories, and sentiment analysis.

**location:** `src/main/java/com/notesapp/decorators/`

**classes:**
- `NoteEnrichment` (interface)
- `TagEnrichmentDecorator`
- `CategoryEnrichmentDecorator`
- `SentimentEnrichmentDecorator`

**example:**
```java
NoteEnrichment tagEnrichment = new TagEnrichmentDecorator(note, aiOrganizer, userId);
Note enrichedNote = tagEnrichment.enrich();

NoteEnrichment categoryEnrichment = new CategoryEnrichmentDecorator(enrichedNote, aiOrganizer, userId);
enrichedNote = categoryEnrichment.enrich();
```

### 2. mediator pattern (behavioral)
coordinates notification delivery across multiple channels. instead of each service knowing about every channel, the mediator handles routing.

**location:** `src/main/java/com/notesapp/mediator/`

**classes:**
- `NotificationMediator` (central coordinator)
- `NotificationChannel` (interface)
- `EmailNotificationChannel`
- `PushNotificationChannel`
- `SMSNotificationChannel`
- `InAppNotificationChannel`

**before:**
```java
switch (reminder.getChannel()) {
    case PUSH: sendPushNotification(message); break;
    case EMAIL: sendEmailNotification(message); break;
    // ...
}
```

**after:**
```java
mediator.sendNotification(reminder);
```

### 3. singleton pattern (creational)
ensures only one instance of critical services exists throughout the app lifecycle. implemented using spring's dependency injection container.

**location:** `src/main/java/com/notesapp/services/`

**classes:**
- `AIOrganizer` - single instance for ai-powered organization
- `TaskGenerator` - single instance for task extraction
- `NotificationScheduler` - single instance for scheduled reminders

**implementation:**
spring's `@Service` annotation automatically creates beans as singletons. the spring container manages the lifecycle and ensures only one instance exists per application context.

**example:**
```java
@Service  // Singleton by default in Spring
public class AIOrganizer {
    @Autowired
    private TagRepository tagRepository;
    // Spring ensures only one instance exists
}
```

### 4. observer pattern (behavioral)
notifies multiple observers when notes are created, updated, or deleted. allows loose coupling between note operations and dependent systems.

**location:** `src/main/java/com/notesapp/observers/`

**classes:**
- `NoteObserver` (interface)
- `SearchIndexObserver` - updates search index on note changes
- `NotificationObserver` - sends notifications on note events
- `AnalyticsObserver` - tracks statistics and metrics

**usage:**
observers are auto-registered via spring and notified in NoteController when notes change.

**example:**
```java
// in NoteController
notifyObservers(note, "CREATE");  // all observers get notified
```

### 5. factory pattern (creational)
centralizes object creation for notification channels and todos. encapsulates creation logic and makes it easy to add new types.

**location:** `src/main/java/com/notesapp/factories/`

**classes:**
- `NotificationChannelFactory` - creates notification channels by type
- `TodoFactory` - creates todos with different configurations

**example:**
```java
// create notification channel
NotificationChannelFactory channelFactory = new NotificationChannelFactory();
NotificationChannel channel = channelFactory.createChannel(NotificationChannel.EMAIL);

// create todos with different priorities
TodoFactory todoFactory = new TodoFactory();
TodoItem urgentTodo = todoFactory.createUrgentTodo(user, "Fix critical bug");
TodoItem normalTodo = todoFactory.createTodoWithPriority(user, "Review PR", Priority.MEDIUM);
```

## major features

### feature 1: pdf export
export notes to professional-looking pdf documents with complete formatting preservation.

**capabilities:**
- export single notes with one click
- batch export multiple selected notes
- preserves rich text formatting (bold, italic, lists)
- includes metadata (title, date, category, tags)
- embeds todo items and checklists
- clean, readable layout with proper spacing

**usage:**
1. hover over any note card
2. click the "📄 PDF" button
3. pdf downloads automatically

**api endpoints:**
- `GET /api/notes/{id}/export/pdf` - export single note
- `POST /api/notes/export/pdf` - export multiple notes
- `GET /api/notes/export/all/pdf` - export all user notes

**implementation:**
- uses apache pdfbox library for generation
- custom formatting service handles quill html conversion
- automatic text wrapping and page layout
- caching for optimal performance

### feature 2: rich media support
attach images and audio recordings directly to your notes.

**capabilities:**
- upload images (jpg, png, gif, webp) up to 10mb
- record audio directly in browser
- playback audio recordings
- delete media with one click
- automatic file storage and retrieval
- displays images in grid layout

**usage:**
1. open or create a note
2. click "📷 Image" to upload photos
3. click "🎤 Record" to start/stop audio recording
4. media appears below the note editor
5. hover over media to delete

**api endpoints:**
- `POST /api/media/upload/image` - upload image file
- `POST /api/media/upload/audio` - upload audio recording
- `GET /api/media/images/{filename}` - retrieve image
- `GET /api/media/audio/{filename}` - retrieve audio
- `DELETE /api/media/images/{filename}` - delete image
- `DELETE /api/media/audio/{filename}` - delete audio

**implementation:**
- fileStorageService handles local file storage in `uploads/` directory
- mediaController provides rest api for uploads/downloads
- browser mediarecorder api for audio capture
- responsive image grid with tailwind css
- automatic content-type detection

