package com.notesapp.repositories;

import com.notesapp.entities.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, String> {

    @Query("SELECT r FROM Reminder r WHERE r.scheduledTime <= :time AND r.isDelivered = false")
    List<Reminder> findPendingReminders(@Param("time") LocalDateTime time);

    List<Reminder> findByIsDelivered(Boolean isDelivered);

    List<Reminder> findByEntityId(String entityId);
}
