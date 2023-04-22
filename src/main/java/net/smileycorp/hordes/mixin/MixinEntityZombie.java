package net.smileycorp.hordes.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.monster.EntityZombie;
import net.smileycorp.hordes.common.ConfigHandler;

@Mixin(EntityZombie.class)
public class MixinEntityZombie {

	@Inject(at=@At("HEAD"), method = "shouldBurnInDay()Z", cancellable = true)
	public void shouldBurnInDay(CallbackInfoReturnable<Boolean> callback) {
		callback.setReturnValue(ConfigHandler.zombiesBurn);
		callback.cancel();
	}

}
