package com.trelloclone.backend.card;

import jakarta.validation.constraints.NotBlank;

record CreateCardRequest(@NotBlank String title, String description) {
}
