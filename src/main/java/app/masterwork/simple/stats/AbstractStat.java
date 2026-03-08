package app.masterwork.simple.stats;

import java.util.Objects;
import java.util.function.UnaryOperator;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import app.masterwork.simple.Simple;

/**
 * Shared implementation for attachment-backed player stats.
 */
public abstract class AbstractStat<T> implements IStat<T> {
    private final Identifier id;
    private final Codec<T> codec;
    private final T defaultValue;
    private final AttachmentType<T> attachmentType;

    protected AbstractStat(String path, Codec<T> codec, T defaultValue) {
        this(Identifier.parse(Simple.MOD_ID + ":" + path), codec, defaultValue, true);
    }

    protected AbstractStat(Identifier id, Codec<T> codec, T defaultValue, boolean copyOnDeath) {
        this.id = Objects.requireNonNull(id, "id");
        this.codec = Objects.requireNonNull(codec, "codec");
        this.defaultValue = Objects.requireNonNull(defaultValue, "defaultValue");

        this.attachmentType = AttachmentRegistry.create(this.id, builder -> {
            builder.persistent(this.codec).initializer(() -> this.defaultValue);

            if (copyOnDeath) {
                builder.copyOnDeath();
            }
        });
    }

    @Override
    public final Identifier id() {
        return id;
    }

    @Override
    public final Codec<T> codec() {
        return codec;
    }

    @Override
    public final T defaultValue() {
        return defaultValue;
    }

    @Override
    public final AttachmentType<T> attachmentType() {
        return attachmentType;
    }

    @Override
    public T get(ServerPlayer player) {
        return onGet(player, target(player).getAttachedOrCreate(attachmentType));
    }

    @Override
    public void set(ServerPlayer player, T value) {
        AttachmentTarget target = target(player);
        T sanitized = sanitize(value);
        T oldValue = target.getAttachedOrCreate(attachmentType);

        if (Objects.equals(oldValue, sanitized)) {
            return;
        }

        target.setAttached(attachmentType, sanitized);
        onSet(player, oldValue, sanitized);
    }

    @Override
    public T modify(ServerPlayer player, UnaryOperator<T> modifier) {
        Objects.requireNonNull(modifier, "modifier");

        AttachmentTarget target = target(player);
        T oldValue = target.getAttachedOrCreate(attachmentType);
        T newValue = sanitize(modifier.apply(oldValue));

        if (!Objects.equals(oldValue, newValue)) {
            target.setAttached(attachmentType, newValue);
            onSet(player, oldValue, newValue);
        }

        return newValue;
    }

    @Override
    public void reset(ServerPlayer player) {
        set(player, defaultValue);
    }

    @Override
    public boolean has(ServerPlayer player) {
        return target(player).hasAttached(attachmentType);
    }

    protected T sanitize(T value) {
        return Objects.requireNonNull(value, "value");
    }

    protected T onGet(ServerPlayer player, T value) {
        return value;
    }

    protected void onSet(ServerPlayer player, T oldValue, T newValue) {
    }

    protected AttachmentTarget target(ServerPlayer player) {
        return (AttachmentTarget) Objects.requireNonNull(player, "player");
    }
}
