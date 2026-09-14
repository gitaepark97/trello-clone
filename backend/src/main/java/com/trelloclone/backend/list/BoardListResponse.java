package com.trelloclone.backend.list;

import java.time.LocalDateTime;

record BoardListResponse(
        Long id,
        Long boardId,
        String title,
        double position,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    static BoardListResponse from(BoardList list) {
        return new BoardListResponse(
                list.getId(),
                list.getBoardId(),
                list.getTitle(),
                list.getPosition(),
                list.getCreatedAt(),
                list.getUpdatedAt()
        );
    }
}
