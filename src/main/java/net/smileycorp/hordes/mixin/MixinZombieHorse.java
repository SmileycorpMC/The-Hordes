package net.smileycorp.hordes.mixin;

import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RunAroundLikeCrazyGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.ZombieHorse;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.smileycorp.hordes.common.CommonConfigHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ZombieHorse.class)
public abstract class MixinZombieHorse extends AbstractHorse implements Enemy {

	protected MixinZombieHorse(Level level) {
		super(null, level);
	}

	@Inject(at=@At("HEAD"), method = "addBehaviourGoals()V", cancellable = true)
	public void addBehaviourGoals(CallbackInfo callback) {
		if (CommonConfigHandler.aggressiveZombieHorses.get()) {
			targetSelector.addGoal(1, (new HurtByTargetGoal(this)));
			targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
			targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
			targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
			targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Turtle.class, 10, true, false, Turtle.BABY_ON_LAND_SELECTOR));
			goalSelector.addGoal(2, new MeleeAttackGoal(this, 2.0D, false));
			goalSelector.addGoal(6, new MoveThroughVillageGoal(this, 1.0D, true, 4, () -> false));
			goalSelector.getAvailableGoals().removeIf((goal) -> goal.getGoal() instanceof PanicGoal);
			goalSelector.getAvailableGoals().removeIf((goal) -> goal.getGoal() instanceof RunAroundLikeCrazyGoal);
		}
	}

	@Inject(at=@At("TAIL"), method = "createAttributes()Lnet/minecraft/world/entity/ai/attributes/AttributeSupplier$Builder;", cancellable = true)
	private static void createAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> callback) {
		if (CommonConfigHandler.aggressiveZombieHorses.get()) {
			callback.setReturnValue(callback.getReturnValue()
					.add(Attributes.FOLLOW_RANGE, 35.0D).add(Attributes.ATTACK_DAMAGE, 3.0D));
		}
	}

	@Override
	protected boolean shouldDespawnInPeaceful() {
		return true;
	}

}
