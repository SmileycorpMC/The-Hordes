package net.smileycorp.hordes.common.ai;

import java.util.Collections;
import java.util.List;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.smileycorp.hordes.infection.InfectionRegister;

public class EntityAINearestAttackableConversion extends EntityAINearestAttackableTarget<EntityLivingBase> {

	public EntityAINearestAttackableConversion(EntityCreature creature, int chance, boolean checkSight, boolean onlyNearby) {
		super(creature, EntityLivingBase.class, chance, checkSight, onlyNearby, null);
	}


	@Override
	public boolean shouldExecute() {
		if (targetChance > 0 && taskOwner.getRNG().nextInt(targetChance) != 0) {
			return false;
		}
		else {
			List<EntityLivingBase> list = this.taskOwner.world.getEntitiesWithinAABB(EntityLivingBase.class, getTargetableArea(getTargetDistance()), InfectionRegister::canBeInfected);
			if (list.isEmpty()) {
				return false;
			}
			else
			{
				Collections.sort(list, this.sorter);
				this.targetEntity = list.get(0);
				return true;
			}
		}
	}

}
