CREATE TABLE server_links
(
id                  integer                 PRIMARY KEY                         NOT NULL,
transport_stop_id   integer                 REFERENCES transport_stop (id)      NOT NULL,
link                CHARACTER VARYING(100)                                       NOT NULL
);