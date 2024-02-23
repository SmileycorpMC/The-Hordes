package net.smileycorp.hordes.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.smileycorp.hordes.common.mixinutils.ChatName;
import net.smileycorp.hordes.hordeevent.capability.HordeEvent;
import net.smileycorp.hordes.hordeevent.capability.HordeSavedData;
import net.smileycorp.hordes.hordeevent.capability.HordeSpawn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class MixinEntity implements ChatName {

    private String chatName;

    @Inject(at = @At("HEAD"), method = "getTypeName", cancellable = true)
    protected void getTypeName(CallbackInfoReturnable<Component> callback) {
        if (hasChatName()) callback.setReturnValue(Component.translatable(getChatName()));
    }

    @Inject(at =@At("HEAD"), method = "remove", cancellable = true)
    protected void remove(Entity.RemovalReason reason, CallbackInfo callback) {
        ServerPlayer player = HordeSpawn.getHordePlayer((Entity)(Object)this);
        if (player == null) return;
        HordeEvent horde = HordeSavedData.getData((ServerLevel) player.level()).getEvent(player);
        if (horde != null) horde.removeEntity((Mob)(Object)this);
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
