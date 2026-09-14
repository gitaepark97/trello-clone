package com.trelloclone.backend.card;

import java.time.LocalDateTime;

record CardResponse(
        Long id,
        Long listId,
        String title,
        String description,
        double position,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    static CardResponse from(Card card) {
        return new CardResponse(
                card.getId(),
                card.getListId(),
                card.getTitle(),
                card.getDescription(),
                card.getPosition(),
                card.getCreatedAt(),
                card.getUpdatedAt()
        );
    }
}
