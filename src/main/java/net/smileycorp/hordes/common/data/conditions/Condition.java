package net.smileycorp.hordes.common.data.conditions;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.Random;

public interface Condition {

	boolean apply(Level level, LivingEntity entity, ServerPlayer player, Random rand);

}
