package com.shrona.mommytalk.entitlement.infrastructure.jpa;

import com.shrona.mommytalk.entitlement.domain.UserEntitlement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserEntitlementJpaRepository extends JpaRepository<UserEntitlement, Long> {

}
