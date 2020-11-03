package com.github.telegram.bot.repos;

import com.github.telegram.bot.db.FavoriteRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteRequestRepository extends JpaRepository<FavoriteRequest, Integer> {
}
