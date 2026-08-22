package com.smart.home.repository;

import com.smart.home.domain.Light;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LightRepository extends JpaRepository<Light, Long> {
    List<Light> findByRoomId(Long roomId);
}
