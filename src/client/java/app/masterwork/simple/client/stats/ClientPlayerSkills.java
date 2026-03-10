package app.masterwork.simple.client.stats;

import java.util.LinkedHashMap;
import java.util.Map;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.resources.Identifier;

import app.masterwork.simple.skills.sync.PlayerSkillsSnapshotPayload;
import app.masterwork.simple.skills.sync.RequestPlayerSkillsPayload;
import app.masterwork.simple.skills.sync.SkillSnapshot;

public final class ClientPlayerSkills {
    private static Snapshot snapshot = Snapshot.EMPTY;
    private static boolean loading;
    private static boolean registered;

    private ClientPlayerSkills() {
    }

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;

        ClientPlayNetworking.registerGlobalReceiver(PlayerSkillsSnapshotPayload.TYPE, (payload, context) -> accept(payload));
        ClientPlayConnectionEvents.DISCONNECT.register((listener, client) -> clear());
    }

    public static void requestRefresh() {
        loading = true;

        if (ClientPlayNetworking.canSend(RequestPlayerSkillsPayload.TYPE)) {
            ClientPlayNetworking.send(new RequestPlayerSkillsPayload());
            return;
        }

        loading = false;
    }

    public static Snapshot snapshot() {
        return snapshot;
    }

    public static boolean isLoading() {
        return loading;
    }

    public static void clear() {
        snapshot = Snapshot.EMPTY;
        loading = false;
    }

    private static void accept(PlayerSkillsSnapshotPayload payload) {
        LinkedHashMap<Identifier, Boolean> unlockedBySkill = new LinkedHashMap<>();

        for (SkillSnapshot skill : payload.skills()) {
            unlockedBySkill.put(skill.skillId(), skill.unlocked());
        }

        snapshot = new Snapshot(payload.bXp(), payload.ep(), Map.copyOf(unlockedBySkill));
        loading = false;
    }

    public record Snapshot(int bXp, int ep, Map<Identifier, Boolean> unlockedBySkill) {
        private static final Snapshot EMPTY = new Snapshot(0, 0, Map.of());

        public Snapshot {
            bXp = Math.max(0, bXp);
            ep = Math.max(0, ep);
            unlockedBySkill = Map.copyOf(unlockedBySkill);
        }

        public boolean isUnlocked(Identifier skillId) {
            return unlockedBySkill.getOrDefault(skillId, false);
        }

        public int unlockedCount() {
            return (int) unlockedBySkill.values().stream()
                    .filter(Boolean::booleanValue)
                    .count();
        }
    }
}
