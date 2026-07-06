DELETE FROM rentals;
DELETE FROM cars;

INSERT INTO cars(id, model, brand, type, inventory, daily_fee)
VALUES (1, 'M5', 'BMW', 'SEDAN', 1, 39.99),
       (2, 'RX', 'Lexus', 'SUV', 3, 49.99),
       (3, 'Civic', 'Honda', 'HATCHBACK', 5, 29.99);

SELECT setval('cars_id_seq', (SELECT MAX(id) FROM cars));

INSERT INTO rentals (id, rental_date, return_date, actual_return_date, car_id, user_id)
VALUES
    (1, '2026-05-20', '2026-05-25', NULL, 1, 1),
    (2, '2026-05-01', '2026-05-10', '2026-05-09', 2, 1),
    (3, '2026-05-15', '2026-06-01', NULL, 3, 2),
    (4, '2026-05-25', '2026-06-15', NULL, 1, 2);

SELECT setval('rentals_id_seq', (SELECT MAX(id) FROM rentals));