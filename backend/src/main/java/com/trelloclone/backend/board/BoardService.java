package com.trelloclone.backend.board;

import com.trelloclone.backend.common.NotFoundException;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Transactional
class BoardService {

    private final BoardRepository boardRepository;

    Board create(CreateBoardRequest request) {
        Board board = new Board(request.title(), request.description());
        return boardRepository.save(board);
    }

    @Transactional(readOnly = true)
    List<Board> findAll() {
        return boardRepository.findAll();
    }

    @Transactional(readOnly = true)
    Board findById(Long boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new NotFoundException("Board not found: " + boardId));
    }

    Board update(Long boardId, UpdateBoardRequest request) {
        Board board = findById(boardId);
        if (request.title() != null) {
            board.setTitle(request.title());
        }
        if (request.description() != null) {
            board.setDescription(request.description());
        }
        return board;
    }

    void delete(Long boardId) {
        Board board = findById(boardId);
        boardRepository.delete(board);
    }
}
