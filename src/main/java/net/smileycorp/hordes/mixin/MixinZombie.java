package net.smileycorp.hordes.mixin;

import net.minecraft.world.entity.monster.Zombie;
import net.smileycorp.hordes.config.CommonConfigHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Zombie.class)
public abstract class MixinZombie {
	
	@Inject(at=@At("HEAD"), method = "isSunSensitive", cancellable = true)
	public void isSunSensitive(CallbackInfoReturnable<Boolean> callback) {
		callback.setReturnValue(CommonConfigHandler.zombiesBurn.get());
	}

}
