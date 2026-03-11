package com.myprojects.lovable_clone.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UsageLog {
    Long id;

    User user;
    Project project;

    String action;
    Integer tokenUsed;
    Integer durationMs;
    String metadata; //JSON of {model_used, prompt_used}.
    Instant createdAt;

}
