package com.github.telegram.bot.db;

import com.github.telegram.bot.models.Right;

import javax.persistence.*;

@Entity
@Table(name = "user_right")
public class UserRight {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public int id;

    @Column(name = "user_name")
    public String username;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "right_id")
    public Right right;
}
