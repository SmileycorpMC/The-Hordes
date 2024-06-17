package net.smileycorp.hordes.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.smileycorp.hordes.hordeevent.capability.HordeEvent;
import net.smileycorp.hordes.hordeevent.capability.WorldDataHordes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class MixinEntity {
    
    @Shadow public World world;
    
    //load pre 1.5.0 horde data from capability
    @Inject(at = @At("HEAD"), method = "readFromNBT")
    public void load(NBTTagCompound nbt, CallbackInfo callback) {
        if (!(world instanceof WorldServer) |! (((Object)this) instanceof EntityPlayerMP)) return;
        if (!nbt.hasKey("ForgeCaps", 10)) return;
        NBTTagCompound caps = nbt.getCompoundTag("ForgeCaps");
        if (!caps.hasKey("hordes:hordeevent", 10)) return;
        WorldDataHordes data = WorldDataHordes.getData(world);
        HordeEvent event = data.getEvent((EntityPlayerMP) (Object)this);
        event.readFromNBT(caps.getCompoundTag("hordes:hordeevent"));
    }
    
}
