package com.hacker.boooks.service.impl;

import com.hacker.boooks.bean.Fine;
import com.hacker.boooks.entity.FineEntity;
import com.hacker.boooks.repository.FineRepository;
import com.hacker.boooks.service.FineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Log management service implementation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("unused")
public class FineServiceImpl implements FineService {

    private static final String FINE_DETAILS_NOT_FOUND = "Fine details not found in the system";
    private final FineRepository fineRepository;

    /**
     * Retrieves the current fine details from the system.
     *
     * @return ResponseEntity containing the Fine details
     */
    @Override
    public ResponseEntity<Fine> getFineDetails() {

        try {

            List<FineEntity> fineEntities = fineRepository.findAll();

            if (fineEntities.isEmpty()) {
                log.debug(FINE_DETAILS_NOT_FOUND);
                return ResponseEntity.notFound().build();
            }

            FineEntity fineEntity = fineEntities.get(0);
            Fine fine = new Fine(fineEntity.getDaysOverdue(), fineEntity.getFineAmount());

            log.debug("Fine details retrieved successfully");
            return ResponseEntity.ok(fine);
        } catch (Exception e) {
            log.error("Error occurred while retrieving fine details: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Sets the number of days after which fine should be collected.
     *
     * @param days The number of days for fine collection
     * @return ResponseEntity indicating the success of the operation
     */
    @Override
    public ResponseEntity<String> setDaysForFine(int days) {
        try {
            List<FineEntity> fineEntities = fineRepository.findAll();

            if (fineEntities.isEmpty()) {
                log.debug(FINE_DETAILS_NOT_FOUND);
                return ResponseEntity.notFound().build();
            }

            FineEntity fineEntity = fineEntities.get(0);
            fineEntity.setDaysOverdue(days);
            fineRepository.save(fineEntity);

            log.debug("Fine collection days updated successfully");
            return ResponseEntity.ok("Fine collection days updated successfully");
        } catch (Exception e) {
            log.error("Error occurred while setting fine collection days: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Sets the amount to be collected as fine.
     *
     * @param amount The amount to be set as fine
     * @return ResponseEntity indicating the success of the operation
     */
    @Override
    public ResponseEntity<String> setAmountForFine(float amount) {
        try {
            List<FineEntity> fineEntities = fineRepository.findAll();

            if (fineEntities.isEmpty()) {
                log.debug(FINE_DETAILS_NOT_FOUND);
                return ResponseEntity.notFound().build();
            }

            FineEntity fineEntity = fineEntities.get(0);
            fineEntity.setFineAmount(amount);
            fineRepository.save(fineEntity);

            log.debug("Fine amount updated successfully");
            return ResponseEntity.ok("Fine amount updated successfully");
        } catch (Exception e) {
            log.error("Error occurred while setting fine amount: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
