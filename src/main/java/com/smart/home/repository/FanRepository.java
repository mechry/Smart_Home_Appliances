package com.smart.home.repository;

import com.smart.home.domain.Fan;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FanRepository extends JpaRepository<Fan, Long> {
    List<Fan> findByRoomId(Long roomId);
}
