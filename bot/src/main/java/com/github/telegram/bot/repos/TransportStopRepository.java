package com.github.telegram.bot.repos;

import com.github.telegram.bot.db.TransportStop;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransportStopRepository extends JpaRepository<TransportStop, Integer> {
}
