# AI notes cs5800

notes app with ai stuff - auto tags, pulls tasks from your notes, reminders. made for cs5800 at cal poly pomona.

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

implemented 2 patterns for the assignment:

### decorator pattern
adds ai features to notes without changing the Note class. you can stack decorators to add tags, categories, and sentiment analysis.

files in `src/main/java/com/notesapp/decorators/`

example:
```java
NoteEnrichment tagEnrichment = new TagEnrichmentDecorator(note, aiOrganizer, userId);
Note enrichedNote = tagEnrichment.enrich();

NoteEnrichment categoryEnrichment = new CategoryEnrichmentDecorator(enrichedNote, aiOrganizer, userId);
enrichedNote = categoryEnrichment.enrich();
```

### mediator pattern
handles notifications across email/push/sms/in-app. instead of a giant switch statement, the mediator coordinates everything.

files in `src/main/java/com/notesapp/mediator/`

before:
```java
switch (reminder.getChannel()) {
    case PUSH: sendPushNotification(message); break;
    case EMAIL: sendEmailNotification(message); break;
    // ...
}
```

after:
```java
mediator.sendNotification(reminder);
```

