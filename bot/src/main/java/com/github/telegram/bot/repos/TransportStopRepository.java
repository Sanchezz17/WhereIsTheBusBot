package com.github.telegram.bot.repos;

import com.github.telegram.bot.db.TransportStop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransportStopRepository extends JpaRepository<TransportStop, Integer> {
    List<TransportStop> findByNameStartsWith(String namePrefix);
}
