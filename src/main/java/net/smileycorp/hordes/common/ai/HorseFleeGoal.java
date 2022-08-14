package net.smileycorp.hordes.common.ai;

import net.minecraft.world.entity.Mob;
import net.smileycorp.hordes.common.infection.InfectionRegister;

public class HorseFleeGoal extends FleeEntityGoal {

	public HorseFleeGoal(Mob entity) {
		super(entity, 2D, 15, InfectionRegister::canCauseInfection);
	}

	@Override
	public boolean canUse() {
		return super.canUse() && entity.getPassengers().isEmpty();
	}

}
