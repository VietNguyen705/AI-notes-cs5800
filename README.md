# AI-Enhanced Notes Application

A smart note-taking web application with AI-powered organization and automatic to-do generation.

## Project Information

- **Course:** CS 5800 - Software Engineering
- **Institution:** Cal Poly Pomona
- **Project:** Full-stack implementation of Object-Oriented Analysis & Design (OOA/OOD)

## Features

### Core Functionality (Must Have - UC1-UC6)
- **UC1: Create Note** - Create notes with rich text formatting, colors, and metadata
- **UC2: Edit/Delete Note** - Click-to-edit notes with modal interface
- **UC3: Auto-Organize Notes** - AI-powered tag suggestions and categorization
- **UC4: Generate To-Dos** - Extract actionable tasks from note content using NLP
- **UC5: Manage To-Do List** - Always-visible sidebar with quick task management
- **UC6: Search & Filter** - Real-time search with multi-criteria filtering

### Additional Features (Should Have - UC7-UC8)
- **UC7: Tag/Pin/Color Notes** - Visual organization with 5 color options
- **UC8: Reminders & Due Dates** - Scheduled notifications with datetime picker

### Modern UI/UX Features
- **Dark Mode** - Toggle between light and dark themes with localStorage persistence
- **Rich Text Editing** - Bold, italic, lists, headers, code blocks via Quill.js
- **Toast Notifications** - Non-intrusive success/error messages
- **Inline Task Editing** - Click any task to edit details (title, priority, status, due date)
- **Responsive Design** - Fully optimized for desktop, tablet, and mobile
- **Modal-Based Editing** - Clean, focused editing experience
- **Always-Visible Tasks** - Sidebar shows top 5 active tasks at all times

## Technology Stack

### Backend
- **Java 17+** (tested with Java 21)
- **Spring Boot 3.1.5** - Web framework
- **Spring Data JPA** - Database ORM
- **H2 Database** - Embedded file-based database (no installation required)
- **Maven 3.9.5** - Build tool

### Frontend
- **HTML5/CSS3/JavaScript** - Modern web interface
- **Tailwind CSS 3.x** - Utility-first CSS framework
- **Quill.js 1.3.6** - Rich text editor for notes
- **Vanilla JavaScript** - No heavy framework dependencies

### AI Integration
- **OpenAI API** (optional) - For advanced tag suggestions and task extraction
- **Fallback keyword-based logic** - Works without API key

## Prerequisites

