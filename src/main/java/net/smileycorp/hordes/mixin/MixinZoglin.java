package net.smileycorp.hordes.mixin;

import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zoglin;
import net.smileycorp.hordes.config.CommonConfigHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Zoglin.class)
public abstract class MixinZoglin {

	@Inject(at=@At("HEAD"), method = "isTargetable", cancellable = true)
	protected void isTargetable(LivingEntity entity, CallbackInfoReturnable<Boolean> callback) {
		if ((!CommonConfigHandler.zoglinsAttackUndead.get() && entity.getType().is(EntityTypeTags.UNDEAD)) ||
				(!CommonConfigHandler.zoglinsAttackMobs.get() && entity instanceof Monster)) callback.setReturnValue(false);
	}

}
