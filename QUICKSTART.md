# Quick Start Guide

## Run the Application in 3 Steps (Windows)

### Step 1: Open PowerShell
Navigate to the project directory:
```powershell
cd "C:\Users\nguye\OneDrive - Cal Poly Pomona\projects\2025fallhw\5800\Project\main"
```

### Step 2: Set JAVA_HOME and Run
```powershell
# Set Java home (one-time per session)
$env:JAVA_HOME = (Get-ChildItem "C:\Program Files\Java" -Directory | Where-Object {$_.Name -like "jdk*"} | Select-Object -First 1).FullName

# Run the application
.\mvnw.cmd spring-boot:run
# run the tests
.\mvnw test
```

**Note:** First run will download dependencies (takes 2-3 minutes). Subsequent runs are fast!

### Step 3: Open Browser
Go to: **http://localhost:8080**

That's it! 🎉

## First Steps in the App

1. **Register** - Enter username and email, click "Register"
2. **Create a Note** - Click "+ New Note"
3. **Try AI Features**:
   - Write a note and click "Auto-Organize" to get AI tags
   - Write actionable items and click "Generate To-Dos"

## Common Issues

**"JAVA_HOME not found"** → Run the `$env:JAVA_HOME = ...` command from Step 2

**"Java not found"** → Install Java 17+: https://www.oracle.com/java/technologies/downloads/

**"Port 8080 in use"** → Change port in `src/main/resources/application.properties`

**First run taking long?** → Maven is downloading dependencies, this is normal!

## Database Viewer

View your data at: **http://localhost:8080/h2-console**
- JDBC URL: `jdbc:h2:file:./data/notesapp`
- Username: `sa`
- Password: *(leave empty)*

## Stopping the Application

Press `Ctrl+C` in the terminal

---

For detailed documentation, see [README.md](README.md)
