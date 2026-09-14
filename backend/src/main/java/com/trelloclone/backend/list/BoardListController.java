package com.trelloclone.backend.list;

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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class BoardListController {

    private final BoardListService boardListService;

    @PostMapping("/api/boards/{boardId}/lists")
    @ResponseStatus(HttpStatus.CREATED)
    BoardListResponse create(@PathVariable Long boardId, @Valid @RequestBody CreateListRequest request) {
        return BoardListResponse.from(boardListService.create(boardId, request));
    }

    @GetMapping("/api/boards/{boardId}/lists")
    List<BoardListResponse> findAllByBoard(@PathVariable Long boardId) {
        return boardListService.findAllByBoard(boardId).stream().map(BoardListResponse::from).toList();
    }

    @PatchMapping("/api/lists/{listId}")
    BoardListResponse update(@PathVariable Long listId, @RequestBody UpdateListRequest request) {
        return BoardListResponse.from(boardListService.update(listId, request));
    }

    @DeleteMapping("/api/lists/{listId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable Long listId) {
        boardListService.delete(listId);
    }

    @PatchMapping("/api/lists/{listId}/position")
    BoardListResponse updatePosition(
            @PathVariable Long listId, @Valid @RequestBody UpdateListPositionRequest request) {
        return BoardListResponse.from(boardListService.updatePosition(listId, request));
    }
}
