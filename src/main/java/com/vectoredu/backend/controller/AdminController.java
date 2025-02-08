package com.vectoredu.backend.controller;

import com.vectoredu.backend.dto.request.TaskToCreate;
import com.vectoredu.backend.dto.response.TaskResponse;
import com.vectoredu.backend.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@RequestBody TaskToCreate taskToCreate) {
        TaskResponse taskResponse = adminService.createTask(taskToCreate);
        return ResponseEntity.ok(taskResponse);
    }
}
