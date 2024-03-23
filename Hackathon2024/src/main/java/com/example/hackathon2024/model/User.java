package com.example.hackathon2024.model;

import jakarta.persistence.Id;
import lombok.Data;
import jakarta.persistence.Entity;

@Data
@Entity(name = "users")
public class User {

    @Id
    private long id;

    private long chat_id;

    private String name;

    private int msg_numb;

    private int percentage;

}
