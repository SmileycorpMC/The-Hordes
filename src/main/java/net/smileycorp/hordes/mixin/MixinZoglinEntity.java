package net.smileycorp.hordes.mixin;

import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.monster.ZoglinEntity;
import net.smileycorp.hordes.config.CommonConfigHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ZoglinEntity.class)
public abstract class MixinZoglinEntity {
	
	@Inject(at=@At("HEAD"), method = "isTargetable", cancellable = true)
	protected void isTargetable(LivingEntity entity, CallbackInfoReturnable<Boolean> callback) {
		if ((!CommonConfigHandler.zoglinsAttackUndead.get() && entity.getMobType() == CreatureAttribute.UNDEAD) ||
				(!CommonConfigHandler.zoglinsAttackMobs.get() && entity instanceof MonsterEntity)) callback.setReturnValue(false);
	}

}
