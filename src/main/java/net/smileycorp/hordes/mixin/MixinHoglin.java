package net.smileycorp.hordes.mixin;

import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.smileycorp.hordes.common.CommonConfigHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Hoglin.class)
public abstract class MixinHoglin {

	@Inject(at=@At("HEAD"), method = "isImmuneToZombification", cancellable = true)
	public void isImmuneToZombification(CallbackInfoReturnable<Boolean> callback) {
		if (!CommonConfigHandler.piglinsHoglinsConvert.get()) {
			callback.setReturnValue(true);
			callback.cancel();
		}
	}

}
