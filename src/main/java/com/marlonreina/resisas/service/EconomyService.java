package com.marlonreina.resisas.service;

import com.marlonreina.resisas.dto.DailyRewardResult;
import com.marlonreina.resisas.model.EconomyAccount;
import com.marlonreina.resisas.repository.EconomyAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class EconomyService {

    public static final long DAILY_REWARD = 250L;
    private static final Duration DAILY_COOLDOWN = Duration.ofHours(24);

    private final EconomyAccountRepository repo;

    public EconomyService(EconomyAccountRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public EconomyAccount getOrCreate(String guildId, String userId) {
        return repo.findByGuildIdAndUserId(guildId, userId)
                .orElseGet(() -> repo.save(new EconomyAccount(null, guildId, userId, 0L, null)));
    }

    @Transactional
    public DailyRewardResult claimDaily(String guildId, String userId) {
        EconomyAccount account = getOrCreate(guildId, userId);
        OffsetDateTime now = OffsetDateTime.now();

        if (account.getLastDailyAt() != null) {
            OffsetDateTime nextClaimAt = account.getLastDailyAt().plus(DAILY_COOLDOWN);
            if (now.isBefore(nextClaimAt)) {
                return new DailyRewardResult(false, 0L, account.getBalance(),
                        Duration.between(now, nextClaimAt));
            }
        }

        account.setBalance(account.getBalance() + DAILY_REWARD);
        account.setLastDailyAt(now);
        repo.save(account);
        return new DailyRewardResult(true, DAILY_REWARD, account.getBalance(), Duration.ZERO);
    }

    @Transactional
    public EconomyAccount transfer(String guildId, String fromUserId, String toUserId, long amount) {
        EconomyAccount sender = getOrCreate(guildId, fromUserId);
        EconomyAccount receiver = getOrCreate(guildId, toUserId);

        if (sender.getBalance() < amount) {
            throw new IllegalArgumentException("Saldo insuficiente.");
        }

        sender.setBalance(sender.getBalance() - amount);
        receiver.setBalance(receiver.getBalance() + amount);
        repo.save(sender);
        repo.save(receiver);
        return sender;
    }

    public List<EconomyAccount> leaderboard(String guildId) {
        return repo.findTop10ByGuildIdOrderByBalanceDesc(guildId);
    }
}
