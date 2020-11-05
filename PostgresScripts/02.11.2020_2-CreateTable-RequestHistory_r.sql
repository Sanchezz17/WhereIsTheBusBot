CREATE TABLE request_history
(
id                  integer     PRIMARY KEY                         NOT NULL,
user_Id             integer                                         NOT NULL,
transport_stop_id   integer     REFERENCES transport_stop (id)      NOT NULL,
datetime            timestamp                                       NOT NULL
);