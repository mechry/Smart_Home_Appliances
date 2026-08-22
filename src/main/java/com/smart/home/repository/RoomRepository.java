package com.smart.home.repository;

import com.smart.home.domain.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {
    boolean existsByNameIgnoreCase(String name);
}
