package com.github.telegram.bot.db;

import javax.persistence.*;

@Entity
@Table(name = "transport_stop")
public class TransportStop {
    @Id
    public int id;

    @Column(name = "name")
    public String name;

    @Column(name = "direction")
    public String direction;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "transport_type")
    public TransportType transportType;
}
