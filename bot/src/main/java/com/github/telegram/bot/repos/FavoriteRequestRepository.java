package com.github.telegram.bot.repos;

import com.github.telegram.bot.db.FavoriteRequest;
import com.github.telegram.bot.db.TransportStop;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FavoriteRequestRepository extends JpaRepository<FavoriteRequest, Integer> {
    @EntityGraph(value = "favoriteRequest", type = EntityGraph.EntityGraphType.LOAD)
    List<FavoriteRequest> findByUserId(int userId);

    FavoriteRequest findByTransportStopAndUserId(TransportStop transportStop, int userId);

    void removeByTransportStopAndUserId(TransportStop transportStop, int userId);
}
