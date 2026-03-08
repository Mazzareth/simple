package app.masterwork.simple.stats.xp;

public enum XpRewardActivity {
    BLOCK_BREAK("block_break"),
    SMELTING("smelting");

    private final String directory;

    XpRewardActivity(String directory) {
        this.directory = directory;
    }

    public String directory() {
        return directory;
    }

    public String resourcePath() {
        return "xp_rewards/" + directory;
    }
}
