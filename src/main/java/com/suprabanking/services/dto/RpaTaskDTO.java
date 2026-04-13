package com.suprabanking.services.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RpaTaskDTO {

    private Long id;
    private String nomTache;
    private String type;
    private String statut;
    private String payload;
    private String resultMessage;
    private String lastError;
    private Integer retryCount;

    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private LocalDateTime dateExecution;

    private Long userId;
    private String username;
}
