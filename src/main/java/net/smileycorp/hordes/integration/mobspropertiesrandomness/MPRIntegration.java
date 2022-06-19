package net.smileycorp.hordes.integration.mobspropertiesrandomness;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.attributes.IAttributeInstance;

public class MPRIntegration {

	public static void addFollowAttribute(EntityLiving entity) {
		IAttributeInstance attribute = entity.getAttributeMap().getAttributeInstanceByName("mpr:generic.followRange");
		if (attribute != null) attribute.setBaseValue(100.0D);
	}

}
