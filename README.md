# AI Notes

Notes app with AI-powered tagging, task extraction, and reminders. Built for CS5800 at Cal Poly Pomona.

**Demo:** https://youtu.be/Mstve0qUzWg

## Features

- Rich text editor (Quill.js)
- AI-suggested tags & categories (OpenAI API)
- Auto-extract todos from note text
- PDF export with formatting
- Image uploads & voice recordings
- Multi-channel reminders (email/push/SMS/in-app)
- Search, filters, dark mode

## Tech Stack

- **Backend:** Java 21, Spring Boot 3.1.5, H2 Database
- **Frontend:** HTML/CSS/JS, Tailwind CSS, Quill.js
- **AI:** OpenAI API (optional)

## Setup

```bash
# 1. Run the app
./mvnw spring-boot:run        # mac/linux
.\mvnw.cmd spring-boot:run    # windows

# 2. Open browser
http://localhost:8000

# 3. (Optional) Set OpenAI key
export OPENAI_API_KEY=your-key
```

---

## CS5800 Project Parts

### Part 1: Design Patterns (5 total)

| Pattern | Purpose | Location |
|---------|---------|----------|
| Decorator | Enrich notes with AI metadata (tags, categories, sentiment) | `decorators/` |
| Mediator | Coordinate notification channels | `mediator/` |
| Singleton | Single instance services | `services/` |
| Observer | Notify on note changes | `observers/` |
| Factory | Create channels and todos | `factories/` |

### Part 2: Major Features

1. **PDF Export** - Export notes with formatting, todos, metadata (`PDFExportService.java`)
2. **Rich Media** - Upload images, record audio (`FileStorageService.java`, `MediaController.java`)

### Part 3: Clean Code

- Extracted `OpenAIService` (eliminated 200 lines duplication)
- Created `AppConstants` (47 magic numbers centralized)
- All methods ≤20 lines, proper logging, full Javadoc
