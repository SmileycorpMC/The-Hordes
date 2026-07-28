package net.smileycorp.hordes.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.smileycorp.hordes.hordeevent.Playtime;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import javax.annotation.Nullable;

@Mixin(WorldServer.class)
public abstract class MixinWorldServer {

    @Shadow @Nullable public abstract MinecraftServer getMinecraftServer();

    @WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldServer;setWorldTime(J)V", ordinal = 0),  method = "tick")
    public void hordes$tick$setWorldTime(WorldServer instance, long time, Operation<Void> original) {
        long timePassed = time - instance.getWorldTime();
        for (EntityPlayerMP player : getMinecraftServer().getPlayerList().getPlayers()) {
            Playtime pt = (Playtime) player;
            pt.setPlaytime(pt.getPlaytime() + timePassed);
        }
    }

}
