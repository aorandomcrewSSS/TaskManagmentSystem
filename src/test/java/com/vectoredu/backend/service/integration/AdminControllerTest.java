package com.vectoredu.backend.service.integration;

import com.vectoredu.backend.model.User;
import com.vectoredu.backend.model.enums.Role;
import com.vectoredu.backend.repository.UserRepository;
import com.vectoredu.backend.service.JwtService;
import com.vectoredu.backend.service.config.AbstractIntegrationTest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.JsonNode;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@AutoConfigureMockMvc
@Tag(name = "Управление задачами", description = "Операции по созданию, редактированию и удалению задач")
public class AdminControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private String adminToken;

    @BeforeEach
    public void setup() throws Exception {
        createAdminIfNotExists();
        createAssigneeIfNotExists();

        // Генерация токена для администратора
        User admin = userRepository.findByEmail("admin@example.com")
                .orElseThrow(() -> new RuntimeException("Администратор не найден"));
        adminToken = jwtService.generateToken(admin);
    }

    @AfterEach
    public void clearDatabase() {
        jdbcTemplate.execute("DELETE FROM comments;");
        jdbcTemplate.execute("DELETE FROM tasks;");
        jdbcTemplate.execute("DELETE FROM reset_password;");
        jdbcTemplate.execute("DELETE FROM users;");
    }

    @Test
    public void testCreateTask() throws Exception {
        String json = """
        {
            "title": "Task 1",
            "description": "Task description",
            "priority": "HIGH",
            "assigneeEmail": "assignee@example.com"
        }
        """;

        MvcResult createResult = mockMvc.perform(post("/admin/tasks")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn();

        // Извлечение ID задачи из ответа
        String responseBody = createResult.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        Long taskId = jsonNode.get("id").asLong();

        // Проверка, что задача была создана
        mockMvc.perform(get("/admin/tasks/{id}", taskId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Task 1"))
                .andExpect(jsonPath("$.description").value("Task description"))
                .andExpect(jsonPath("$.priority").value("HIGH"));
    }

    @Test
    public void testGetAllTasks() throws Exception {
        // Создание задачи
        String taskJson = """
        {
            "title": "Task 1",
            "description": "Task description",
            "priority": "HIGH",
            "assigneeEmail": "assignee@example.com"
        }
        """;
        MvcResult createResult = mockMvc.perform(post("/admin/tasks")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskJson))
                .andExpect(status().isOk())
                .andReturn();

        // Извлечение ID задачи из ответа
        String responseBody = createResult.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        Long taskId = jsonNode.get("id").asLong();

        // Получение всех задач
        mockMvc.perform(get("/admin/tasks")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Task 1"));
    }

    @Test
    public void testUpdateTask() throws Exception {
        // Создание задачи
        String taskJson = """
        {
            "title": "Task 1",
            "description": "Task description",
            "priority": "HIGH",
            "assigneeEmail": "assignee@example.com"
        }
        """;
        MvcResult createResult = mockMvc.perform(post("/admin/tasks")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskJson))
                .andExpect(status().isOk())
                .andReturn();

        // Извлечение ID задачи из ответа
        String responseBody = createResult.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        Long taskId = jsonNode.get("id").asLong();

        // Обновление задачи
        String updateJson = """
        {
            "title": "Updated Task 1",
            "description": "Updated description",
            "priority": "LOW",
            "assigneeEmail": "assignee@example.com"
        }
        """;
        mockMvc.perform(put("/admin/tasks/{taskId}", taskId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Task 1"))
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.priority").value("LOW"));
    }

    @Test
    public void testDeleteTask() throws Exception {
        // Создание задачи
        String taskJson = """
        {
            "title": "Task 1",
            "description": "Task description",
            "priority": "HIGH",
            "assigneeEmail": "assignee@example.com"
        }
        """;
        MvcResult createResult = mockMvc.perform(post("/admin/tasks")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskJson))
                .andExpect(status().isOk())
                .andReturn();

        // Извлечение ID задачи из ответа
        String responseBody = createResult.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        Long taskId = jsonNode.get("id").asLong();

        // Удаление задачи
        mockMvc.perform(delete("/admin/tasks/{taskId}", taskId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Task 1"));

        // Проверка, что задача была удалена
        mockMvc.perform(get("/admin/tasks/{taskId}", taskId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testAddCommentToTask() throws Exception {
        // Создание задачи
        String taskJson = """
        {
            "title": "Task 1",
            "description": "Task description",
            "priority": "HIGH",
            "assigneeEmail": "assignee@example.com"
        }
        """;
        MvcResult createResult = mockMvc.perform(post("/admin/tasks")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskJson))
                .andExpect(status().isOk())
                .andReturn();

        // Извлечение ID задачи из ответа
        String responseBody = createResult.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        Long taskId = jsonNode.get("id").asLong();

        // Добавление комментария
        String commentText = "This is a comment";
        mockMvc.perform(post("/admin/tasks/{taskId}/comments", taskId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commentText))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("This is a comment"));
    }

    @PostConstruct
    public void createAdminIfNotExists() {
        Optional<User> adminOptional = userRepository.findByEmail("admin@example.com");
        if (adminOptional.isEmpty()) {
            User admin = User.builder()
                    .firstName("Admin")
                    .lastName("Admin")
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("Admin1234"))
                    .role(Role.ADMIN)
                    .enabled(true)
                    .build();
            userRepository.save(admin);
        }
    }

    @PostConstruct
    public void createAssigneeIfNotExists() {
        String assigneeEmail = "assignee@example.com";
        Optional<User> assigneeOptional = userRepository.findByEmail(assigneeEmail);
        if (assigneeOptional.isEmpty()) {
            User assignee = User.builder()
                    .firstName("Assignee")
                    .lastName("Assignee")
                    .email(assigneeEmail)
                    .password(passwordEncoder.encode("Assignee1234"))
                    .role(Role.USER)
                    .enabled(true)
                    .build();
            userRepository.save(assignee);
        }
    }
}
