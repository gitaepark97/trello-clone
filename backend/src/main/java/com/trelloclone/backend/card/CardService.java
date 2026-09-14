package com.trelloclone.backend.card;

import com.trelloclone.backend.common.NotFoundException;
import com.trelloclone.backend.list.BoardListRepository;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Transactional
class CardService {

    private final CardRepository cardRepository;
    private final BoardListRepository boardListRepository;

    Card create(Long listId, CreateCardRequest request) {
        if (!boardListRepository.existsById(listId)) {
            throw new NotFoundException("List not found: " + listId);
        }
        double position = cardRepository.findTopByListIdOrderByPositionDesc(listId)
                .map(last -> last.getPosition() + 1.0)
                .orElse(1.0);
        return cardRepository.save(new Card(listId, request.title(), request.description(), position));
    }

    @Transactional(readOnly = true)
    List<Card> findAllByList(Long listId) {
        if (!boardListRepository.existsById(listId)) {
            throw new NotFoundException("List not found: " + listId);
        }
        return cardRepository.findByListIdOrderByPositionAsc(listId);
    }

    @Transactional(readOnly = true)
    Card findById(Long cardId) {
        return cardRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException("Card not found: " + cardId));
    }

    Card update(Long cardId, UpdateCardRequest request) {
        Card card = findById(cardId);
        if (request.title() != null) {
            card.setTitle(request.title());
        }
        if (request.description() != null) {
            card.setDescription(request.description());
        }
        return card;
    }

    void delete(Long cardId) {
        Card card = findById(cardId);
        cardRepository.delete(card);
    }

    Card updatePosition(Long cardId, UpdateCardPositionRequest request) {
        Card card = findById(cardId);
        if (!boardListRepository.existsById(request.listId())) {
            throw new NotFoundException("List not found: " + request.listId());
        }
        card.setListId(request.listId());
        card.setPosition(request.position());
        return card;
    }
}
