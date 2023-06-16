package net.smileycorp.hordes.common.hordeevent.data.conditions;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public interface Condition {

	public boolean apply(Level level, Player player, RandomSource rand);

}
