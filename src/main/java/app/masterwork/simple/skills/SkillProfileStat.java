package app.masterwork.simple.skills;

import app.masterwork.simple.stats.AbstractStat;

public final class SkillProfileStat extends AbstractStat<SkillProfile> {
    public SkillProfileStat() {
        super("skill_profile", SkillProfile.CODEC, SkillProfile.ZERO);
    }

    @Override
    protected SkillProfile sanitize(SkillProfile value) {
        return SkillProgression.sanitize(value);
    }
}
