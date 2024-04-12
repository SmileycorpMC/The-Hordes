package net.smileycorp.hordes.mixin;


import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayer {
	
	@Shadow public abstract ServerStatsCounter getStats();
	
	@Inject(at = @At("HEAD"), method = "addAdditionalSaveData")
	public void addAdditionalSaveData(CompoundTag nbt, CallbackInfo callback) {
		nbt.putInt("playtime", getStats().getValue(Stats.CUSTOM.get(Stats.PLAY_TIME)));
	}
	
}
