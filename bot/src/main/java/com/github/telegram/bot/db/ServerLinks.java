package com.github.telegram.bot.db;

import javax.persistence.*;

@Entity
@Table(name = "server_links")
public class ServerLinks {
    @Id
    public int id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transport_stop_id")
    public TransportStop transportStop;

    @Column(name = "link")
    public String link;

}
