package app.masterwork.simple.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;

@Mixin(Slot.class)
public interface SlotAccessor {
    @Accessor("container")
    Container simple$getContainer();
}
