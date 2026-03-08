package app.masterwork.simple.stats;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public final class AttributeEffectHelper {
    private static final double EPSILON = 1.0E-9D;

    private AttributeEffectHelper() {
    }

    public static void applyModifier(ServerPlayer player, Holder<Attribute> attribute, Identifier modifierId, double amount, AttributeModifier.Operation operation) {
        AttributeInstance instance = player.getAttribute(attribute);

        if (instance == null) {
            return;
        }

        if (Math.abs(amount) <= EPSILON) {
            instance.removeModifier(modifierId);
            return;
        }

        instance.addOrUpdateTransientModifier(new AttributeModifier(modifierId, amount, operation));
    }
}
