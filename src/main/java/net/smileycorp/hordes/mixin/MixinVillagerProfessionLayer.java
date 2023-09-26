package net.smileycorp.hordes.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.VillagerProfessionLayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerDataHolder;
import net.minecraft.world.entity.npc.VillagerType;
import net.smileycorp.hordes.common.mixinutils.ICustomTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VillagerProfessionLayer.class)
public class MixinVillagerProfessionLayer {

    @Inject(at = @At("HEAD"), method="render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/Entity;FFFFFF)V", cancellable = true)
    public void render(PoseStack p_117646_, MultiBufferSource p_117647_, int p_117648_, Entity entity, float p_117650_, float p_117651_, float p_117652_, float p_117653_, float p_117654_, float p_117655_, CallbackInfo callback) {
        if (entity.isInvisible()) return;
        if (((ICustomTexture)entity).hasCustomTexture()) callback.cancel();
    }

}
