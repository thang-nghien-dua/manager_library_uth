package com.hacker.boooks.repository;

import com.hacker.boooks.entity.FineEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class FineRepositoryTest {

    private static final int slNo = 1;

    @Autowired
    @SuppressWarnings("unused")
    private FineRepository underTest;

    @BeforeEach
    void setUp() {
        int daysOverdue = 5;
        float fineAmount = 2.0f;

        FineEntity fineEntity = new FineEntity(slNo, daysOverdue, fineAmount);
        underTest.save(fineEntity);
    }

    @Test
    void findAll_WhenFineEntitiesExist_ShouldReturnListOfFineEntities() {
        // Act
        List<FineEntity> fineEntities = underTest.findAll();

        // Assert
        assertThat(fineEntities).hasSize(1);
        assertThat(fineEntities.get(0).getSlNo()).isEqualTo(slNo);
    }

    @Test
    void findAll_WhenNoFineEntitiesExist_ShouldReturnEmptyList() {
        // Arrange
        underTest.deleteAll();

        // Act
        List<FineEntity> fineEntities = underTest.findAll();

        // Assert
        assertThat(fineEntities).isEmpty();
    }

    @Test
    void findById_WhenFineEntityExists_ShouldReturnFineEntityById() {
        // Act
        Optional<FineEntity> optionalFineEntity = underTest.findById(1);

        // Assert
        assertThat(optionalFineEntity).isPresent();
        assertThat(optionalFineEntity.get().getSlNo()).isEqualTo(slNo);
    }

    @Test
    void findById_WhenFineEntityDoesNotExist_ShouldReturnEmptyOptional() {
        // Act
        Optional<FineEntity> optionalFineEntity = underTest.findById(-1);

        // Assert
        assertThat(optionalFineEntity).isEmpty();
    }

    @Test
    void save_WhenFineEntityIsValid_ShouldSaveFineEntity() {
        // Arrange
        FineEntity fineEntity = new FineEntity();
        fineEntity.setSlNo(2);
        fineEntity.setDaysOverdue(10);
        fineEntity.setFineAmount(5.0f);

        // Act
        FineEntity savedFineEntity = underTest.save(fineEntity);

        // Assert
        assertThat(underTest.findById(2)).isPresent();
        assertThat(savedFineEntity.getSlNo()).isEqualTo(fineEntity.getSlNo());
        assertThat(savedFineEntity.getDaysOverdue()).isEqualTo(fineEntity.getDaysOverdue());
        assertThat(savedFineEntity.getFineAmount()).isEqualTo(fineEntity.getFineAmount());
    }


}