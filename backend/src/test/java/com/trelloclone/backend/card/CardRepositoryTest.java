package com.trelloclone.backend.card;

import static org.assertj.core.api.Assertions.assertThat;

import com.trelloclone.backend.TestcontainersConfiguration;
import com.trelloclone.backend.board.Board;
import com.trelloclone.backend.board.BoardRepository;
import com.trelloclone.backend.list.BoardList;
import com.trelloclone.backend.list.BoardListRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class CardRepositoryTest {

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private BoardListRepository boardListRepository;

    @Autowired
    private CardRepository cardRepository;

    @Test
    void savesAndFindsCardsOrderedByPosition() {
        Board board = boardRepository.save(new Board("Board", null));
        BoardList list = boardListRepository.save(new BoardList(board.getId(), "To Do", 1.0));

        cardRepository.save(new Card(list.getId(), "Write tests", "", 2.0));
        cardRepository.save(new Card(list.getId(), "Write code", null, 1.0));

        List<Card> cards = cardRepository.findByListIdOrderByPositionAsc(list.getId());

        assertThat(cards).extracting(Card::getTitle).containsExactly("Write code", "Write tests");
    }

    @Test
    void findsTopByListIdOrderByPositionDesc() {
        Board board = boardRepository.save(new Board("Board", null));
        BoardList list = boardListRepository.save(new BoardList(board.getId(), "To Do", 1.0));
        cardRepository.save(new Card(list.getId(), "Write code", null, 1.0));
        cardRepository.save(new Card(list.getId(), "Write tests", null, 2.0));

        Card last = cardRepository.findTopByListIdOrderByPositionDesc(list.getId()).orElseThrow();

        assertThat(last.getTitle()).isEqualTo("Write tests");
    }
}