- **Java 17 or higher** - [Download here](https://www.oracle.com/java/technologies/downloads/)
- **Git** - For cloning the repository

No database installation required - H2 is embedded!

## Installation & Setup

### 1. Navigate to Project Directory

**Windows PowerShell:**
```powershell
cd "C:\Users\nguye\OneDrive - Cal Poly Pomona\projects\2025fallhw\5800\Project\main"
```

**Linux/Mac/WSL:**
```bash
cd "/mnt/c/Users/nguye/OneDrive - Cal Poly Pomona/projects/2025fallhw/5800/Project/main"
```

### 2. Configure OpenAI API (Optional)

If you want to use real AI features instead of keyword-based logic:

1. Get an API key from [OpenAI](https://platform.openai.com/api-keys)
2. Edit `src/main/resources/application.properties`:
   ```properties
   openai.api.key=YOUR_API_KEY_HERE
   ```

**Note:** The application works perfectly fine without an API key using keyword-based analysis!

### 3. Run the Application

**Windows PowerShell:**
```powershell
# Set JAVA_HOME (one-time per session)
$env:JAVA_HOME = (Get-ChildItem "C:\Program Files\Java" -Directory | Where-Object {$_.Name -like "jdk*"} | Select-Object -First 1).FullName

# Run the application
.\mvnw.cmd spring-boot:run
```

**Linux/Mac:**
```bash
./mvnw spring-boot:run
```

The application will start on **http://localhost:8080**

**Note:** First run downloads dependencies (2-3 minutes). Subsequent runs are fast!

## Usage

### First Time Setup

1. Open your browser and go to **http://localhost:8080**
2. Register a new user:
   - Enter a username
   - Enter an email
   - Click "Register"
3. You're now logged in!

### Creating Notes

1. Click the **"New Note"** button
2. Enter a title using the rich text editor
3. Format your text with bold, italic, lists, etc.
4. Choose a color from the color picker (optional)
5. Click the pin icon to pin the note (optional)
6. Click **"Save Note"**

### Editing Notes

1. Click anywhere on a note card
2. The edit modal will open with the note's content
3. Make your changes using the rich text editor
4. Click **"Save Note"** to update

### AI Features

#### Auto-Organize (AI Tag Suggestions)
1. Create or edit a note
2. Click **"Auto-Organize"** button
3. AI will analyze the content and suggest relevant tags
4. The note will be categorized automatically

#### Generate To-Dos
1. Create a note with actionable items (e.g., "Buy groceries, Call doctor, Finish report")
2. Click **"Generate To-Dos"** button
3. Tasks will be extracted and added to your to-do list
4. View tasks by clicking **"View To-Do List"**

### Managing Tasks

**Quick Task Management (Sidebar):**
- View your top 5 active tasks at all times
- Check/uncheck tasks to mark complete
- Click any task to edit it (title, priority, status, due date)

**Full Task Management (Modal):**
1. Click **"View All Tasks"** in the sidebar
2. Filter tasks by status: All, Pending, In Progress, Completed
3. Click any task to edit details
4. Delete tasks using the delete button

### Searching Notes

- Use the search bar to find notes by title or content
- Click filter buttons (All Notes, Pinned, Work, Personal) to filter by category

## Database Access

The application uses an embedded H2 database that persists data to a file.

### H2 Console (Database Inspector)

1. Open **http://localhost:8080/h2-console**
2. Use these settings:
   - **JDBC URL:** `jdbc:h2:file:./data/notesapp`
   - **Username:** `sa`
   - **Password:** *(leave empty)*
3. Click **"Connect"**

You can now browse tables, run SQL queries, and inspect data.

### Data Persistence

- Data is stored in `./data/notesapp.mv.db`
- Data persists between application restarts
- To reset the database, delete the `data` folder

## API Documentation

### Base URL
```
http://localhost:8080/api
```

### User Endpoints

#### Register User
```http
POST /api/users/register
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john@example.com"
}
```

#### Get User by Username
```http
GET /api/users/username/{username}
```

### Note Endpoints

#### Get All Notes for User
```http
GET /api/notes?userId={userId}
```

#### Create Note
```http
POST /api/notes
Content-Type: application/json

{
  "userId": "user-id-here",
  "title": "My Note",
  "body": "Note content here",
  "color": "#FFFFFF"
}
```

#### Update Note
```http
PUT /api/notes/{noteId}
Content-Type: application/json

{
  "title": "Updated Title",
  "body": "Updated content"
}
```

#### Delete Note
```http
DELETE /api/notes/{noteId}
```

#### Auto-Organize Note
```http
POST /api/notes/{noteId}/auto-organize
```

#### Search Notes
```http
GET /api/notes/search?userId={userId}&query={searchQuery}
```

### To-Do Endpoints

#### Get All Tasks for User
```http
GET /api/todos?userId={userId}
```

#### Create Task
```http
POST /api/todos
Content-Type: application/json

{
  "userId": "user-id-here",
  "title": "Buy groceries",
  "priority": "HIGH"
}
```

#### Generate Tasks from Note
```http
POST /api/todos/generate/{noteId}?userId={userId}
```

#### Complete Task
```http
PUT /api/todos/{taskId}/complete
```

#### Get Tasks by Status
```http
GET /api/todos/status/{status}?userId={userId}
```
Status: `PENDING`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`

## Architecture

### Project Structure

```
main/
├── src/
│   ├── main/
│   │   ├── java/com/notesapp/
│   │   │   ├── entities/          # JPA entities (User, Note, TodoItem, etc.)
│   │   │   ├── enums/             # TaskStatus, Priority, NotificationChannel
│   │   │   ├── repositories/      # Spring Data JPA repositories
│   │   │   ├── services/          # Business logic (AI, Tasks, Search, Notifications)
│   │   │   ├── controllers/       # REST API endpoints
│   │   │   └── NotesApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── static/            # Frontend (HTML/CSS/JS)
│   └── test/                      # Unit tests (to be implemented)
├── .mvn/                          # Maven wrapper configuration
├── mvnw.cmd                       # Maven wrapper for Windows
├── pom.xml                        # Maven build configuration
└── README.md
```

### Design Patterns

- **Repository Pattern** - Data access abstraction
- **Service Layer** - Business logic separation
- **Dependency Injection** - Spring IoC container
- **RESTful API** - Stateless HTTP endpoints

### UML Diagrams

All UML diagrams (Class, Activity, Sequence) are available in:
```
../Project Part 2(Activity, Class and Sequential UML)/
```

## Testing

Unit tests are defined in the testing plan:
```
../Project_3_Testing_Startup/Testing_Plan.md
```

To run tests (when implemented):
```bash
./mvnw test        # Linux/Mac
.\mvnw.cmd test    # Windows
```

## Troubleshooting

### JAVA_HOME not found (Windows)

Set JAVA_HOME in PowerShell:
```powershell
$env:JAVA_HOME = (Get-ChildItem "C:\Program Files\Java" -Directory | Where-Object {$_.Name -like "jdk*"} | Select-Object -First 1).FullName
```

### Port 8080 Already in Use

If port 8080 is occupied, change it in `src/main/resources/application.properties`:
```properties
server.port=8081
```

### Java Version Error

Ensure you have Java 17 or higher:
```bash
java -version
```

### First Run Taking Long

Maven downloads dependencies on first run (2-3 minutes). This is normal and only happens once.

### Database Locked Error

If you see "Database may be already in use", close all H2 console sessions and restart the app.

## UI/UX Highlights

### Dark Mode
- Toggle between light and dark themes using the sun/moon icon
- Preference saved in browser localStorage
- Smooth color transitions throughout the app

### Rich Text Editing
- **Formatting:** Bold, italic, underline, strikethrough
- **Lists:** Ordered and unordered lists
- **Structure:** Headers (H1, H2, H3), blockquotes, code blocks
- **Clean Interface:** Quill.js toolbar with essential formatting options

### Task Management
- **Always Visible:** Top 5 active tasks in sidebar
- **Click to Edit:** Edit any task with full modal interface
- **Priority Badges:** Visual indicators for task importance
- **Status Tracking:** Pending, In Progress, Completed, Cancelled

### Note Organization
- **Click to Edit:** Click anywhere on a note card to open editor
- **Color Coding:** 5 color options for visual organization
- **Pin Notes:** Keep important notes at the top
- **AI Organize:** One-click tag suggestions

## Future Enhancements

- **User Authentication** - Password-based login with JWT
- **Note Sharing** - Collaboration features
- **Cloud Sync** - Cross-device synchronization
- **Mobile App** - iOS/Android clients
- **File Attachments** - Images and documents
- **Voice-to-Text** - Speech recognition
- **Export/Import** - PDF, Markdown, JSON formats

## Project Timeline

- **Project 1:** Object-Oriented Analysis & Design
- **Project 2:** UML Modeling (Activity, Class, Sequence diagrams)
- **Project 3:** Testing Strategy & Test Cases
- **Project 4:** Implementation (this application)

## Contributors

- **Student:** Nguyen (Cal Poly Pomona)
- **Course:** CS 5800 - Software Engineering
- **Semester:** Fall 2025

## License

This is an academic project for educational purposes.

## Support

For questions or issues, refer to:
- UML diagrams in `Project Part 2(Activity, Class and Sequential UML)/`
- Testing plan in `Project_3_Testing_Startup/Testing_Plan.md`
- Course materials and lecture notes

---

**Built with Spring Boot & AI** 🚀
