package org.example.clearsolutionstest.repository;

import org.example.clearsolutionstest.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    @Query("select u from User u where u.birthDate >= :from and u.birthDate <= :to")
    List<User> getAllByBirthDateRange(@Param("from") LocalDate from, @Param("to") LocalDate to, Pageable pageable);

    @Query("select u from User u where u.email = :email")
    Optional<User> findByEmail(@Param("email") String email);

}
