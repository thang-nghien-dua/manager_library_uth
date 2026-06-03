package com.hacker.boooks.service;

import com.hacker.boooks.bean.Fine;
import com.hacker.boooks.entity.FineEntity;
import com.hacker.boooks.repository.FineRepository;
import com.hacker.boooks.service.impl.FineServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FineServiceTest {


    @Mock
    private FineRepository fineRepository;

    @InjectMocks
    private FineServiceImpl underTest;

    @Test
    void testGetFineDetails_WithExistingFineDetails_ReturnsFineDetails() {
        // Arrange
        FineEntity fineEntity = new FineEntity(1, 5, 10.0f);
        List<FineEntity> fineEntities = new ArrayList<>();
        fineEntities.add(fineEntity);
        when(fineRepository.findAll()).thenReturn(fineEntities);

        // Act
        ResponseEntity<Fine> response = underTest.getFineDetails();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(fineEntity.getDaysOverdue(), Objects.requireNonNull(response.getBody()).getDaysOverdue());
        assertEquals(fineEntity.getFineAmount(), response.getBody().getAmount());
    }

    @Test
    void testGetFineDetails_WithNoFineDetails_ReturnsNotFound() {
        // Arrange
        List<FineEntity> fineEntities = new ArrayList<>();
        when(fineRepository.findAll()).thenReturn(fineEntities);

        // Act
        ResponseEntity<Fine> response = underTest.getFineDetails();

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testGetFineDetails_ExceptionThrown_ReturnsInternalServerError() {
        // Arrange
        when(fineRepository.findAll()).thenThrow(new RuntimeException("Database connection error"));

        // Act
        ResponseEntity<Fine> response = underTest.getFineDetails();

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testSetDaysForFine_WithExistingFineDetails_ReturnsSuccessMessage() {
        // Arrange
        FineEntity fineEntity = new FineEntity(1, 5, 10.0f);
        List<FineEntity> fineEntities = new ArrayList<>();
        fineEntities.add(fineEntity);
        when(fineRepository.findAll()).thenReturn(fineEntities);

        // Act
        ResponseEntity<String> response = underTest.setDaysForFine(7);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Fine collection days updated successfully", response.getBody());
        assertEquals(7, fineEntity.getDaysOverdue());
        verify(fineRepository, times(1)).save(fineEntity);
    }

    @Test
    void testSetDaysForFine_WithNoFineDetails_ReturnsNotFound() {
        // Arrange
        List<FineEntity> fineEntities = new ArrayList<>();
        when(fineRepository.findAll()).thenReturn(fineEntities);

        // Act
        ResponseEntity<String> response = underTest.setDaysForFine(7);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(fineRepository, never()).save(any());
    }

    @Test
    void testSetDaysForFine_ExceptionThrown_ReturnsInternalServerError() {
        // Arrange
        when(fineRepository.findAll()).thenThrow(new RuntimeException("Database connection error"));

        // Act
        ResponseEntity<String> response = underTest.setDaysForFine(7);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
        verify(fineRepository, never()).save(any());
    }

    @Test
    void testSetAmountForFine_WithExistingFineDetails_ReturnsSuccessMessage() {
        // Arrange
        FineEntity fineEntity = new FineEntity(1, 5, 10.0f);
        List<FineEntity> fineEntities = new ArrayList<>();
        fineEntities.add(fineEntity);
        when(fineRepository.findAll()).thenReturn(fineEntities);

        // Act
        ResponseEntity<String> response = underTest.setAmountForFine(15.0f);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Fine amount updated successfully", response.getBody());
        assertEquals(15.0f, fineEntity.getFineAmount());
        verify(fineRepository, times(1)).save(fineEntity);
    }

    @Test
    void testSetAmountForFine_WithNoFineDetails_ReturnsNotFound() {
        // Arrange
        List<FineEntity> fineEntities = new ArrayList<>();
        when(fineRepository.findAll()).thenReturn(fineEntities);

        // Act
        ResponseEntity<String> response = underTest.setAmountForFine(15.0f);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(fineRepository, never()).save(any());
    }

    @Test
    void testSetAmountForFine_ExceptionThrown_ReturnsInternalServerError() {
        // Arrange
        when(fineRepository.findAll()).thenThrow(new RuntimeException("Database connection error"));

        // Act
        ResponseEntity<String> response = underTest.setAmountForFine(15.0f);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
        verify(fineRepository, never()).save(any());
    }

}