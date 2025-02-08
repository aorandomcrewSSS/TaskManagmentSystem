package com.vectoredu.backend.service;

import com.vectoredu.backend.dto.response.CommentResponse;
import com.vectoredu.backend.dto.response.TaskResponse;
import com.vectoredu.backend.model.Comment;
import com.vectoredu.backend.model.Task;
import com.vectoredu.backend.model.User;
import com.vectoredu.backend.model.enums.Status;
import com.vectoredu.backend.repository.CommentRepository;
import com.vectoredu.backend.repository.TaskRepository;
import com.vectoredu.backend.repository.UserRepository;
import com.vectoredu.backend.util.exception.NotFoundException;
import com.vectoredu.backend.util.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserTaskService {
    private final TaskRepository taskRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    // Метод для изменения статуса задачи
    public TaskResponse updateTaskStatus(Long taskId, Status newStatus) {
        Task task = getTask(taskId);
        User authenticatedUser = getAuthenticatedUser();

        if (!task.getAssignee().equals(authenticatedUser)) {
            throw new UnauthorizedException("Вы не можете изменять задачу, если не являетесь ее исполнителем.");
        }

        task.setStatus(newStatus);
        taskRepository.save(task);

        return mapToTaskResponse(task);
    }

    public Page<TaskResponse> getAllTasksForUser(Pageable pageable) {
        User authenticatedUser = getAuthenticatedUser();
        Page<Task> tasks = taskRepository.findByAssignee(authenticatedUser, pageable);
        return tasks.map(this::mapToTaskResponse);
    }

    // Метод для добавления комментария к задаче
    public CommentResponse addComment(Long taskId, String commentText) {
        Task task = getTask(taskId);
        User authenticatedUser = getAuthenticatedUser();

        if (!task.getAssignee().equals(authenticatedUser)) {
            throw new UnauthorizedException("Вы не можете добавлять комментарии к задаче, если не являетесь ее исполнителем.");
        }

        Comment comment = new Comment();
        comment.setText(commentText);
        comment.setTask(task);
        comment.setAuthor(authenticatedUser);
        commentRepository.save(comment);

        return mapToCommentResponse(comment);
    }

    // Получение задачи по ID
    private Task getTask(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Задача не найдена"));
    }

    // Получение текущего аутентифицированного пользователя
    private User getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }

    // Преобразование задачи в ответ
    private TaskResponse mapToTaskResponse(Task task) {
        return TaskResponse.builder()
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .assigneeFirstName(task.getAssignee().getFirstName())
                .assigneeLastName(task.getAssignee().getLastName())
                .comments(task.getComments().stream().map(this::mapToCommentResponse).toList())
                .build();
    }

    // Преобразование комментария в ответ
    private CommentResponse mapToCommentResponse(Comment comment) {
        return CommentResponse.builder()
                .text(comment.getText())
                .build();
    }
}
