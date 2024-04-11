package net.smileycorp.hordes.mixin;

import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.smileycorp.hordes.config.CommonConfigHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractPiglin.class)
public abstract class MixinAbstractPiglin {
	
	@Inject(at=@At("HEAD"), method = "isImmuneToZombification", cancellable = true)
	public void isImmuneToZombification(CallbackInfoReturnable<Boolean> callback) {
		if (!CommonConfigHandler.piglinsHoglinsConvert.get()) callback.setReturnValue(true);
	}
	
}
