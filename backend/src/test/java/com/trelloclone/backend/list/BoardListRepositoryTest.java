package com.trelloclone.backend.list;

import static org.assertj.core.api.Assertions.assertThat;

import com.trelloclone.backend.TestcontainersConfiguration;
import com.trelloclone.backend.board.Board;
import com.trelloclone.backend.board.BoardRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class BoardListRepositoryTest {

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private BoardListRepository boardListRepository;

    @Test
    void savesAndFindsListsOrderedByPosition() {
        Board board = boardRepository.save(new Board("Board", null));

        boardListRepository.save(new BoardList(board.getId(), "Doing", 2.0));
        boardListRepository.save(new BoardList(board.getId(), "To Do", 1.0));

        List<BoardList> lists = boardListRepository.findByBoardIdOrderByPositionAsc(board.getId());

        assertThat(lists).extracting(BoardList::getTitle).containsExactly("To Do", "Doing");
    }

    @Test
    void findsTopByBoardIdOrderByPositionDesc() {
        Board board = boardRepository.save(new Board("Board", null));
        boardListRepository.save(new BoardList(board.getId(), "To Do", 1.0));
        boardListRepository.save(new BoardList(board.getId(), "Doing", 2.0));

        BoardList last = boardListRepository.findTopByBoardIdOrderByPositionDesc(board.getId()).orElseThrow();

        assertThat(last.getTitle()).isEqualTo("Doing");
    }
}
