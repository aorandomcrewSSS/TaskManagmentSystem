package com.vectoredu.backend.dto.response;

import com.vectoredu.backend.model.enums.Priority;
import com.vectoredu.backend.model.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain=true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskResponse {
    private Long id;

    private String title;

    private String description;

    private Priority priority;

    private String assigneeFirstName;

    private String assigneeLastName;

    private Status status;

    private List<CommentResponse> comments;
}
