package net.smileycorp.hordes.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.smileycorp.hordes.common.mixinutils.IChatName;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class MixinEntity implements IChatName {

    private String chatName;

    @Inject(at = @At("HEAD"), method = "getTypeName", cancellable = true)
    protected void getTypeName(CallbackInfoReturnable<Component> callback) {
        if (hasChatName()) callback.setReturnValue(Component.translatable(getChatName()));
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
