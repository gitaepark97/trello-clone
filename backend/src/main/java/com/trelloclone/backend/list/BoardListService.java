package com.trelloclone.backend.list;

import com.trelloclone.backend.board.BoardRepository;
import com.trelloclone.backend.common.NotFoundException;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Transactional
class BoardListService {

    private final BoardListRepository boardListRepository;
    private final BoardRepository boardRepository;

    BoardList create(Long boardId, CreateListRequest request) {
        if (!boardRepository.existsById(boardId)) {
            throw new NotFoundException("Board not found: " + boardId);
        }
        double position = boardListRepository.findTopByBoardIdOrderByPositionDesc(boardId)
                .map(last -> last.getPosition() + 1.0)
                .orElse(1.0);
        return boardListRepository.save(new BoardList(boardId, request.title(), position));
    }

    @Transactional(readOnly = true)
    List<BoardList> findAllByBoard(Long boardId) {
        if (!boardRepository.existsById(boardId)) {
            throw new NotFoundException("Board not found: " + boardId);
        }
        return boardListRepository.findByBoardIdOrderByPositionAsc(boardId);
    }

    @Transactional(readOnly = true)
    BoardList findById(Long listId) {
        return boardListRepository.findById(listId)
                .orElseThrow(() -> new NotFoundException("List not found: " + listId));
    }

    BoardList update(Long listId, UpdateListRequest request) {
        BoardList list = findById(listId);
        if (request.title() != null) {
            list.setTitle(request.title());
        }
        return list;
    }

    void delete(Long listId) {
        BoardList list = findById(listId);
        boardListRepository.delete(list);
    }

    BoardList updatePosition(Long listId, UpdateListPositionRequest request) {
        BoardList list = findById(listId);
        list.setPosition(request.position());
        return list;
    }
}
