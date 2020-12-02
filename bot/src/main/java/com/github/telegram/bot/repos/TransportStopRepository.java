package com.github.telegram.bot.repos;

import com.github.telegram.bot.db.TransportStop;
import com.github.telegram.bot.models.Transport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransportStopRepository extends JpaRepository<TransportStop, Integer> {
    List<TransportStop> findByNameStartsWithAndTransportEquals(String namePrefix, Transport transport);
    List<TransportStop> findByNameAndTransport(String name, Transport transport);
}
