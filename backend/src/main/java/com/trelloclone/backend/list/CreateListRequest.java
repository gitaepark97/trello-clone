package com.trelloclone.backend.list;

import jakarta.validation.constraints.NotBlank;

record CreateListRequest(@NotBlank String title) {
}
