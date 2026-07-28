package net.smileycorp.hordes.mixin;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.stats.StatisticsManagerServer;
import net.minecraft.world.WorldServer;
import net.smileycorp.hordes.hordeevent.Playtime;
import net.smileycorp.hordes.hordeevent.capability.HordeEvent;
import net.smileycorp.hordes.hordeevent.capability.WorldDataHordes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayerMP.class)
public abstract class MixinEntityPlayerMP implements Playtime {

    @Unique
    private long playtime = 0;

    @Shadow public abstract WorldServer getServerWorld();

    @Shadow public abstract StatisticsManagerServer getStatFile();

    @Inject(at = @At("HEAD"), method = "writeEntityToNBT")
    public void hordes$writeEntityToNBT(NBTTagCompound nbt, CallbackInfo callback) {
        nbt.setLong("playtime", playtime == -1 ? getStatFile().readStat(StatList.PLAY_ONE_MINUTE) : playtime);
        HordeEvent event = WorldDataHordes.getData(getServerWorld()).getEvent((EntityPlayerMP) (Object)this);
        if (event != null) nbt.setInteger("next_horde_day", event.getNextDay());
    }

    @Inject(at = @At("HEAD"), method = "readEntityFromNBT")
    public void hordes$readEntityFromNBT(NBTTagCompound nbt, CallbackInfo callback) {
        playtime = nbt.hasKey("playtime") ? nbt.getLong("playtime") : getStatFile().readStat(StatList.PLAY_ONE_MINUTE);
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
