package com.notesapp.controllers;

import com.notesapp.entities.Note;
import com.notesapp.entities.TodoItem;
import com.notesapp.entities.User;
import com.notesapp.entities.Reminder;
import com.notesapp.enums.Priority;
import com.notesapp.enums.TaskStatus;
import com.notesapp.enums.NotificationChannel;
import com.notesapp.repositories.NoteRepository;
import com.notesapp.repositories.TaskRepository;
import com.notesapp.repositories.UserRepository;
import com.notesapp.services.TaskGenerator;
import com.notesapp.services.NotificationScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/todos")
@CrossOrigin(origins = "*")
public class TodoController {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private TaskGenerator taskGenerator;

    @Autowired
    private NotificationScheduler notificationScheduler;

    @GetMapping
    public ResponseEntity<List<TodoItem>> getAllTasks(@RequestParam String userId) {
        List<TodoItem> tasks = taskRepository.findByUserId(userId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TodoItem> getTaskById(@PathVariable String id) {
        return taskRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<TodoItem> createTask(@RequestBody Map<String, Object> taskData) {
        try {
            String userId = (String) taskData.get("userId");
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

            TodoItem task = new TodoItem();
            task.setUser(user);
            task.setTitle((String) taskData.get("title"));
            task.setDescription((String) taskData.getOrDefault("description", ""));
            task.setStatus(TaskStatus.PENDING);

            if (taskData.containsKey("priority")) {
                task.setPriority(Priority.valueOf((String) taskData.get("priority")));
            }

            if (taskData.containsKey("noteId")) {
                task.setNoteId((String) taskData.get("noteId"));
            }

            TodoItem savedTask = taskRepository.save(task);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedTask);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/generate/{noteId}")
    public ResponseEntity<List<TodoItem>> generateTasksFromNote(@PathVariable String noteId, @RequestParam String userId) {
        try {
            Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("Note not found"));

            User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

            List<TodoItem> tasks = taskGenerator.generateTasks(note, user);
            return ResponseEntity.status(HttpStatus.CREATED).body(tasks);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<TodoItem> updateTask(@PathVariable String id, @RequestBody Map<String, Object> updates) {
        return taskRepository.findById(id)
            .map(task -> {
                if (updates.containsKey("title")) {
                    String title = (String) updates.get("title");
                    LocalDateTime dueDate = task.getDueDate();
                    Priority priority = task.getPriority();

                    if (updates.containsKey("dueDate") && updates.get("dueDate") != null) {
                        dueDate = LocalDateTime.parse((String) updates.get("dueDate"));
                    }

                    if (updates.containsKey("priority")) {
                        priority = Priority.valueOf((String) updates.get("priority"));
                    }

                    task.update(title, dueDate, priority);
                }

                if (updates.containsKey("status")) {
                    TaskStatus status = TaskStatus.valueOf((String) updates.get("status"));
                    task.updateStatus(status);
                }

                TodoItem updated = taskRepository.save(task);
                return ResponseEntity.ok(updated);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<TodoItem> completeTask(@PathVariable String id) {
        return taskRepository.findById(id)
            .map(task -> {
                task.complete();
                TodoItem updated = taskRepository.save(task);
                return ResponseEntity.ok(updated);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable String id) {
        return taskRepository.findById(id)
            .map(task -> {
                task.delete();
                taskRepository.delete(task);
                return ResponseEntity.ok().<Void>build();
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<TodoItem>> getTasksByStatus(@RequestParam String userId, @PathVariable String status) {
        TaskStatus taskStatus = TaskStatus.valueOf(status.toUpperCase());
        List<TodoItem> tasks = taskRepository.findByUserIdAndStatus(userId, taskStatus);
        return ResponseEntity.ok(tasks);
    }

    @PostMapping("/{id}/reminder")
    public ResponseEntity<TodoItem> setReminder(@PathVariable String id, @RequestBody Map<String, Object> reminderData) {
        return taskRepository.findById(id)
            .map(task -> {
                Reminder reminder = new Reminder();
                reminder.setEntityId(task.getTaskId());
                reminder.setEntityType("TASK");
                reminder.setScheduledTime(LocalDateTime.parse((String) reminderData.get("scheduledTime")));
                reminder.setChannel(NotificationChannel.valueOf((String) reminderData.getOrDefault("channel", "IN_APP")));
                reminder.setMessage((String) reminderData.getOrDefault("message", "Task reminder: " + task.getTitle()));

                task.setReminder(reminder);
                notificationScheduler.scheduleReminder(reminder);

                TodoItem updated = taskRepository.save(task);
                return ResponseEntity.ok(updated);
            })
            .orElse(ResponseEntity.notFound().build());
    }
}
