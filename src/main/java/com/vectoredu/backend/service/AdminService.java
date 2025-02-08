package com.vectoredu.backend.service;

import com.vectoredu.backend.dto.request.taskRequestDto.TaskToCreate;
import com.vectoredu.backend.dto.request.taskRequestDto.TaskToUpdate;
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
import com.vectoredu.backend.util.exception.ValidationException;
import com.vectoredu.backend.util.validators.InputValidator;
import com.vectoredu.backend.util.validators.EmailValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional
@PreAuthorize("hasRole('ADMIN')")
public class AdminService {
    private final TaskRepository taskRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EmailValidator emailValidator;
    private final InputValidator inputValidator;

    public TaskResponse createTask(TaskToCreate taskToCreate) {
        validateTaskInput(taskToCreate);
        User author = getAuthenticatedUser();
        User assignee = getUserByEmail(taskToCreate.getAssigneeEmail(), "Исполнитель не найден");

        Task task = buildTask(taskToCreate, author, assignee);
        taskRepository.save(task);

        return mapToTaskResponse(task);
    }

    public TaskResponse getTaskById(Long taskId) {
        return mapToTaskResponse(getTask(taskId));
    }

    public Page<TaskResponse> getAllTasks(Pageable pageable) {
        return taskRepository.findAll(pageable).map(this::mapToTaskResponse);
    }

    public Page<TaskResponse> getTasksByAuthor(String authorEmail, Pageable pageable) {
        User author = getUserByEmail(authorEmail, "Автор не найден");
        return taskRepository.findByAuthor(author, pageable).map(this::mapToTaskResponse);
    }

    public Page<TaskResponse> getTasksByAssignee(String assigneeEmail, Pageable pageable) {
        User assignee = getUserByEmail(assigneeEmail, "Исполнитель не найден");
        return taskRepository.findByAssignee(assignee, pageable).map(this::mapToTaskResponse);
    }

    public TaskResponse updateTask(Long taskId, TaskToUpdate taskToUpdate) {
        Task task = getTask(taskId);
        User author = getAuthenticatedUser();
        task.setAuthor(author);

        updateTaskFields(task, taskToUpdate);
        taskRepository.save(task);

        return mapToTaskResponse(task);
    }

    public TaskResponse deleteTask(Long taskId) {
        Task task = getTask(taskId);
        commentRepository.deleteByTask(task);
        taskRepository.deleteById(taskId);
        return mapToTaskResponse(task);
    }

    public CommentResponse addCommentToTask(Long taskId, String text) {
        validateCommentInput(text);
        Task task = getTask(taskId);
        User author = getAuthenticatedUser();

        Comment comment = Comment.builder()
                .task(task)
                .author(author)
                .text(text)
                .build();

        commentRepository.save(comment);
        return mapToCommentResponse(comment);
    }

    private User getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return getUserByEmail(email, "Пользователь не найден");
    }

    private User getUserByEmail(String email, String errorMessage) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(errorMessage));
    }

    private Task getTask(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Задача не найдена"));
    }

    private Task buildTask(TaskToCreate taskToCreate, User author, User assignee) {
        return Task.builder()
                .title(taskToCreate.getTitle())
                .description(taskToCreate.getDescription())
                .author(author)
                .assignee(assignee)
                .status(Status.PENDING)
                .priority(taskToCreate.getPriority())
                .build();
    }

    private void updateTaskFields(Task task, TaskToUpdate taskToUpdate) {
        if (isValidString(taskToUpdate.getTitle())) {
            validateTitle(taskToUpdate.getTitle());
            task.setTitle(taskToUpdate.getTitle());
        }
        if (isValidString(taskToUpdate.getDescription())) {
            validateDescription(taskToUpdate.getDescription());
            task.setDescription(taskToUpdate.getDescription());
        }
        if (taskToUpdate.getPriority() != null) {
            task.setPriority(taskToUpdate.getPriority());
        }
        if (taskToUpdate.getStatus() != null) {
            task.setStatus(taskToUpdate.getStatus());
        }
        if (isValidString(taskToUpdate.getAssigneeEmail())) {
            userRepository.findByEmail(taskToUpdate.getAssigneeEmail())
                    .ifPresent(task::setAssignee);
        }
    }

    private boolean isValidString(String value) {
        return value != null && !value.isBlank();
    }

    private void validateTitle(String title) {
        if (!inputValidator.isValid(title, null)) {
            throw new ValidationException("Название задачи слишком длинное (макс. 256 символов)");
        }
    }

    private void validateDescription(String description) {
        if (!inputValidator.isValid(description, null)) {
            throw new ValidationException("Описание задачи слишком длинное (макс. 256 символов)");
        }
    }

    private void validateTaskInput(TaskToCreate task) {
        if (!isValidString(task.getTitle())) {
            throw new ValidationException("Название задачи не может быть пустым");
        }
        validateTitle(task.getTitle());
        if (!isValidString(task.getDescription())) {
            throw new ValidationException("Описание задачи не может быть пустым");
        }
        validateDescription(task.getDescription());
        if (task.getPriority() == null) {
            throw new ValidationException("Приоритет задачи должен быть указан");
        }
        if (!emailValidator.isValid(task.getAssigneeEmail(), null)) {
            throw new ValidationException("Неверный формат email или пользователь не найден");
        }
    }

    private void validateCommentInput(String text) {
        if (!isValidString(text)) {
            throw new ValidationException("Комментарий не может быть пустым");
        }
        if (!inputValidator.isValid(text, null)) {
            throw new ValidationException("Комментарий слишком длинный (макс. 256 символов)");
        }
    }

    private TaskResponse mapToTaskResponse(Task task) {
        return TaskResponse.builder()
                .title(task.getTitle())
                .description(task.getDescription())
                .assigneeFirstName(task.getAssignee().getFirstName())
                .assigneeLastName(task.getAssignee().getLastName())
                .priority(task.getPriority())
                .status(task.getStatus())
                .comments(task.getComments().stream().map(this::mapToCommentResponse).toList())
                .build();
    }

    private CommentResponse mapToCommentResponse(Comment comment) {
        return CommentResponse.builder()
                .text(comment.getText())
                .build();
    }
}

