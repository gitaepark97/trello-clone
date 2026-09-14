package com.trelloclone.backend.card;

import jakarta.validation.constraints.NotNull;

record UpdateCardPositionRequest(@NotNull Long listId, @NotNull Double position) {
}
