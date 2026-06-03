package net.smileycorp.hordes.mixin;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.stats.StatisticsManagerServer;
import net.minecraft.world.WorldServer;
import net.smileycorp.hordes.hordeevent.capability.HordeEvent;
import net.smileycorp.hordes.hordeevent.capability.WorldDataHordes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayerMP.class)
public abstract class MixinEntityPlayerMP {

    @Shadow public abstract WorldServer getServerWorld();

    @Shadow public abstract StatisticsManagerServer getStatFile();

    @Inject(at = @At("HEAD"), method = "writeEntityToNBT")
    public void hordes$writeEntityToNBT(NBTTagCompound nbt, CallbackInfo callback) {
        nbt.setInteger("playtime", getStatFile().readStat(StatList.PLAY_ONE_MINUTE));
        HordeEvent event = WorldDataHordes.getData(getServerWorld()).getEvent((EntityPlayerMP) (Object)this);
        if (event != null) nbt.setInteger("next_horde_day", event.getNextDay());
    }


}
