package app.masterwork.simple.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ServerPlayer;

import app.masterwork.simple.skills.PlayerSkills;

@Mixin(ServerPlayer.class)
abstract class ServerPlayerExperienceMixin {
    @Inject(method = "giveExperiencePoints", at = @At("HEAD"))
    private void simple$awardBxpFromVanillaXp(int experiencePoints, CallbackInfo ci) {
        if (experiencePoints > 0) {
            PlayerSkills.awardGameplayBxp((ServerPlayer) (Object) this, experiencePoints);
        }
    }
}
