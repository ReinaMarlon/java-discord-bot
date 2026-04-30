package com.marlonreina.resisas.dto;

import java.time.Duration;

public record DailyRewardResult(boolean claimed,
                                long reward,
                                long balance,
                                Duration remaining) {
}
