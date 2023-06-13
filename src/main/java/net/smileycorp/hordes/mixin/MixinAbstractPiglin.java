package net.smileycorp.hordes.mixin;

import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.level.Level;
import net.smileycorp.hordes.common.CommonConfigHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractPiglin.class)
public abstract class MixinAbstractPiglin extends Monster {

	protected MixinAbstractPiglin(Level level) {
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
