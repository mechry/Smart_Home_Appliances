package com.smart.home.repository;

import com.smart.home.domain.AirConditioner;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AirConditionerRepository extends JpaRepository<AirConditioner, Long> {
    List<AirConditioner> findByRoomId(Long roomId);
}
