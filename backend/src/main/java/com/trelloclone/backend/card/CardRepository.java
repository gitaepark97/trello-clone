package com.trelloclone.backend.card;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardRepository extends JpaRepository<Card, Long> {

    List<Card> findByListIdOrderByPositionAsc(Long listId);

    Optional<Card> findTopByListIdOrderByPositionDesc(Long listId);
}
