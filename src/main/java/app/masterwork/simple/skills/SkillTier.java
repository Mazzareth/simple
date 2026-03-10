package app.masterwork.simple.skills;

import java.util.Locale;

public enum SkillTier {
    COMMON(150, 0xFF5E9E8B),
    EXTRA(500, 0xFFB18C4C),
    UNIQUE(2_500, 0xFFC86A7A);

    private final int epValue;
    private final int accentColor;

    SkillTier(int epValue, int accentColor) {
        this.epValue = epValue;
        this.accentColor = accentColor;
    }

    public int epValue() {
        return epValue;
    }

    public int accentColor() {
        return accentColor;
    }

    public String translationKey() {
        return "skill.simple.tier." + name().toLowerCase(Locale.ROOT);
    }
}
