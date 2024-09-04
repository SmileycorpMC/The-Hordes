package net.smileycorp.hordes.mixin;


import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.stats.ServerStatisticsManager;
import net.minecraft.stats.Stats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class MixinServerPlayerEntity {
	
	@Shadow public abstract ServerStatisticsManager getStats();
	
	@Inject(at = @At("HEAD"), method = "addAdditionalSaveData")
	public void addAdditionalSaveData(CompoundNBT nbt, CallbackInfo callback) {
		nbt.putInt("playtime", getStats().getValue(Stats.CUSTOM.get(Stats.PLAY_ONE_MINUTE)));
	}
	
}
