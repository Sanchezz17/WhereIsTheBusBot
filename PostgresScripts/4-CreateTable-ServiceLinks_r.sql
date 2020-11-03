CREATE TABLE server_links
(
id integer PRIMARY KEY,
transport_stop_id integer REFERENCES transport_stop (id),
link CHARACTER VARYING(50)
);