package com.vectoredu.backend.controller;

import com.vectoredu.backend.dto.response.CommentResponse;
import com.vectoredu.backend.dto.response.TaskResponse;
import com.vectoredu.backend.model.enums.Status;
import com.vectoredu.backend.service.UserTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/user/tasks")
@RequiredArgsConstructor
@Tag(name = "Задачи пользователя", description = "Операции по изменению задач пользователем")
public class UserTaskController {
    private final UserTaskService userTaskService;

    @Operation(summary = "Обновление статуса задачи", responses = {
            @ApiResponse(responseCode = "200", description = "Статус задачи успешно обновлен"),
            @ApiResponse(responseCode = "403", description = "Пользователь не может изменить статус задачи")
    })
    @PutMapping("/{taskId}/status")
    public ResponseEntity<TaskResponse> updateTaskStatus(@PathVariable Long taskId, @RequestParam Status newStatus) {
        return ResponseEntity.ok(userTaskService.updateTaskStatus(taskId, newStatus));
    }

    @Operation(summary = "Добавление комментария к задаче", responses = {
            @ApiResponse(responseCode = "200", description = "Комментарий успешно добавлен"),
            @ApiResponse(responseCode = "403", description = "Пользователь не может добавить комментарий к задаче")
    })
    @PostMapping("/{taskId}/comments")
    public ResponseEntity<CommentResponse> addComment(@PathVariable Long taskId, @RequestBody String commentText) {
        return ResponseEntity.ok(userTaskService.addComment(taskId, commentText));
    }

    @Operation(summary = "Получение всех задач текущего пользователя с пагинацией", responses = {
            @ApiResponse(responseCode = "200", description = "Список задач получен")
    })
    @GetMapping
    public ResponseEntity<Page<TaskResponse>> getAllTasksForUser(@ParameterObject @Parameter(
            description = "Параметры пагинации",
            schema = @Schema(
                    example = "{\"page\": 0, \"size\": 10, \"sort\": [\"createdAt,DESC\"]}"
            )
    ) Pageable pageable) {
        Page<TaskResponse> taskResponses = userTaskService.getAllTasksForUser(pageable);
        return ResponseEntity.ok(taskResponses);
    }
}
