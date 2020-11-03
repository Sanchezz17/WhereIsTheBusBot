package com.github.telegram.bot.db;

import javax.persistence.*;

@Entity
@Table(name="favorite_request")
public class FavoriteRequest {
    @Id
    public int id;

    @Column(name = "user_id")
    public int userId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transport_stop_id")
    public TransportStop transportStop;
}
