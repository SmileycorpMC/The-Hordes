package net.smileycorp.hordes.common.hordeevent.data.conditions;

import java.util.Random;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public interface Condition {

	public boolean apply(Level level, Player player, Random rand);

}
