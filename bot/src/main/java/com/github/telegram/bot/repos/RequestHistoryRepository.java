package com.github.telegram.bot.repos;

import com.github.telegram.bot.db.RequestHistoryItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RequestHistoryRepository extends JpaRepository<RequestHistoryItem, Integer> {
    @Query(value = "SELECT r FROM RequestHistoryItem r ORDER BY r.datetime DESC")
    @EntityGraph(value = "requestHistory", type = EntityGraph.EntityGraphType.LOAD)
    List<RequestHistoryItem> getHistorySortedByDateDesc(Pageable page);
}
