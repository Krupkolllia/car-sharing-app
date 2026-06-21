package org.project.carsharingapp.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.project.carsharingapp.util.TestDataHelper.ADD_SCRIPT_PATH;
import static org.project.carsharingapp.util.TestDataHelper.DELETE_SCRIPT_PATH;
import static org.project.carsharingapp.util.TestDataHelper.createTestCustomer;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.project.carsharingapp.model.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @Sql(scripts = ADD_SCRIPT_PATH, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = DELETE_SCRIPT_PATH, executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("""
        findByEmail method with email of existing User in database
        should return not empty Optional<User> with 1 User with
        requested email
        """)
    void findByEmail_WithValidEmail_ShouldReturnOptionalWithUser() {
        // Given
        User expected = createTestCustomer();

        // When
        Optional<User> actual = userRepository.findByEmail(expected.getEmail());

        // Then
        assertThat(actual).isPresent().get().isEqualTo(expected);
    }

    @Test
    @Sql(scripts = ADD_SCRIPT_PATH, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = DELETE_SCRIPT_PATH, executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("""
        findByEmail method with email of non-existing User in database
        should return empty Optional<User>
        """)
    void findByEmail_WithInvalidEmail_ShouldReturnEmptyOptional() {
        // Given
        String invalidEmail = "invalidEmail@mail.com";

        // When
        Optional<User> actual = userRepository.findByEmail(invalidEmail);

        // Then
        assertThat(actual).isEmpty();
    }

    @Test
    @Sql(scripts = ADD_SCRIPT_PATH, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = DELETE_SCRIPT_PATH, executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("""
        existsByEmail method with email of existing User
        in database should return boolean true
        """)
    void existsByEmail_WithValidEmail_ShouldReturnBooleanTrue() {
        // Given
        String email = createTestCustomer().getEmail();

        // When
        boolean actual = userRepository.existsByEmail(email);

        // Then
        assertThat(actual).isTrue();
    }

    @Test
    @Sql(scripts = ADD_SCRIPT_PATH, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = DELETE_SCRIPT_PATH, executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("""
        existsByEmail method with email of non-existing
        User in database should return boolean false
        """)
     void existsByEmail_WithInvalidEmail_ShouldReturnBooleanFalse() {
        // Given
        String invalidEmail = "invalidEmail@mail.com";

        // When
        boolean actual = userRepository.existsByEmail(invalidEmail);

        // Then
        assertThat(actual).isFalse();
    }
}
