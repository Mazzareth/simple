package app.masterwork.simple.stats.agility;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import app.masterwork.simple.stats.PlayerStats;

public final class AgilityEvents {
    private static boolean registered;

    private AgilityEvents() {
    }

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;
        ServerLivingEntityEvents.ALLOW_DAMAGE.register(AgilityEvents::allowDamage);
    }

    private static boolean allowDamage(LivingEntity entity, net.minecraft.world.damagesource.DamageSource source, float amount) {
        if (!(entity instanceof ServerPlayer player)) {
            return true;
        }

        AgilityData data = PlayerStats.agilityData(player);

        if (data.dodgeChance() <= 0.0D) {
            return true;
        }

        return player.getRandom().nextDouble() >= data.dodgeChance();
    }
}
