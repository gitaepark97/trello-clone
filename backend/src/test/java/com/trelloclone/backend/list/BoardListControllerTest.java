package com.trelloclone.backend.list;

import static org.assertj.core.api.Assertions.assertThat;

import com.trelloclone.backend.TestcontainersConfiguration;
import com.trelloclone.backend.board.Board;
import com.trelloclone.backend.board.BoardRepository;
import com.trelloclone.backend.common.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Import(TestcontainersConfiguration.class)
class BoardListControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private BoardListRepository boardListRepository;

    @LocalServerPort
    private int port;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private Long createBoard() {
        return boardRepository.save(new Board("Board", null)).getId();
    }

    @Test
    void createsAListOnAnExistingBoardAtTheLastPosition() {
        Long boardId = createBoard();
        boardListRepository.save(new BoardList(boardId, "To Do", 1.0));

        ResponseEntity<BoardListResponse> response = restTemplate.postForEntity(
                url("/api/boards/" + boardId + "/lists"), new CreateListRequest("Doing"), BoardListResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().title()).isEqualTo("Doing");
        assertThat(response.getBody().position()).isGreaterThan(1.0);
    }

    @Test
    void returnsNotFoundWhenCreatingAListOnAMissingBoard() {
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                url("/api/boards/999999/lists"), new CreateListRequest("Doing"), ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void rejectsCreatingAListWithoutATitle() {
        Long boardId = createBoard();

        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                url("/api/boards/" + boardId + "/lists"), new CreateListRequest(""), ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void listsListsOrderedByPosition() {
        Long boardId = createBoard();
        boardListRepository.save(new BoardList(boardId, "Doing", 2.0));
        boardListRepository.save(new BoardList(boardId, "To Do", 1.0));

        ResponseEntity<BoardListResponse[]> response =
                restTemplate.getForEntity(url("/api/boards/" + boardId + "/lists"), BoardListResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).extracting(BoardListResponse::title).containsExactly("To Do", "Doing");
    }

    @Test
    void updatesAListTitle() {
        Long boardId = createBoard();
        BoardList list = boardListRepository.save(new BoardList(boardId, "Old", 1.0));

        ResponseEntity<BoardListResponse> response = restTemplate.exchange(
                url("/api/lists/" + list.getId()),
                HttpMethod.PATCH,
                new HttpEntity<>(new UpdateListRequest("New")),
                BoardListResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().title()).isEqualTo("New");
    }

    @Test
    void returnsNotFoundWhenUpdatingAMissingList() {
        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                url("/api/lists/999999"),
                HttpMethod.PATCH,
                new HttpEntity<>(new UpdateListRequest("New")),
                ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deletesAListAndItsCards() {
        Long boardId = createBoard();
        BoardList list = boardListRepository.save(new BoardList(boardId, "To Do", 1.0));

        ResponseEntity<Void> response =
                restTemplate.exchange(url("/api/lists/" + list.getId()), HttpMethod.DELETE, null, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(boardListRepository.findById(list.getId())).isEmpty();
    }

    @Test
    void returnsNotFoundWhenDeletingAMissingList() {
        ResponseEntity<ErrorResponse> response =
                restTemplate.exchange(url("/api/lists/999999"), HttpMethod.DELETE, null, ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void changesAListsPositionWithinTheSameBoard() {
        Long boardId = createBoard();
        BoardList list = boardListRepository.save(new BoardList(boardId, "To Do", 1.0));

        ResponseEntity<BoardListResponse> response = restTemplate.exchange(
                url("/api/lists/" + list.getId() + "/position"),
                HttpMethod.PATCH,
                new HttpEntity<>(new UpdateListPositionRequest(5.5)),
                BoardListResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().position()).isEqualTo(5.5);
    }

    @Test
    void returnsNotFoundWhenChangingPositionOfAMissingList() {
        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                url("/api/lists/999999/position"),
                HttpMethod.PATCH,
                new HttpEntity<>(new UpdateListPositionRequest(1.0)),
                ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
