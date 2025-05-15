package com.example.app.repository;

import com.example.app.model.DbAccount;
import com.example.app.model.DbTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DbTransactionRepository extends JpaRepository<DbTransaction, Long> {
    List<DbTransaction> findByFromAccount(DbAccount fromAccount);
    List<DbTransaction> findByToAccount(DbAccount toAccount);
    List<DbTransaction> findByFromAccountOrToAccount(DbAccount fromAccount, DbAccount toAccount);

    @Query("SELECT t FROM DbTransaction t WHERE t.fromAccount.accountNumber = :accountNumber OR t.toAccount.accountNumber = :accountNumber ORDER BY t.timestamp DESC")
    List<DbTransaction> findAllByAccountNumber(@Param("accountNumber") String accountNumber);

    @Query("SELECT t FROM DbTransaction t JOIN t.fromAccount da JOIN da.user u WHERE u.username = :username ORDER BY t.timestamp DESC")
    List<DbTransaction> findAllTransactionsByUsernameFrom(@Param("username") String username);

    @Query("SELECT t FROM DbTransaction t JOIN t.toAccount da JOIN da.user u WHERE u.username = :username ORDER BY t.timestamp DESC")
    List<DbTransaction> findAllTransactionsByUsernameTo(@Param("username") String username);

    // More comprehensive query for all transactions related to a user's accounts
    @Query("SELECT t FROM DbTransaction t WHERE t.fromAccount.user.username = :username OR t.toAccount.user.username = :username ORDER BY t.timestamp DESC")
    List<DbTransaction> findAllTransactionsByUsername(@Param("username") String username);
}