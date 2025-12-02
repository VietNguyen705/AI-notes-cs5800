package com.notesapp.factories;

import com.notesapp.entities.TodoItem;
import com.notesapp.entities.User;
import com.notesapp.enums.Priority;
import com.notesapp.enums.TaskStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Factory pattern implementation for creating TodoItem instances.
 * Provides convenient methods to create todos with different configurations.
 */
@Component
public class TodoFactory {

    /**
     * Creates a basic todo with default settings (PENDING status, MEDIUM priority).
     *
     * @param user The user who owns the todo
     * @param title The title of the todo
     * @return A new TodoItem instance
     */
    public TodoItem createTodo(User user, String title) {
        TodoItem todo = new TodoItem();
        todo.setUser(user);
        todo.setTitle(title);
        todo.setStatus(TaskStatus.PENDING);
        todo.setPriority(Priority.MEDIUM);
        todo.setDescription("");
        return todo;
    }

    /**
     * Creates a todo with specified priority.
     *
     * @param user The user who owns the todo
     * @param title The title of the todo
     * @param priority The priority level
     * @return A new TodoItem instance
     */
    public TodoItem createTodoWithPriority(User user, String title, Priority priority) {
        TodoItem todo = createTodo(user, title);
        todo.setPriority(priority);
        return todo;
    }

    /**
     * Creates a todo with a due date.
     *
     * @param user The user who owns the todo
     * @param title The title of the todo
     * @param dueDate The due date for the todo
     * @return A new TodoItem instance
     */
    public TodoItem createTodoWithDueDate(User user, String title, LocalDateTime dueDate) {
        TodoItem todo = createTodo(user, title);
        todo.setDueDate(dueDate);
        return todo;
    }

    /**
     * Creates an urgent todo (high priority, due today).
     *
     * @param user The user who owns the todo
     * @param title The title of the todo
     * @return A new TodoItem instance with URGENT priority and today's due date
     */
    public TodoItem createUrgentTodo(User user, String title) {
        TodoItem todo = createTodo(user, title);
        todo.setPriority(Priority.URGENT);
        todo.setDueDate(LocalDateTime.now().withHour(23).withMinute(59));
        return todo;
    }

    /**
     * Creates a fully configured todo with all parameters.
     *
     * @param user The user who owns the todo
     * @param title The title of the todo
     * @param description The description
     * @param priority The priority level
     * @param status The task status
     * @param dueDate The due date
     * @param noteId The associated note ID
     * @return A new TodoItem instance
     */
    public TodoItem createFullTodo(User user, String title, String description,
                                   Priority priority, TaskStatus status,
                                   LocalDateTime dueDate, String noteId) {
        TodoItem todo = new TodoItem();
        todo.setUser(user);
        todo.setTitle(title);
        todo.setDescription(description != null ? description : "");
        todo.setPriority(priority != null ? priority : Priority.MEDIUM);
        todo.setStatus(status != null ? status : TaskStatus.PENDING);
        todo.setDueDate(dueDate);
        todo.setNoteId(noteId);
        return todo;
    }

    /**
     * Creates a todo from a note ID.
     *
     * @param user The user who owns the todo
     * @param title The title of the todo
     * @param noteId The ID of the associated note
     * @return A new TodoItem instance
     */
    public TodoItem createTodoFromNote(User user, String title, String noteId) {
        TodoItem todo = createTodo(user, title);
        todo.setNoteId(noteId);
        return todo;
    }
}
