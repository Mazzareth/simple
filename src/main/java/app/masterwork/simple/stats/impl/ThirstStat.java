package app.masterwork.simple.stats.impl;

import com.mojang.serialization.Codec;

import app.masterwork.simple.stats.AbstractStat;

public final class ThirstStat extends AbstractStat<Integer> {
    private static final int MIN_VALUE = 0;
    private static final int MAX_VALUE = 20;

    public ThirstStat() {
        super("thirst", Codec.INT, 20);
    }

    @Override
    protected Integer sanitize(Integer value) {
        int v = super.sanitize(value);
        return Math.max(MIN_VALUE, Math.min(MAX_VALUE, v));
    }
}
