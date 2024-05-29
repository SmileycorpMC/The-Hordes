package net.smileycorp.hordes.config.data.conditions;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.World;

import java.util.Random;

public interface Condition {
	
	boolean apply(World level, EntityLiving entity, EntityPlayerMP player, Random rand);

}
