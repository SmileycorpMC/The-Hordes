package net.smileycorp.hordes.mixin;

import net.minecraft.client.renderer.entity.model.VillagerModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.smileycorp.hordes.common.mixinutils.CustomTexture;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VillagerModel.class)
public class MixinVillagerModel {
    
    @Shadow protected ModelRenderer hat;
    
    @Shadow @Final protected ModelRenderer hatRim;
    
    @Shadow @Final protected ModelRenderer jacket;
    
    @Inject(at = @At("HEAD"), method="setupAnim", cancellable = true)
    public void setupAnim(Entity entity, float p_225597_2_, float p_225597_3_, float p_225597_4_, float p_225597_5_, float p_225597_6_, CallbackInfo callback) {
        if (entity instanceof CustomTexture && ((CustomTexture)entity).hasCustomTexture()) {
            hat.visible = false;
            hatRim.visible = false;
            jacket.visible = false;
        } else {
            hat.visible = true;
            hatRim.visible = true;
            jacket.visible = true;
        }
    }

}
