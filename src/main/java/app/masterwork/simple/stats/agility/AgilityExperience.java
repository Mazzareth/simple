package app.masterwork.simple.stats.agility;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stats;

public final class AgilityExperience {
    private static final int SCORE_PER_XP = 2_000;
    private static final int WALK_WEIGHT = 2;
    private static final int SPRINT_WEIGHT = 4;
    private static final int SWIM_WEIGHT = 6;
    private static final int CLIMB_WEIGHT = 8;

    private AgilityExperience() {
    }

    public static MovementSnapshot sample(ServerPlayer player) {
        ServerStatsCounter stats = player.getStats();
        return new MovementSnapshot(
                stats.getValue(Stats.CUSTOM, Stats.WALK_ONE_CM),
                stats.getValue(Stats.CUSTOM, Stats.SPRINT_ONE_CM),
                stats.getValue(Stats.CUSTOM, Stats.SWIM_ONE_CM),
                stats.getValue(Stats.CUSTOM, Stats.CLIMB_ONE_CM)
        );
    }

    public static SampleResult awardFromDelta(MovementSnapshot previous, MovementSnapshot current, int carryScore) {
        MovementSnapshot delta = current.deltaFrom(previous);
        int weightedScore = (delta.walkCm() * WALK_WEIGHT)
                + (delta.sprintCm() * SPRINT_WEIGHT)
                + (delta.swimCm() * SWIM_WEIGHT)
                + (delta.climbCm() * CLIMB_WEIGHT);
        int totalScore = Math.max(0, carryScore) + weightedScore;
        int xpAwarded = totalScore / SCORE_PER_XP;
        int newCarryScore = totalScore % SCORE_PER_XP;
        return new SampleResult(current, newCarryScore, xpAwarded);
    }

    public record MovementSnapshot(int walkCm, int sprintCm, int swimCm, int climbCm) {
        public MovementSnapshot deltaFrom(MovementSnapshot previous) {
            return new MovementSnapshot(
                    Math.max(0, walkCm - previous.walkCm),
                    Math.max(0, sprintCm - previous.sprintCm),
                    Math.max(0, swimCm - previous.swimCm),
                    Math.max(0, climbCm - previous.climbCm)
            );
        }
    }

    public record SampleResult(MovementSnapshot snapshot, int carryScore, int xpAwarded) {
    }
}
