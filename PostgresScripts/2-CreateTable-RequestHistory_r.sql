CREATE TABLE request_history
(
id integer PRIMARY KEY,
user_Id integer,
transport_stop_id integer REFERENCES transport_stop (id) ,
datetime timestamp
);