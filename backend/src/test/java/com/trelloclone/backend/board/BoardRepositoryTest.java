package com.trelloclone.backend.board;

import static org.assertj.core.api.Assertions.assertThat;

import com.trelloclone.backend.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class BoardRepositoryTest {

    @Autowired
    private BoardRepository boardRepository;

    @Test
    void savesAndFindsABoard() {
        Board saved = boardRepository.save(new Board("My Board", "description"));

        Board found = boardRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getId()).isNotNull();
        assertThat(found.getTitle()).isEqualTo("My Board");
        assertThat(found.getDescription()).isEqualTo("description");
        assertThat(found.getOwnerId()).isNull();
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getUpdatedAt()).isNotNull();
    }
}
