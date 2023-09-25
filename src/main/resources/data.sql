CREATE TABLE IF NOT EXISTS customers(
   id   INTEGER PRIMARY KEY AUTO_INCREMENT,
   name VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS accounts(
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    customer_id INTEGER NOT NULL,
    balance DECIMAL(9, 2) NOT NULL DEFAULT 0,
    FOREIGN KEY (customer_id) REFERENCES customers(id)
);

CREATE TABLE IF NOT EXISTS transactions(
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    sender_account_id INTEGER NOT NULL,
    FOREIGN KEY (sender_account_id) REFERENCES customers(id),
    receiver_account_id INTEGER NOT NULL,
    FOREIGN KEY (receiver_account_id) REFERENCES customers(id),
    amount DECIMAL(9, 2) NOT NULL,
    transaction_details VARCHAR(255)
);

INSERT INTO customers(name) VALUES ('Arisha Barron');
INSERT INTO customers(name) VALUES ('Branden Gibson');
INSERT INTO customers(name) VALUES ('Rhonda Church');
INSERT INTO customers(name) VALUES ('Georgina Hazel');
INSERT INTO customers(name) VALUES ('Judah Parham');
INSERT INTO accounts(customer_id, balance) VALUES (1, 520);
INSERT INTO accounts(customer_id, balance) VALUES (1, 5520);
INSERT INTO accounts(customer_id, balance) VALUES (2, 800);
INSERT INTO accounts(customer_id, balance) VALUES (3, 8400);
INSERT INTO accounts(customer_id, balance) VALUES (4, 80);
INSERT INTO accounts(customer_id, balance) VALUES (5, 8999);
INSERT INTO transactions(sender_account_id, amount, receiver_account_id, transaction_details) VALUES (4,20,1, 'FROM Georgina Hazel 20 TO Arisha Barron');
INSERT INTO transactions(sender_account_id, amount, receiver_account_id, transaction_details) VALUES (2,20,4, 'FROM Branden Gibson 20 TO Georgina Hazel');
INSERT INTO transactions(sender_account_id, amount, receiver_account_id, transaction_details) VALUES (2,800,3,'FROM Branden Gibson 800 TO Rhonda Church'); 