package net.smileycorp.hordes.common.ai;

import net.minecraft.entity.MobEntity;
import net.smileycorp.hordes.common.infection.InfectionRegister;

public class HorseFleeGoal extends FleeEntityGoal {

	public HorseFleeGoal(MobEntity entity) {
		super(entity, 2D, 15, InfectionRegister::canCauseInfection);
	}

	@Override
	public boolean canUse() {
		return super.canUse() && entity.getPassengers().isEmpty();
	}

}
