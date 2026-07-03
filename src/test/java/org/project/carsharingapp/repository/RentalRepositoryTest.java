package org.project.carsharingapp.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.project.carsharingapp.util.TestDataHelper.ADD_SCRIPT_PATH;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

import java.util.List;
import java.util.Optional;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.project.carsharingapp.model.rental.Rental;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class RentalRepositoryTest {
    
    @Autowired
    private RentalRepository rentalRepository;

    @Test
    @Sql(scripts = ADD_SCRIPT_PATH, executionPhase = BEFORE_TEST_METHOD)
    @DisplayName("""
        findAllByFilters method with null userId and null isActive
        should return all rentals
        """)
    void findAllByFilters_WithNullUserIdAndNullIsActive_ShouldReturnAllRentals() {
        // Given
        List<Rental> expected = rentalRepository.findAll();
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Rental> actual = rentalRepository.findAllByFilters(null, null, pageable);

        // Then
        assertThat(actual.getContent())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    @Sql(scripts = ADD_SCRIPT_PATH, executionPhase = BEFORE_TEST_METHOD)
    @DisplayName("""
        findAllByFilters method with specific userId and null isActive
        should return only that user's rentals
        """)
    void findAllByFilters_WithSpecificUserId_ShouldReturnOnlyThatUsersRentals() {
        // Given
        List<Rental> allRentals = rentalRepository.findAll();
        Long userId = allRentals.get(0).getUser().getId();

        List<Rental> expected = allRentals.stream()
            .filter(r -> r.getUser().getId().equals(userId))
            .toList();

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Rental> actual = rentalRepository.findAllByFilters(userId, null, pageable);

        // Then
        assertThat(actual.getContent())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    @Sql(scripts = ADD_SCRIPT_PATH, executionPhase = BEFORE_TEST_METHOD)
    @DisplayName("""
        findAllByFilters method with null userId and isActive true
        should return only rentals not yet returned
        """)
    void findAllByFilters_WithIsActiveTrue_ShouldReturnOnlyActiveRentals() {
        // Given
        List<Rental> allRentals = rentalRepository.findAll();
        List<Rental> expected = allRentals.stream()
            .filter(r -> r.getActualReturnDate() == null)
            .toList();

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Rental> actual = rentalRepository.findAllByFilters(null, true, pageable);

        // Then
        assertThat(actual.getContent())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    @Sql(scripts = ADD_SCRIPT_PATH, executionPhase = BEFORE_TEST_METHOD)
    @DisplayName("""
        findAllByFilters method with null userId and isActive false
        should return only returned rentals
        """)
    void findAllByFilters_WithIsActiveFalse_ShouldReturnOnlyReturnedRentals() {
        // Given
        List<Rental> allRentals = rentalRepository.findAll();

        List<Rental> expected = allRentals.stream()
            .filter(r -> r.getActualReturnDate() != null)
            .toList();

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Rental> actual = rentalRepository.findAllByFilters(null, false, pageable);

        // Then
        assertThat(actual.getContent())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    @Sql(scripts = ADD_SCRIPT_PATH, executionPhase = BEFORE_TEST_METHOD)
    @DisplayName("""
        findAllByFilters method with specific userId and isActive true
        should return only that user's active rentals
        """)
    void findAllByFilters_WithUserIdAndIsActiveTrue_ShouldReturnOnlyMatchingBoth() {
        // Given
        List<Rental> allRentals = rentalRepository.findAll();
        Long userId = allRentals.get(0).getUser().getId();

        List<Rental> expected = allRentals.stream()
            .filter(r -> r.getUser().getId().equals(userId))
            .filter(r -> r.getActualReturnDate() == null)
            .toList();

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Rental> actual = rentalRepository.findAllByFilters(userId, true, pageable);

        // Then
        assertThat(actual.getContent())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    @Sql(scripts = ADD_SCRIPT_PATH, executionPhase = BEFORE_TEST_METHOD)
    @DisplayName("""
        findAllByFilters method with specific userId and isActive false
        should return only that user's returned rentals
    """)
    void findAllByFilters_WithUserIdAndIsActiveFalse_ShouldReturnOnlyMatchingBoth() {
        // Given
        List<Rental> allRentals = rentalRepository.findAll();

        Long userId = allRentals.stream()
            .filter(r -> r.getActualReturnDate() != null)
            .findFirst()
            .orElseThrow()
            .getUser()
            .getId();

        List<Rental> expected = allRentals.stream()
            .filter(r -> r.getUser().getId().equals(userId))
            .filter(r -> r.getActualReturnDate() != null)
            .toList();

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Rental> actual = rentalRepository.findAllByFilters(userId, false, pageable);

        // Then
        assertThat(actual.getContent())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrderElementsOf(expected);
    }
    
    @Test
    @Sql(scripts = ADD_SCRIPT_PATH, executionPhase = BEFORE_TEST_METHOD)
    @DisplayName("""
        findByIdWithCar method with id of existing rental should
        return not empty Optional of rental with not empty Car
        """)
    void findByIdWithCar_WithValidId_ShouldReturnNotEmptyOptionalOfRental() {
        // Given
        Rental expected = rentalRepository.findAll().get(0);
        
        // When
        Optional<Rental> actual = rentalRepository.findByIdWithCar(expected.getId());
        
        // Then
        assertThat(actual).isPresent();
        assertThat(actual).get()
            .usingRecursiveComparison()
            .isEqualTo(expected);
        assertThat(Hibernate.isInitialized(actual.get().getCar())).isTrue();
    }

    @Test
    @Sql(scripts = ADD_SCRIPT_PATH, executionPhase = BEFORE_TEST_METHOD)
    @DisplayName("""
        findByIdWithCar method with id of non-existing
        rental should return empty Optional of rental
        """)
    void findByIdWithCar_WithInvalidId_ShouldReturnEmptyOptionalOfRental() {
        // Given
        Long invalidId = 404L;

        // When
        Optional<Rental> actual = rentalRepository.findByIdWithCar(invalidId);

        // Then
        assertThat(actual).isEmpty();
    }

}
