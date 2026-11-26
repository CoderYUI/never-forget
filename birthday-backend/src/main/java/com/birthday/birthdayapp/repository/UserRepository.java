package com.birthday.birthdayapp.repository;

import com.birthday.birthdayapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);


    @Query("SELECT u FROM User u WHERE EXTRACT(MONTH FROM u.dateOfBirth) = :month AND EXTRACT(DAY FROM u.dateOfBirth) = :day")
    List<User> findUsersByBirthdayMonthAndDay(@Param("month") int month, @Param("day") int day);

}
