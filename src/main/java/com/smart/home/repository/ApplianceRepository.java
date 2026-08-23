package com.smart.home.repository;

import com.smart.home.domain.Appliance;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplianceRepository<T extends Appliance> extends JpaRepository<T, Long> {
    List<T> findByRoomId(Long roomId);
}
