package com.vectoredu.backend.repository;

import com.vectoredu.backend.model.Comment;
import com.vectoredu.backend.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByTask(Task task);

    @Modifying
    @Transactional
    void deleteByTask(Task task);
}
