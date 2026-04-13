package com.suprabanking.services;

import com.suprabanking.services.dto.CreateRpaTaskRequest;
import com.suprabanking.services.dto.RpaTaskDTO;

import java.util.List;

public interface RpaTaskService {

    RpaTaskDTO createMyTask(CreateRpaTaskRequest request);

    List<RpaTaskDTO> getMyTasks();

    void cancelMyTask(Long taskId);

    List<RpaTaskDTO> getPendingTasks();

    List<RpaTaskDTO> getAllTasks();

    RpaTaskDTO executeTaskNow(Long taskId);

    int executePendingTasksBatch();
}
