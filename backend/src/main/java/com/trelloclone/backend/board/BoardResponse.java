package com.trelloclone.backend.board;

import java.time.LocalDateTime;

record BoardResponse(
        Long id,
        String title,
        String description,
        Long ownerId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    static BoardResponse from(Board board) {
        return new BoardResponse(
                board.getId(),
                board.getTitle(),
                board.getDescription(),
                board.getOwnerId(),
                board.getCreatedAt(),
                board.getUpdatedAt()
        );
    }
}
