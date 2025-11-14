package com.notesapp.repositories;

import com.notesapp.entities.TodoItem;
import com.notesapp.enums.TaskStatus;
import com.notesapp.enums.Priority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<TodoItem, String> {

    List<TodoItem> findByNoteId(String noteId);

    @Query("SELECT t FROM TodoItem t WHERE t.user.userId = :userId")
    List<TodoItem> findByUserId(@Param("userId") String userId);

    List<TodoItem> findByStatus(TaskStatus status);

    @Query("SELECT t FROM TodoItem t WHERE t.user.userId = :userId AND t.status = :status")
    List<TodoItem> findByUserIdAndStatus(@Param("userId") String userId, @Param("status") TaskStatus status);

    @Query("SELECT t FROM TodoItem t WHERE t.user.userId = :userId AND t.priority = :priority")
    List<TodoItem> findByUserIdAndPriority(@Param("userId") String userId, @Param("priority") Priority priority);

    @Query("SELECT t FROM TodoItem t WHERE t.user.userId = :userId AND " +
           "t.status = :status AND t.priority IN :priorities ORDER BY t.priority DESC, t.dueDate ASC")
    List<TodoItem> findByUserIdAndStatusAndPriorityIn(@Param("userId") String userId,
                                                       @Param("status") TaskStatus status,
                                                       @Param("priorities") List<Priority> priorities);

    @Query("SELECT t FROM TodoItem t WHERE t.status = :status AND " +
           "t.dueDate IS NOT NULL AND t.dueDate <= :dueDate")
    List<TodoItem> findDueTasks(@Param("status") TaskStatus status, @Param("dueDate") LocalDateTime dueDate);

    void deleteByNoteId(String noteId);
}
