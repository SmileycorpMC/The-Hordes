package net.smileycorp.hordes.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.smileycorp.hordes.common.mixinutils.ChatName;
import net.smileycorp.hordes.hordeevent.capability.HordeEvent;
import net.smileycorp.hordes.hordeevent.capability.HordeSpawn;
import net.smileycorp.hordes.hordeevent.capability.WorldDataHordes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class MixinEntity implements ChatName {

    private String chatName;

    @Inject(at = @At("HEAD"), method = "getDisplayName", cancellable = true)
    protected void hordes$getDisplayName(CallbackInfoReturnable<ITextComponent> callback) {
        if (hasChatName()) callback.setReturnValue(new TextComponentTranslation(getChatName()));
    }

    @Inject(at =@At("HEAD"), method = "onRemovedFromWorld", remap = false)
    protected void hordes$onRemovedFromWorld(CallbackInfo ci) {
        EntityPlayerMP player = HordeSpawn.getHordePlayer((Entity) (Object) this);
        if (player == null) return;
        HordeEvent horde = WorldDataHordes.getData(player.getServerWorld()).getEvent(player);
        if (horde != null) horde.removeEntity((EntityLiving) (Object)this);
    }

    @Override
    public boolean hasChatName() {
        return chatName != null;
    }

    @Override
    public String getChatName() {
        return chatName;
    }

    @Override
    public void setChatName(String name) {
        chatName = name;
    }
    
}
