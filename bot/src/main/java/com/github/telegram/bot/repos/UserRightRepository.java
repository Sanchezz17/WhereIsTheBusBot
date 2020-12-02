package com.github.telegram.bot.repos;

import com.github.telegram.bot.db.UserRight;
import com.github.telegram.bot.models.Right;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRightRepository extends JpaRepository<UserRight, Integer> {
    boolean existsByUsernameAndRight(String username, Right right);
}
