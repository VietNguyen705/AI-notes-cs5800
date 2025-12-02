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
ensures only one instance of critical services exists throughout the app lifecycle. provides global access point while preventing duplicate instances.

**location:** `src/main/java/com/notesapp/services/`

**classes:**
- `AIOrganizer` - single instance for ai-powered organization
- `TaskGenerator` - single instance for task extraction
- `NotificationScheduler` - single instance for scheduled reminders

**example:**
```java
AIOrganizer organizer = AIOrganizer.getInstance();
TaskGenerator generator = TaskGenerator.getInstance();
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

