DELETE FROM users;
INSERT INTO users (id, email, password, first_name, last_name, role)
VALUES (1, 'test.user1@mail.com', '$2a$12$TAdN8UgihLS/va7duaWPpuMiwNKY9osRtyLtMQhOqUtIjSkg0AMfK',
        'test', 'user1', 'CUSTOMER'),
       (2, 'test.user2@mail.com', '$2a$12$oYswZ6gUaAkZ.XsupQcVx.gzKGrUzVzsFMnOGKvFB2HieSgrLoYSi',
        'test', 'user2', 'CUSTOMER'),
       (3, 'test.manager1@mail.com', '$2a$12$qKflh2qMWNcDITE/LS3XbOM55ijO1M16.tkgZyrfESWiHTmM1aNni',
        'test', 'manager1', 'MANAGER');

SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));

DELETE FROM cars;
INSERT INTO cars(id, model, brand, type, inventory, daily_fee)
VALUES (1, 'M5', 'BMW', 'SEDAN', 1, 39.99),
       (2, 'RX', 'Lexus', 'SUV', 3, 49.99),
       (3, 'Civic', 'Honda', 'HATCHBACK', 5, 29.99);

SELECT setval('cars_id_seq', (SELECT MAX(id) FROM cars));
