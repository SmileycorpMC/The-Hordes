package net.smileycorp.hordes.common.data.conditions;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public interface Condition {

	public boolean apply(Level level, LivingEntity entity, RandomSource rand);

}
