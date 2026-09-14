package com.trelloclone.backend.list;

import jakarta.validation.constraints.NotNull;

record UpdateListPositionRequest(@NotNull Double position) {
}
