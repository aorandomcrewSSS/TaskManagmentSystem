package com.vectoredu.backend.controller;

import com.vectoredu.backend.dto.request.taskRequestDto.TaskToCreate;
import com.vectoredu.backend.dto.request.taskRequestDto.TaskToUpdate;
import com.vectoredu.backend.dto.response.CommentResponse;
import com.vectoredu.backend.dto.response.TaskResponse;
import com.vectoredu.backend.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/tasks")
@RequiredArgsConstructor
@Tag(name = "Управление задачами", description = "Операции по созданию, редактированию и удалению задач")
public class AdminController {
    private final AdminService adminService;

    @Operation(summary = "Создание задачи", responses = {
            @ApiResponse(responseCode = "200", description = "Задача успешно создана"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации входных данных")
    })
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@RequestBody TaskToCreate taskToCreate) {
        return ResponseEntity.ok(adminService.createTask(taskToCreate));
    }

    @Operation(summary = "Получение задачи по ID", responses = {
            @ApiResponse(responseCode = "200", description = "Задача найдена"),
            @ApiResponse(responseCode = "404", description = "Задача не найдена")
    })
    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable Long taskId) {
        return ResponseEntity.ok(adminService.getTaskById(taskId));
    }

    @Operation(summary = "Получение всех задач с пагинацией", responses = {
            @ApiResponse(responseCode = "200", description = "Список задач получен")
    })
    @GetMapping
    public ResponseEntity<Page<TaskResponse>> getAllTasks(@ParameterObject @Parameter(
            description = "Параметры пагинации",
            schema = @Schema(
                    example = "{\"page\": 0, \"size\": 10, \"sort\": [\"createdAt,DESC\"]}"
            )
    ) Pageable pageable) {
        return ResponseEntity.ok(adminService.getAllTasks(pageable));
    }

    @Operation(summary = "Получение задач по email автора", responses = {
            @ApiResponse(responseCode = "200", description = "Список задач получен"),
            @ApiResponse(responseCode = "404", description = "Автор не найден")
    })
    @GetMapping("/author")
    public ResponseEntity<Page<TaskResponse>> getTasksByAuthor(@RequestParam String authorEmail, @ParameterObject @Parameter(
            description = "Параметры пагинации",
            schema = @Schema(
                    example = "{\"page\": 0, \"size\": 10, \"sort\": [\"createdAt,DESC\"]}"
            )
    ) Pageable pageable) {
        return ResponseEntity.ok(adminService.getTasksByAuthor(authorEmail, pageable));
    }

    @Operation(summary = "Получение задач по email исполнителя", responses = {
            @ApiResponse(responseCode = "200", description = "Список задач получен"),
            @ApiResponse(responseCode = "404", description = "Исполнитель не найден")
    })
    @GetMapping("/assignee")
    public ResponseEntity<Page<TaskResponse>> getTasksByAssignee(@RequestParam String assigneeEmail, @ParameterObject @Parameter(
            description = "Параметры пагинации",
            schema = @Schema(
                    example = "{\"page\": 0, \"size\": 10, \"sort\": [\"createdAt,DESC\"]}"
            )
    ) Pageable pageable) {
        return ResponseEntity.ok(adminService.getTasksByAssignee(assigneeEmail, pageable));
    }

    @Operation(summary = "Обновление задачи", responses = {
            @ApiResponse(responseCode = "200", description = "Задача успешно обновлена"),
            @ApiResponse(responseCode = "404", description = "Задача или исполнитель не найдены")
    })
    @PutMapping("/{taskId}")
    public ResponseEntity<TaskResponse> updateTask(@PathVariable Long taskId, @RequestBody TaskToUpdate taskToUpdate) {
        return ResponseEntity.ok(adminService.updateTask(taskId, taskToUpdate));
    }

    @Operation(summary = "Удаление задачи", responses = {
            @ApiResponse(responseCode = "200", description = "Задача успешно удалена"),
            @ApiResponse(responseCode = "404", description = "Задача не найдена")
    })
    @DeleteMapping("/{taskId}")
    public ResponseEntity<TaskResponse> deleteTask(@PathVariable Long taskId) {
        return ResponseEntity.ok(adminService.deleteTask(taskId));
    }

    @Operation(summary = "Добавление комментария к задаче", responses = {
            @ApiResponse(responseCode = "200", description = "Комментарий успешно добавлен"),
            @ApiResponse(responseCode = "404", description = "Задача не найдена")
    })
    @PostMapping("/{taskId}/comments")
    public ResponseEntity<CommentResponse> addCommentToTask(@PathVariable Long taskId, @RequestBody String text) {
        return ResponseEntity.ok(adminService.addCommentToTask(taskId, text));
    }
}
