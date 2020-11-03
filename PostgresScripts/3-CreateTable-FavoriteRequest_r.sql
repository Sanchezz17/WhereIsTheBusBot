CREATE TABLE favorite_request
(
id integer PRIMARY KEY,
user_id integer,
transport_stop_id integer REFERENCES transport_stop (id)
);