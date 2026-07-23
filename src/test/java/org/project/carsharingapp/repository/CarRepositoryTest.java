package org.project.carsharingapp.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.project.carsharingapp.util.TestDataHelper.ADD_SCRIPT_PATH;
import static org.project.carsharingapp.util.TestDataHelper.CAR_ID;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.project.carsharingapp.model.car.Car;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class CarRepositoryTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private CarRepository carRepository;

    @Test
    @Sql(scripts = ADD_SCRIPT_PATH, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
    @DisplayName("""
        increaseInventory method with id of existing car
        should increase car inventory by 1 and return int 1 (affected rows)
        """)
    void increaseInventory_WithValidId_ShouldIncreaseByOneAndReturnIntOne() {
        // Given
        Car car = carRepository.findById(CAR_ID).orElseThrow();
        Integer oldInventoryValue = car.getInventory();

        // When
        int actual = carRepository.increaseInventory(car.getId());
        Car updatedCar = carRepository.findById(CAR_ID).orElseThrow();

        // Then
        assertThat(actual).isOne();
        assertThat(updatedCar.getInventory()).isEqualTo(oldInventoryValue + 1);
    }

    @Test
    @Sql(scripts = ADD_SCRIPT_PATH, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
    @DisplayName("""
        increaseInventory method with id of non-existing car
        should return int 0 (affected rows)
        """)
    void increaseInventory_WithInvalidId_ShouldReturnIntZero() {
        // Given
        Long invalidId = 404L;

        // When
        int actual = carRepository.increaseInventory(invalidId);

        // Then
        assertThat(actual).isZero();
    }

    @Test
    @Sql(scripts = ADD_SCRIPT_PATH, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
    @DisplayName("""
        decreaseInventory method with id of existing car
        and car inventory > 0 should decrease inventory by 1
        and return int 1 (affected rows)
        """)
    void decreaseInventory_ValidCase_ShouldDecreaseByOneAndReturnIntOne() {
        // Given
        Car car = carRepository.findById(CAR_ID).orElseThrow();
        Integer oldInventoryValue = car.getInventory();

        // When
        int actual = carRepository.decreaseInventory(car.getId());
        entityManager.refresh(car);

        // Then
        assertThat(actual).isOne();
        assertThat(car.getInventory()).isEqualTo(oldInventoryValue - 1);
    }

    @Test
    @Sql(scripts = ADD_SCRIPT_PATH, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
    @DisplayName("""
        decreaseInventory method with id of existing car
        but car inventory is exactly 0 should not change
        inventory and return int 0 (affected rows)
        """)
    void decreaseInventory_WithZeroInventory_ShouldReturnZeroAndNotChangeInventory() {
        // Given
        Car car = carRepository.findById(CAR_ID).orElseThrow();
        car.setInventory(0);
        carRepository.save(car);
        carRepository.flush();

        // When
        int actual = carRepository.decreaseInventory(car.getId());
        entityManager.refresh(car);

        // Then
        assertThat(actual).isZero();
        assertThat(car.getInventory()).isZero();
    }

    @Test
    @Sql(scripts = ADD_SCRIPT_PATH, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
    @DisplayName("""
        decreaseInventory method with id of non-existing car
        should return int 0 (affected rows)
        """)
    void decreaseInventory_WithInvalidId_ShouldReturnIntZero() {
        // Given
        Long invalidId = 404L;

        // When
        int actual = carRepository.decreaseInventory(invalidId);

        // Then
        assertThat(actual).isZero();
    }

}
