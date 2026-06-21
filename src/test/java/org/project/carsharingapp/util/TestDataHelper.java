package org.project.carsharingapp.util;

import org.project.carsharingapp.model.user.Role;
import org.project.carsharingapp.model.user.User;

public class TestDataHelper {
    public static final String ADD_SCRIPT_PATH = "classpath:database/add-test-data.sql";
    public static final String DELETE_SCRIPT_PATH = "classpath:database/delete-test-data.sql";

    public static final String USER_RAW_PASSWORD = "testuser1";

    public static User createTestCustomer() {
        return new User()
            .setId(1L)
            .setEmail("test.user1@mail.com")
            .setPassword("$2a$12$KvTBaCc8tnqLRp3F0c1Bp.DZZYUf0TUmLdcNdnt/w2uPdQZ/5l1m6")
            .setFirstName("test")
            .setLastName("user1")
            .setRole(Role.CUSTOMER)
            .setDeleted(false);
    }
}
