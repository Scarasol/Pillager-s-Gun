package com.scarasol.pillagers_gun.util;

import net.minecraft.util.RandomSource;

import java.util.List;
import java.util.Optional;

public final class WeightedRandom {
    private WeightedRandom() {
    }

    public record Entry<T>(T value, double weight) {
    }

    public static <T> Entry<T> entry(T value, double weight) {
        return new Entry<>(value, weight);
    }

    public static <T> Optional<T> pick(RandomSource random, List<Entry<T>> entries) {
        double totalWeight = 0;
        for (Entry<T> entry : entries) {
            if (isUsableWeight(entry.weight())) {
                totalWeight += entry.weight();
            }
        }
        if (totalWeight <= 0) {
            return Optional.empty();
        }

        double weight = random.nextDouble() * totalWeight;
        T fallback = null;
        for (Entry<T> entry : entries) {
            if (!isUsableWeight(entry.weight())) {
                continue;
            }
            fallback = entry.value();
            weight -= entry.weight();
            if (weight < 0) {
                return Optional.of(entry.value());
            }
        }
        return Optional.ofNullable(fallback);
    }

    private static boolean isUsableWeight(double weight) {
        return weight > 0 && Double.isFinite(weight);
    }
}
