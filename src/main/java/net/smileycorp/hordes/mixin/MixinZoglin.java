package net.smileycorp.hordes.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.hoglin.HoglinBase;
import net.minecraft.world.level.Level;
import net.smileycorp.hordes.common.CommonConfigHandler;

@Mixin(Zoglin.class)
public abstract class MixinZoglin extends Monster implements Enemy, HoglinBase {

	protected MixinZoglin(Level level) {
		super(null, level);
	}

	@Inject(at=@At("HEAD"), method = "isTargetable(Lnet/minecraft/world/entity/LivingEntity;)Z", cancellable = true)
	protected void isTargetable(LivingEntity entity, CallbackInfoReturnable<Boolean> callback) {
		if (!CommonConfigHandler.zoglinsAttackUndead.get() && entity.getMobType() == MobType.UNDEAD) {
			callback.setReturnValue(false);
			callback.cancel();
		}
	}

}
