package net.smileycorp.hordes.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.ai.attributes.AttributeModifierMap.MutableAttribute;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.PanicGoal;
import net.minecraft.entity.ai.goal.RunAroundLikeCrazyGoal;
import net.minecraft.entity.merchant.villager.AbstractVillagerEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.entity.passive.horse.ZombieHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.smileycorp.hordes.common.CommonConfigHandler;

@Mixin(ZombieHorseEntity.class)
public abstract class MixinZombieHorseEntity extends AbstractHorseEntity implements IMob {

	protected MixinZombieHorseEntity(World world) {
		super(null, world);
	}

	@Inject(at=@At("HEAD"), method = "addBehaviourGoals()V", cancellable = true)
	public void addBehaviourGoals(CallbackInfo callback) {
		if (CommonConfigHandler.aggressiveZombieHorses.get()) {
			targetSelector.addGoal(1, (new HurtByTargetGoal(this)));
			targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, true));
			targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillagerEntity.class, false));
			targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolemEntity.class, true));
			targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, TurtleEntity.class, 10, true, false, TurtleEntity.BABY_ON_LAND_SELECTOR));
			goalSelector.addGoal(2, new MeleeAttackGoal(this, 2.0D, false));
			goalSelector.addGoal(6, new MoveThroughVillageGoal(this, 1.0D, true, 4, () -> false));
			goalSelector.availableGoals.removeIf((goal) -> goal.getGoal() instanceof PanicGoal);
			goalSelector.availableGoals.removeIf((goal) -> goal.getGoal() instanceof RunAroundLikeCrazyGoal);
		}
	}

	@Inject(at=@At("TAIL"), method = "createAttributes()Lnet/minecraft/entity/ai/attributes/AttributeModifierMap$MutableAttribute;", cancellable = true)
	private static void createAttributes(CallbackInfoReturnable<MutableAttribute> callback) {
		if (CommonConfigHandler.aggressiveZombieHorses.get()) {
			callback.setReturnValue(callback.getReturnValue().add(Attributes.FOLLOW_RANGE, 35.0D).add(Attributes.ATTACK_DAMAGE, 3.0D));
		}
	}

	@Override
	protected boolean shouldDespawnInPeaceful() {
		return true;
	}

}
