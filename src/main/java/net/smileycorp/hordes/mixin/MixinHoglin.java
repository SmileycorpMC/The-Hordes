package net.smileycorp.hordes.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.hoglin.HoglinBase;
import net.minecraft.world.level.Level;
import net.smileycorp.hordes.common.CommonConfigHandler;

@Mixin(Hoglin.class)
public abstract class MixinHoglin extends Animal implements Enemy, HoglinBase {

	protected MixinHoglin(Level level) {
		super(null, level);
	}

	@Inject(at=@At("HEAD"), method = "isImmuneToZombification()Z", cancellable = true)
	public void isImmuneToZombification(CallbackInfoReturnable<Boolean> callback) {
		if (!CommonConfigHandler.piglinsHoglinsConvert.get()) {
			callback.setReturnValue(true);
			callback.cancel();
		}
	}

}
