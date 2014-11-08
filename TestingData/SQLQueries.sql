CREATE TABLE users(
	user_id INT PRIMARY KEY,
	first_name VARCHAR(255),
	last_name VARCHAR(255)
);

CREATE TABLE hrecords(
    user_id INT REFERENCES users (user_id),
    age INT,
    weight INT
);

CREATE TABLE dummy(
    id NUMERIC(5),
    text VARCHAR(255)
);

INSERT INTO dummy (id, text) VALUES (0.1, 'd1');
INSERT INTO dummy (id, text) VALUES (1.1, 'd2');

INSERT INTO users (user_id, first_name, last_name) VALUES (0, 'andy', 'johnson');
INSERT INTO users (user_id, first_name, last_name) VALUES (1, 'betty', 'white');
INSERT INTO users (user_id, first_name, last_name) VALUES (2, 'carl', 'potter');
INSERT INTO users (user_id, first_name, last_name) VALUES (3, 'doug', 'johnson');
INSERT INTO users (user_id, first_name, last_name) VALUES (4, 'emma', 'biden');
INSERT INTO users (user_id, first_name, last_name) VALUES (5, 'fil', 'smith');
INSERT INTO users (user_id, first_name, last_name) VALUES (6, 'gorge', 'lee');
INSERT INTO users (user_id, first_name, last_name) VALUES (7, 'hanna', 'tsu');
INSERT INTO users (user_id, first_name, last_name) VALUES (8, 'ivy', 'hung');
INSERT INTO users (user_id, first_name, last_name) VALUES (9, 'jessica', 'lawrence');

INSERT INTO hrecords (user_id, age, weight) VALUES (0, 22, 180);
INSERT INTO hrecords (user_id, age, weight) VALUES (1, 22, 100);
INSERT INTO hrecords (user_id, age, weight) VALUES (2, 32, 178);
INSERT INTO hrecords (user_id, age, weight) VALUES (3, 42, 87);
INSERT INTO hrecords (user_id, age, weight) VALUES (4, 25, 120);
INSERT INTO hrecords (user_id, age, weight) VALUES (5, 43, 79);
INSERT INTO hrecords (user_id, age, weight) VALUES (6, 53, 95);
INSERT INTO hrecords (user_id, age, weight) VALUES (7, 25, 130);
INSERT INTO hrecords (user_id, age, weight) VALUES (8, 12, 145);
INSERT INTO hrecords (user_id, age, weight) VALUES (9, 43, 106);

explain (FORMAT JSON) select * from hrecords h, users u where h.user_id = u.user_id;

explain VERBOSE select * from hrecords h, users u where h.user_id = u.user_id;

explain (VERBOSE TRUE, FORMAT JSON) select * from hrecords h, users u where h.user_id = u.user_id;

explain (VERBOSE TRUE, FORMAT JSON) select avg(age) from hrecords h  // QueryPlan2_aggregation

explain (VERBOSE TRUE, FORMAT JSON) select h.age, avg(h.weight) from hrecords h group by h.age; // QueryPlan3_groupby