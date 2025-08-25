package com.moyeorak.auth_service.repository;

import com.moyeorak.auth_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);

    List<User> findByRegionId(Long regionId);

    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);

}