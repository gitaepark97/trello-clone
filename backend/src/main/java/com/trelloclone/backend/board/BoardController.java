package com.trelloclone.backend.board;

import jakarta.validation.Valid;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class BoardController {

    private final BoardService boardService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    BoardResponse create(@Valid @RequestBody CreateBoardRequest request) {
        return BoardResponse.from(boardService.create(request));
    }

    @GetMapping
    List<BoardResponse> findAll() {
        return boardService.findAll().stream().map(BoardResponse::from).toList();
    }

    @GetMapping("/{boardId}")
    BoardResponse findById(@PathVariable Long boardId) {
        return BoardResponse.from(boardService.findById(boardId));
    }

    @PatchMapping("/{boardId}")
    BoardResponse update(@PathVariable Long boardId, @RequestBody UpdateBoardRequest request) {
        return BoardResponse.from(boardService.update(boardId, request));
    }

    @DeleteMapping("/{boardId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable Long boardId) {
        boardService.delete(boardId);
    }
}
