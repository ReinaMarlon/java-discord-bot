package com.marlonreina.resisas.repository;

import com.marlonreina.resisas.model.EconomyAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EconomyAccountRepository extends JpaRepository<EconomyAccount, Long> {

    Optional<EconomyAccount> findByGuildIdAndUserId(String guildId, String userId);

    List<EconomyAccount> findTop10ByGuildIdOrderByBalanceDesc(String guildId);
}
