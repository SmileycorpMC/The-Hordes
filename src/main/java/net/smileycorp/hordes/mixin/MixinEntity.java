package net.smileycorp.hordes.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.smileycorp.hordes.common.mixinutils.ChatName;
import net.smileycorp.hordes.hordeevent.capability.HordeEvent;
import net.smileycorp.hordes.hordeevent.capability.HordeSavedData;
import net.smileycorp.hordes.hordeevent.capability.HordeSpawn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class MixinEntity implements ChatName {
    
    @Shadow public World level;
    private String chatName;

    @Inject(at = @At("HEAD"), method = "getTypeName", cancellable = true)
    protected void getTypeName(CallbackInfoReturnable<ITextComponent> callback) {
        if (hasChatName()) callback.setReturnValue(new TranslationTextComponent(getChatName()));
    }

    @Inject(at =@At("HEAD"), method = "remove(Z)V", cancellable = true, remap = false)
    protected void remove(boolean keepData, CallbackInfo ci) {
        ServerPlayerEntity player = HordeSpawn.getHordePlayer((Entity)(Object)this);
        if (player == null) return;
        HordeEvent horde = HordeSavedData.getData((ServerWorld) player.level).getEvent(player);
        if (horde != null) horde.removeEntity((MobEntity) (Object)this);
    }
    
    //load pre 1.5.0 horde data from capability
    @Inject(at = @At("HEAD"), method = "load")
    public void load(CompoundNBT nbt, CallbackInfo callback) {
        if (!(level instanceof ServerWorld) |! (((Object)this) instanceof ServerPlayerEntity)) return;
        if (!nbt.contains("ForgeCaps", 10)) return;
        CompoundNBT caps = nbt.getCompound("ForgeCaps");
        if (!caps.contains("hordes:hordeevent", 10)) return;
        HordeSavedData data = HordeSavedData.getData((ServerWorld) level);
        HordeEvent event = data.getEvent((ServerPlayerEntity) (Object)this);
        event.readFromNBT(caps.getCompound("hordes:hordeevent"));
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
