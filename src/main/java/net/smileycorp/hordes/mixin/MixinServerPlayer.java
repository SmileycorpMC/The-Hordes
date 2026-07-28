package net.smileycorp.hordes.mixin;


import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stats;
import net.smileycorp.hordes.hordeevent.Playtime;
import net.smileycorp.hordes.hordeevent.capability.HordeEvent;
import net.smileycorp.hordes.hordeevent.capability.HordeSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayer implements Playtime {

	@Unique
	private long playtime = 0;

	@Shadow public abstract ServerStatsCounter getStats();

	@Shadow public abstract ServerLevel serverLevel();

	@Inject(at = @At("HEAD"), method = "addAdditionalSaveData")
	public void hordes$addAdditionalSaveData(CompoundTag nbt, CallbackInfo callback) {
		nbt.putLong("playtime", playtime == -1 ? getStats().getValue(Stats.CUSTOM.get(Stats.PLAY_TIME)) : playtime);
		HordeEvent event = HordeSavedData.getData(serverLevel()).getEvent((ServerPlayer) (Object)this);
		if (event != null) nbt.putInt("next_horde_day", event.getNextDay());
	}

	@Inject(at = @At("HEAD"), method = "readAdditionalSaveData")
	public void hordes$readAdditionalSaveData(CompoundTag nbt, CallbackInfo callback) {
		playtime = nbt.contains("playtime") ? nbt.getLong("playtime") : getStats().getValue(Stats.CUSTOM.get(Stats.PLAY_TIME));
	}

	@Override
	public long getPlaytime() {
		return playtime;
	}

	@Override
	public void setPlaytime(long playtime) {
		this.playtime = playtime;
	}
	
}
