package app.masterwork.simple.stats;

import java.util.function.UnaryOperator;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

/**
 * Contract for a player stat backed by a Fabric data attachment.
 */
public interface IStat<T> {
    Identifier id();

    Codec<T> codec();

    T defaultValue();

    AttachmentType<T> attachmentType();

    T get(ServerPlayer player);

    void set(ServerPlayer player, T value);

    T modify(ServerPlayer player, UnaryOperator<T> modifier);

    void reset(ServerPlayer player);

    boolean has(ServerPlayer player);
}
