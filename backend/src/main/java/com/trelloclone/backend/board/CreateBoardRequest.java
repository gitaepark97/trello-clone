package com.trelloclone.backend.board;

import jakarta.validation.constraints.NotBlank;

record CreateBoardRequest(@NotBlank String title, String description) {
}
