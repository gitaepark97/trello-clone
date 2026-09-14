package com.trelloclone.backend.card;

import static org.assertj.core.api.Assertions.assertThat;

import com.trelloclone.backend.TestcontainersConfiguration;
import com.trelloclone.backend.board.Board;
import com.trelloclone.backend.board.BoardRepository;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Import(TestcontainersConfiguration.class)
class CardControllerTest {

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

    private Long createList() {
        Long boardId = boardRepository.save(new Board("Board", null)).getId();
        return boardListRepository.save(new BoardList(boardId, "To Do", 1.0)).getId();
    }

    @Test
    void createsACardOnAnExistingListAtTheLastPosition() {
        Long listId = createList();
        cardRepository.save(new Card(listId, "First", null, 1.0));

        ResponseEntity<CardResponse> response = restTemplate.postForEntity(
                url("/api/lists/" + listId + "/cards"), new CreateCardRequest("Second", "desc"), CardResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().title()).isEqualTo("Second");
        assertThat(response.getBody().description()).isEqualTo("desc");
        assertThat(response.getBody().position()).isGreaterThan(1.0);
    }

    @Test
    void returnsNotFoundWhenCreatingACardOnAMissingList() {
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                url("/api/lists/999999/cards"), new CreateCardRequest("Card", null), ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void rejectsCreatingACardWithoutATitle() {
        Long listId = createList();

        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                url("/api/lists/" + listId + "/cards"), new CreateCardRequest("", null), ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void listsCardsOrderedByPosition() {
        Long listId = createList();
        cardRepository.save(new Card(listId, "Second", null, 2.0));
        cardRepository.save(new Card(listId, "First", null, 1.0));

        ResponseEntity<CardResponse[]> response =
                restTemplate.getForEntity(url("/api/lists/" + listId + "/cards"), CardResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).extracting(CardResponse::title).containsExactly("First", "Second");
    }

    @Test
    void returnsNotFoundForAMissingCard() {
        ResponseEntity<ErrorResponse> response =
                restTemplate.getForEntity(url("/api/cards/999999"), ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void updatesACardsTitleAndDescription() {
        Long listId = createList();
        Card card = cardRepository.save(new Card(listId, "Old", "old desc", 1.0));

        ResponseEntity<CardResponse> response = restTemplate.exchange(
                url("/api/cards/" + card.getId()),
                HttpMethod.PATCH,
                new HttpEntity<>(new UpdateCardRequest("New", "new desc")),
                CardResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().title()).isEqualTo("New");
        assertThat(response.getBody().description()).isEqualTo("new desc");
    }

    @Test
    void returnsNotFoundWhenUpdatingAMissingCard() {
        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                url("/api/cards/999999"),
                HttpMethod.PATCH,
                new HttpEntity<>(new UpdateCardRequest("New", null)),
                ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deletesACard() {
        Long listId = createList();
        Card card = cardRepository.save(new Card(listId, "Card", null, 1.0));

        ResponseEntity<Void> response =
                restTemplate.exchange(url("/api/cards/" + card.getId()), HttpMethod.DELETE, null, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(cardRepository.findById(card.getId())).isEmpty();
    }

    @Test
    void returnsNotFoundWhenDeletingAMissingCard() {
        ResponseEntity<ErrorResponse> response =
                restTemplate.exchange(url("/api/cards/999999"), HttpMethod.DELETE, null, ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void changesACardsPositionWithinTheSameList() {
        Long listId = createList();
        Card card = cardRepository.save(new Card(listId, "Card", null, 1.0));

        ResponseEntity<CardResponse> response = restTemplate.exchange(
                url("/api/cards/" + card.getId() + "/position"),
                HttpMethod.PATCH,
                new HttpEntity<>(new UpdateCardPositionRequest(listId, 5.5)),
                CardResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().listId()).isEqualTo(listId);
        assertThat(response.getBody().position()).isEqualTo(5.5);
    }

    @Test
    void movesACardToAnotherListConsistently() {
        Long sourceListId = createList();
        Long boardId = boardListRepository.findById(sourceListId).orElseThrow().getBoardId();
        Long targetListId =
                boardListRepository.save(new BoardList(boardId, "Doing", 2.0)).getId();
        Card card = cardRepository.save(new Card(sourceListId, "Card", null, 1.0));

        ResponseEntity<CardResponse> response = restTemplate.exchange(
                url("/api/cards/" + card.getId() + "/position"),
                HttpMethod.PATCH,
                new HttpEntity<>(new UpdateCardPositionRequest(targetListId, 1.0)),
                CardResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().listId()).isEqualTo(targetListId);
        assertThat(cardRepository.findByListIdOrderByPositionAsc(sourceListId)).isEmpty();
        assertThat(cardRepository.findByListIdOrderByPositionAsc(targetListId))
                .extracting(Card::getId)
                .containsExactly(card.getId());
    }

    @Test
    void returnsNotFoundWhenMovingACardToAMissingList() {
        Long listId = createList();
        Card card = cardRepository.save(new Card(listId, "Card", null, 1.0));

        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                url("/api/cards/" + card.getId() + "/position"),
                HttpMethod.PATCH,
                new HttpEntity<>(new UpdateCardPositionRequest(999999L, 1.0)),
                ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
