package net.smileycorp.hordes.config.data.conditions;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;

import java.util.Random;

public interface Condition {
	
	boolean apply(World level, EntityLivingBase entity, EntityPlayerMP player, Random rand);

}
