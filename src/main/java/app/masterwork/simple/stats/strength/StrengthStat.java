package app.masterwork.simple.stats.strength;

import com.mojang.serialization.Codec;

import app.masterwork.simple.stats.AbstractStat;

public final class StrengthStat extends AbstractStat<Integer> {
    private static final int MIN_VALUE = 0;
    private static final int MAX_VALUE = 200;

    public StrengthStat() {
        super("strength", Codec.INT, 0);
    }

    @Override
    protected Integer sanitize(Integer value) {
        int v = super.sanitize(value);
        return Math.max(MIN_VALUE, Math.min(MAX_VALUE, v));
    }
}
