package com.vectoredu.backend.service;

import com.vectoredu.backend.dto.request.TaskToCreate;
import com.vectoredu.backend.dto.response.TaskResponse;
import com.vectoredu.backend.model.Task;
import com.vectoredu.backend.model.User;
import com.vectoredu.backend.model.enums.Status;
import com.vectoredu.backend.repository.TaskRepository;
import com.vectoredu.backend.repository.UserRepository;
import com.vectoredu.backend.util.exception.NotFoundException;
import com.vectoredu.backend.util.exception.ValidationException;
import com.vectoredu.backend.util.validators.EmailValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final EmailValidator emailValidator;

    @PreAuthorize("hasRole('ADMIN')")
    public TaskResponse createTask(TaskToCreate taskToCreate) {
        validateTaskInput(taskToCreate);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User author = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        User assignee = userRepository.findByEmail(taskToCreate.getAssigneeEmail())
                .orElseThrow(() -> new NotFoundException("Исполнитель не найден"));

        Task task = Task.builder()
                .title(taskToCreate.getTitle())
                .description(taskToCreate.getDescription())
                .author(author)
                .assignee(assignee)
                .status(Status.PENDING)
                .priority(taskToCreate.getPriority())
                .build();

        taskRepository.save(task);

        return TaskResponse.builder()
                .title(task.getTitle())
                .description(task.getDescription())
                .assigneeFirstName(assignee.getFirstName())
                .assigneeLastName(assignee.getLastName())
                .priority(task.getPriority())
                .status(task.getStatus())
                .build();
    }

    private void validateTaskInput(TaskToCreate task) {
        if (task.getTitle() == null || task.getTitle().isBlank()) {
            throw new ValidationException("Название задачи не может быть пустым");
        }
        if (task.getDescription() == null || task.getDescription().isBlank()) {
            throw new ValidationException("Описание задачи не может быть пустым");
        }
        if (task.getPriority() == null) {
            throw new ValidationException("Приоритет задачи должен быть указан");
        }
        if (!emailValidator.isValid(task.getAssigneeEmail(), null)) {
            throw new ValidationException("Не верный формат email или пользователь не найден");
        }
    }
}
