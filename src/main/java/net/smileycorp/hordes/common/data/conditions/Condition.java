package net.smileycorp.hordes.common.data.conditions;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.World;

import java.util.Random;

public interface Condition {
	
	boolean apply(World level, LivingEntity entity, ServerPlayerEntity player, Random rand);

}
