CREATE TABLE favorite_request
(
id                  integer     PRIMARY KEY                         NOT NULL,
user_id             integer                                         NOT NULL,
transport_stop_id   integer     REFERENCES transport_stop (id)      NOT NULL
);