package net.smileycorp.hordes.common.ai;

import net.minecraft.entity.MobEntity;
import net.smileycorp.hordes.infection.HordesInfection;

public class HorseFleeGoal extends FleeEntityGoal {

	public HorseFleeGoal(MobEntity entity) {
		super(entity, 2D, 15, HordesInfection::canCauseInfection);
	}

	@Override
	public boolean canUse() {
		return super.canUse() && entity.getPassengers().isEmpty();
	}

}
