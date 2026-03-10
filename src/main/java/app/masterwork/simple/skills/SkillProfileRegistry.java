package app.masterwork.simple.skills;

public final class SkillProfileRegistry {
    public static final SkillProfileStat PROFILE = new SkillProfileStat();

    private SkillProfileRegistry() {
    }

    public static void bootstrap() {
        // Accessing this class triggers static registration.
    }
}
