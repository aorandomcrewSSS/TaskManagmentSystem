package com.vectoredu.backend.dto.request.taskRequestDto;

import com.vectoredu.backend.model.enums.Priority;
import com.vectoredu.backend.model.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain=true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskToUpdate {

    private String title;

    private String description;

    private Priority priority;

    private Status status;

    private String assigneeEmail;
}
