package com.trelloclone.backend.card;

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
class CardController {

    private final CardService cardService;

    @PostMapping("/api/lists/{listId}/cards")
    @ResponseStatus(HttpStatus.CREATED)
    CardResponse create(@PathVariable Long listId, @Valid @RequestBody CreateCardRequest request) {
        return CardResponse.from(cardService.create(listId, request));
    }

    @GetMapping("/api/lists/{listId}/cards")
    List<CardResponse> findAllByList(@PathVariable Long listId) {
        return cardService.findAllByList(listId).stream().map(CardResponse::from).toList();
    }

    @GetMapping("/api/cards/{cardId}")
    CardResponse findById(@PathVariable Long cardId) {
        return CardResponse.from(cardService.findById(cardId));
    }

    @PatchMapping("/api/cards/{cardId}")
    CardResponse update(@PathVariable Long cardId, @RequestBody UpdateCardRequest request) {
        return CardResponse.from(cardService.update(cardId, request));
    }

    @DeleteMapping("/api/cards/{cardId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable Long cardId) {
        cardService.delete(cardId);
    }

    @PatchMapping("/api/cards/{cardId}/position")
    CardResponse updatePosition(@PathVariable Long cardId, @Valid @RequestBody UpdateCardPositionRequest request) {
        return CardResponse.from(cardService.updatePosition(cardId, request));
    }
}
