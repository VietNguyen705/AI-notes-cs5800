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
export OPENAI_API_KEY=your-key or copy `application-local.properties.example` to `application-local.properties` and add your key

**3. run it:**
use ./mvnw spring-boot:run for mac/linux or .\mvnw.cmd spring-boot:run for windows

**4. open browser:** http://localhost:8000

first run takes a bit to download dependencies.

---

# project parts (cs5800 assignment)

## part 1 - design patterns (5 required)

started with 2 patterns from my original project (decorator and mediator), needed to add 3 more to hit the requirement.

**what i added:**
- singleton - used spring's @Service which gives you singletons for free (AIOrganizer, TaskGenerator, NotificationScheduler)
- observer - notes notify observers when stuff changes (SearchIndexObserver, NotificationObserver, AnalyticsObserver)  
- factory - centralized creation for notification channels and todos (NotificationChannelFactory, TodoFactory)

so now i have 5 total patterns which meets the requirement.

## design patterns breakdown

here's how each pattern works in the app:

### 1. decorator pattern (structural)
adds ai features to notes without changing the Note class. you can stack decorators to add tags, categories, and sentiment analysis.

**location:** `src/main/java/com/notesapp/decorators/`

**classes:**
- `NoteEnrichment` (interface)
- `TagEnrichmentDecorator`
- `CategoryEnrichmentDecorator`
- `SentimentEnrichmentDecorator`

decorators are chained together to add tags, then categories, then sentiment analysis to notes.

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


### 3. singleton pattern (creational)
make sures only one instance of critical services exists throughout the app lifecycle. added using spring's dependency injection container.

**location:** `src/main/java/com/notesapp/services/`

**classes:**
- `AIOrganizer` - single instance for ai-powered organization
- `TaskGenerator` - single instance for task extraction
- `NotificationScheduler` - single instance for scheduled reminders

**addation:**
spring's `@Service` annotation automatically creates beans as singletons. the spring container manages the lifecycle and make sures only one instance exists per application context.


### 4. observer pattern (behavioral)
notifies multiple observers when notes are created, updated, or deleted. keeps things loosely coupled between note operations and dependent systems.

**location:** `src/main/java/com/notesapp/observers/`

**classes:**
- `NoteObserver` (interface)
- `SearchIndexObserver` - updates search index on note changes
- `NotificationObserver` - sends notifications on note events
- `AnalyticsObserver` - tracks statistics and metrics

**usage:**
observers are auto-registered via spring and notified in NoteController when notes change.


### 5. factory pattern (creational)
centralizes object creation for notification channels and todos. encapsulates creation logic and makes it easy to add new types.

**location:** `src/main/java/com/notesapp/factories/`

**classes:**
- `NotificationChannelFactory` - creates notification channels by type
- `TodoFactory` - creates todos with different configurations


---

## part 2 - two major features

added pdf export and rich media support to the app.

**before:** just had basic notes with ai tags and task extraction. couldn't export anything or attach files.

**what i built:**

**1. pdf export**
- click a button on any note card and it downloads as a formatted pdf
- can export multiple notes at once too
- includes all the metadata, todos, checklists
- used apache pdfbox library for this

**2. rich media support**  
- upload images (jpg/png/gif/webp, max 10mb)
- record audio right in the browser using the MediaRecorder api
- files get stored in an uploads/ folder
- images show up in the note cards and editor

### features detail

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
2. click the " PDF" button
3. pdf downloads automatically

**api endpoints:**
- `GET /api/notes/{id}/export/pdf` - export single note
- `POST /api/notes/export/pdf` - export multiple notes
- `GET /api/notes/export/all/pdf` - export all user notes

**addation:**
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
2. click " Image" to upload photos
3. click " Record" to start/stop audio recording
4. media appears below the note editor
5. hover over media to delete

**api endpoints:**
- `POST /api/media/upload/image` - upload image file
- `POST /api/media/upload/audio` - upload audio recording
- `GET /api/media/images/{filename}` - retrieve image
- `GET /api/media/audio/{filename}` - retrieve audio
- `DELETE /api/media/images/{filename}` - delete image
- `DELETE /api/media/audio/{filename}` - delete audio

**addation:**
- fileStorageService handles local file storage in `uploads/` directory
- mediaController provides rest api for uploads/downloads
- browser mediarecorder api for audio capture
- responsive image grid with tailwind css
- automatic content-type detection

---

## part 3 - clean code refactoring

went through and cleaned up all the code following the clean code book and google's java style guide.

**main issues i fixed:**
- had like 200 lines of duplicate code (same openai api calls in multiple places)
- 47 magic numbers everywhere (file size limits, pdf dimensions, etc)
- path traversal security bug in file upload
- was using System.out.println instead of proper logging
- barely any javadoc

**what i did:**
- extracted duplicate openai code into OpenAIService (saved 90 lines)
- created AppConstants class for all the magic numbers
- fixed the security vulnerability with proper path validation
- added @Slf4j logging everywhere
- wrote javadoc for pretty much everything (went from 20% to 95%)
- split up long methods so nothing's over 20 lines

created 2 new files (OpenAIService.java and AppConstants.java) and refactored 7 others.

### clean code principles applied

 **meaningful names** - no abbreviations, intention-revealing names
 **small functions** - all methods ≤20 lines
 **single responsibility** - each class/method does one thing
 **dry (don't repeat yourself)** - eliminated ~200 lines of duplication
 **error handling** - proper exceptions with context
 **no magic numbers** - all constants extracted
 **javadoc** - all public apis documented
 **logging** - slf4j throughout, no system.out
 **security** - input validation, path traversal prevention

### google java style guide compliance

 **formatting** - 2-space indentation, consistent bracing
 **imports** - organized by category
 **naming** - camelCase methods, PascalCase classes
 **javadoc** - standard format with @param, @return, @throws
 **line length** - kept under 100 characters where practical


