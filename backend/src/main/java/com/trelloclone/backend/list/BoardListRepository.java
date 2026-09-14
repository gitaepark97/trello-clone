package com.trelloclone.backend.list;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardListRepository extends JpaRepository<BoardList, Long> {

    List<BoardList> findByBoardIdOrderByPositionAsc(Long boardId);

    Optional<BoardList> findTopByBoardIdOrderByPositionDesc(Long boardId);
}
