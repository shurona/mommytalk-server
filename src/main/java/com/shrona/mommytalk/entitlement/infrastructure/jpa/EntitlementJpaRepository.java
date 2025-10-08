package com.shrona.mommytalk.entitlement.infrastructure.jpa;

import com.shrona.mommytalk.entitlement.domain.Entitlement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EntitlementJpaRepository extends JpaRepository<Entitlement, Long> {

}
