DELETE FROM payments;

INSERT INTO payments (id, status, type, rental_id, session_url, session_id, total)
VALUES
    (1, 'PENDING', 'PAYMENT', 1, 'testurl1', 'testid1', 359.91),
    (2, 'PAID', 'PAYMENT', 2, 'testurl2', 'testid2', 449.91),
    (3, 'PENDING', 'FINE', 2, 'testurl3', 'testid3', 64.99);

SELECT setval('payments_id_seq', (SELECT MAX(id) FROM payments));