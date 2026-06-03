package net.smileycorp.hordes.common.ai;

import net.minecraft.entity.EntityLiving;
import net.smileycorp.hordes.config.data.infection.InfectionData;

public class EntityAIHorseFlee extends EntityAIFleeEntity {

	public EntityAIHorseFlee(EntityLiving entity) {
		super(entity, 2D, 15, InfectionData.INSTANCE::canCauseInfection);
	}

	@Override
	public boolean shouldExecute() {
		return super.shouldExecute() && entity.getPassengers().isEmpty();
	}

}
