package com.example.hackathon2024.repository;

import com.example.hackathon2024.model.User;
import jakarta.transaction.Transactional;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends CrudRepository<User, Long> {
    @Transactional
    @Modifying
    //increasing message counter in db after every call if user was already created
    @Query(value =
            "UPDATE users t SET t.msg_numb = t.msg_numb + 1 " +
                    "WHERE t.id IS NOT NULL AND t.id = :id", nativeQuery = true)
    void updateMsgNumberByUserId(@Param("id") long id);

    @Transactional
    @Modifying
    @Query(value =
            "UPDATE users t SET t.percentage = :percentage " +
                    "WHERE t.id IS NOT NULL AND t.id = :id", nativeQuery = true)
    void updatePercentageById(@Param("id") long id, @Param("percentage") double percentage);

    @NotNull
    Iterable<User> findAll();
}