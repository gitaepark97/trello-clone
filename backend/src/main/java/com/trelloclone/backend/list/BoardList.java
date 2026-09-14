package com.trelloclone.backend.list;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "list")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long id;

    @Column(name = "board_id", nullable = false)
    @Getter
    private Long boardId;

    @Column(nullable = false)
    @Getter(AccessLevel.PACKAGE)
    @Setter(AccessLevel.PACKAGE)
    private String title;

    @Column(nullable = false)
    @Getter(AccessLevel.PACKAGE)
    @Setter(AccessLevel.PACKAGE)
    private double position;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @Getter(AccessLevel.PACKAGE)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    @Getter(AccessLevel.PACKAGE)
    private LocalDateTime updatedAt;

    public BoardList(Long boardId, String title, double position) {
        this.boardId = boardId;
        this.title = title;
        this.position = position;
    }
}
