DELETE FROM users;
INSERT INTO users (id, email, password, first_name, last_name, role)
VALUES (1, 'test.user@mail.com', '$2a$12$Bx.Pdcm6JggueZYewe1lC.1xWBtMr85se/AnlW4MujOefLH2izXoi',
        'test', 'user', 'CUSTOMER'),
       (2, 'test.manager@mail.com', '$2a$12$vfXgg4N72YCshq0/yEmAEe/LmF7qbpFhP3UgZarqI90bb4OLTZgiG',
        'test', 'manager', 'MANAGER');

SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));

DELETE FROM cars;
INSERT INTO cars(id, model, brand, type, inventory, daily_fee)
VALUES (1, 'M5', 'BMW', 'SEDAN', 1, 39.99),
       (2, 'RX', 'Lexus', 'SUV', 3, 49.99),
       (3, 'Civic', 'Honda', 'HATCHBACK', 5, 29.99);

SELECT setval('cars_id_seq', (SELECT MAX(id) FROM cars));
