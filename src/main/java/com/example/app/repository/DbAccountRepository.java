package com.example.app.repository;

import com.example.app.model.DbAccount;
import com.example.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DbAccountRepository extends JpaRepository<DbAccount, Long> {
    Optional<DbAccount> findByAccountNumber(String accountNumber);
    List<DbAccount> findByUser(User user);
    boolean existsByAccountNumber(String accountNumber);

    @Query("SELECT dba FROM DbAccount dba JOIN dba.user u WHERE u.username = :username")
    List<DbAccount> findByUserUsername(@Param("username") String username);
}