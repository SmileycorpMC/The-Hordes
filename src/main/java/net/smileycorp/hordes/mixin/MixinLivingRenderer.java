package net.smileycorp.hordes.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.smileycorp.hordes.common.mixinutils.CustomTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;


@Mixin(LivingRenderer.class)
public abstract class MixinLivingRenderer<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> implements IEntityRenderer<T, M> {
    
    protected MixinLivingRenderer(EntityRendererManager p_i46179_1_) {
        super(p_i46179_1_);
    }
    
    @WrapOperation(method = "getRenderType", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/LivingRenderer;getTextureLocation(Lnet/minecraft/entity/Entity;)Lnet/minecraft/util/ResourceLocation;"))
    public ResourceLocation getCustomTexture(LivingRenderer instance, Entity entity, Operation<ResourceLocation> original) {
        if (!(entity instanceof LivingEntity)) return getTextureLocation((T) entity);
        CustomTexture textureGetter = (CustomTexture) entity;
        return textureGetter.hasCustomTexture() ? textureGetter.getTexture() : original.call(instance, entity);
    }

}
