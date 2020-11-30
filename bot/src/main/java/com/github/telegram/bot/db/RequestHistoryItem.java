package com.github.telegram.bot.db;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "request_history")
public class RequestHistoryItem {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public int id;

    @Column(name = "user_id")
    public int userId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transport_stop_id")
    public TransportStop transportStop;

    @Column(name = "datetime")
    public Date datetime;
}
