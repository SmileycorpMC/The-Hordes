package net.smileycorp.hordes.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.smileycorp.hordes.common.CommonConfigHandler;

@Mixin(ZombifiedPiglin.class)
public abstract class MixinZombifiedPiglin extends Zombie {

	protected MixinZombifiedPiglin(Level level) {
		super(null, level);
	}

	@Inject(at=@At("HEAD"), method = "addBehaviourGoals()V", cancellable = true)
	public void addBehaviourGoals(CallbackInfo callback) {
		if (CommonConfigHandler.aggressiveZombiePiglins.get()) {
			targetSelector.addGoal(1, (new HurtByTargetGoal(this)));
			targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
			targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
			targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
			targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Turtle.class, 10, true, false, Turtle.BABY_ON_LAND_SELECTOR));
			goalSelector.addGoal(6, new MoveThroughVillageGoal(this, 1.0D, true, 4, () -> false));
		}
	}

}
