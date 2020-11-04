package com.github.telegram.bot.repos;

import com.github.telegram.bot.db.ServerLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServerLinksRepository extends JpaRepository<ServerLink, Integer> {
    ServerLink findFirstByTransportStop_Id(int id);
}
