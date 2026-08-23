package com.smart.home.repository;

import com.smart.home.domain.Fan;
import org.springframework.stereotype.Repository;

@Repository
public interface FanRepository extends ApplianceRepository<Fan> {
}
