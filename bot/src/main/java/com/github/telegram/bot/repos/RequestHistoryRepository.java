package com.github.telegram.bot.repos;

import com.github.telegram.bot.db.RequestHistoryItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequestHistoryRepository extends JpaRepository<RequestHistoryItem, Integer> {
}
