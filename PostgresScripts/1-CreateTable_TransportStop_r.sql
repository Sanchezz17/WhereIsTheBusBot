CREATE TABLE transport_stop
(
id              integer                 PRIMARY KEY     NOT NULL,
name            CHARACTER VARYING(50)                   NOT NULL,
direction       CHARACTER VARYING(50),      
transport_type  smallint                                NOT NULL
);