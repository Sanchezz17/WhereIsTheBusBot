CREATE TABLE transport_stop
(
id              integer                 PRIMARY KEY     NOT NULL,
name            CHARACTER VARYING(100)                   NOT NULL,
direction       CHARACTER VARYING(100),      
transport_type  smallint                                NOT NULL
);