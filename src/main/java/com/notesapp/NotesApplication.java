package com.notesapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NotesApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotesApplication.class, args);
        System.out.println("\n===========================================");
        System.out.println("AI-Enhanced Notes Application Started!");
        System.out.println("===========================================");
        System.out.println("Application URL: http://localhost:8000");
        System.out.println("H2 Console: http://localhost:8000/h2-console");
        System.out.println("API Documentation: http://localhost:8000/api");
        System.out.println("===========================================\n");
    }
}
