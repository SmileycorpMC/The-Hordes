package net.smileycorp.hordes.common.ai;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.smileycorp.hordes.config.data.infection.InfectionDataLoader;

public class EntityAIHorseFlee extends EntityAIFleeEntity {

	public EntityAIHorseFlee(EntityLiving entity) {
		super(entity, 1.2, 15.0, EntityAIHorseFlee::canHorseFlee);
	}

	private static boolean canHorseFlee(EntityLivingBase target) {
		boolean loaderReady = InfectionDataLoader.INSTANCE != null;
        return loaderReady && InfectionDataLoader.INSTANCE.canCauseInfection(target);
	}

	@Override
	public boolean shouldExecute() {
		boolean base = super.shouldExecute();
		return base && entity.getPassengers().isEmpty();
	}

}
