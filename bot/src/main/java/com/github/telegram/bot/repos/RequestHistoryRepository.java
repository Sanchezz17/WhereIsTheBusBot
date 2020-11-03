package com.github.telegram.bot.repos;

import com.github.telegram.bot.db.RequestHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequestHistoryRepository extends JpaRepository<RequestHistory, Integer> {
}
