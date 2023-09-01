package net.smileycorp.hordes.common.hordeevent.data.scripts.conditions;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.Random;

public interface Condition {

	public boolean apply(Level level, Player player, Random rand);

}
