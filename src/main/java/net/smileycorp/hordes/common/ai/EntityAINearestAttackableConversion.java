package net.smileycorp.hordes.common.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.smileycorp.hordes.config.data.infection.InfectionDataLoader;

import java.util.Collections;
import java.util.List;

public class EntityAINearestAttackableConversion extends EntityAINearestAttackableTarget<EntityLivingBase> {

	public EntityAINearestAttackableConversion(EntityCreature creature) {
		super(creature, EntityLivingBase.class,  10, true, true, null);
	}


	@Override
	public boolean shouldExecute() {
		if (targetChance > 0 && taskOwner.getRNG().nextInt(targetChance) != 0) return false;
		else {
			List<EntityLivingBase> list = taskOwner.world.getEntitiesWithinAABB(EntityLivingBase.class,
					getTargetableArea(getTargetDistance()), InfectionDataLoader.INSTANCE::canBeInfected);
			if (list.isEmpty()) return false;
			else {
				Collections.sort(list, sorter);
				this.targetEntity = list.get(0);
				return true;
			}
		}
	}

}
