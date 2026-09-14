package com.trelloclone.backend.board;

import static org.assertj.core.api.Assertions.assertThat;

import com.trelloclone.backend.TestcontainersConfiguration;
import com.trelloclone.backend.card.Card;
import com.trelloclone.backend.card.CardRepository;
import com.trelloclone.backend.common.ErrorResponse;
import com.trelloclone.backend.list.BoardList;
import com.trelloclone.backend.list.BoardListRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Import(TestcontainersConfiguration.class)
class BoardControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private BoardListRepository boardListRepository;

    @Autowired
    private CardRepository cardRepository;

    @LocalServerPort
    private int port;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @Test
    void createsABoardWithATitle() {
        ResponseEntity<BoardResponse> response = restTemplate.postForEntity(
                url("/api/boards"), new CreateBoardRequest("My Board", "desc"), BoardResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isNotNull();
        assertThat(response.getBody().title()).isEqualTo("My Board");
        assertThat(response.getBody().description()).isEqualTo("desc");
    }

    @Test
    void rejectsCreatingABoardWithoutATitle() {
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                url("/api/boards"), new CreateBoardRequest("", null), ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void listsAllBoards() {
        boardRepository.save(new Board("Board A", null));
        boardRepository.save(new Board("Board B", null));

        ResponseEntity<BoardResponse[]> response = restTemplate.getForEntity(url("/api/boards"), BoardResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).extracting(BoardResponse::title).contains("Board A", "Board B");
    }

    @Test
    void findsAnExistingBoardById() {
        Board board = boardRepository.save(new Board("Board", "desc"));

        ResponseEntity<BoardResponse> response =
                restTemplate.getForEntity(url("/api/boards/" + board.getId()), BoardResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().id()).isEqualTo(board.getId());
    }

    @Test
    void returnsNotFoundForAMissingBoard() {
        ResponseEntity<ErrorResponse> response =
                restTemplate.getForEntity(url("/api/boards/999999"), ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void updatesAnExistingBoard() {
        Board board = boardRepository.save(new Board("Old title", "old desc"));

        ResponseEntity<BoardResponse> response = restTemplate.exchange(
                url("/api/boards/" + board.getId()),
                org.springframework.http.HttpMethod.PATCH,
                new org.springframework.http.HttpEntity<>(new UpdateBoardRequest("New title", "new desc")),
                BoardResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().title()).isEqualTo("New title");
        assertThat(response.getBody().description()).isEqualTo("new desc");
    }

    @Test
    void returnsNotFoundWhenUpdatingAMissingBoard() {
        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                url("/api/boards/999999"),
                org.springframework.http.HttpMethod.PATCH,
                new org.springframework.http.HttpEntity<>(new UpdateBoardRequest("New title", null)),
                ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deletingABoardCascadesToItsListsAndCards() {
        Board board = boardRepository.save(new Board("Board", null));
        BoardList list = boardListRepository.save(new BoardList(board.getId(), "To Do", 1.0));
        Card card = cardRepository.save(new Card(list.getId(), "Task", null, 1.0));

        ResponseEntity<Void> response = restTemplate.exchange(
                url("/api/boards/" + board.getId()),
                org.springframework.http.HttpMethod.DELETE,
                null,
                Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(boardRepository.findById(board.getId())).isEmpty();
        assertThat(boardListRepository.findById(list.getId())).isEmpty();
        assertThat(cardRepository.findById(card.getId())).isEmpty();
    }

    @Test
    void returnsNotFoundWhenDeletingAMissingBoard() {
        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                url("/api/boards/999999"),
                org.springframework.http.HttpMethod.DELETE,
                null,
                ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
