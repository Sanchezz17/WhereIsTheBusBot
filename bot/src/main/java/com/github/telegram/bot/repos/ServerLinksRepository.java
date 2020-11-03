package com.github.telegram.bot.repos;

import com.github.telegram.bot.db.ServerLinks;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServerLinksRepository extends JpaRepository<ServerLinks, Integer> {
}
