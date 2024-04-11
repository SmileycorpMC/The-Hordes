package net.smileycorp.hordes.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.VillagerProfessionLayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.smileycorp.hordes.common.mixinutils.CustomTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VillagerProfessionLayer.class)
public class MixinVillagerProfessionLayer {

    @Inject(at = @At("HEAD"), method="render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/Entity;FFFFFF)V", cancellable = true)
    public void render(PoseStack par1, MultiBufferSource par2, int par3, Entity entity, float par5, float par6, float par7, float par8, float par9, float par10, CallbackInfo callback) {
        if (entity.isInvisible()) return;
        if (((CustomTexture)entity).hasCustomTexture()) callback.cancel();
    }

}
