package net.smileycorp.hordes.common.hordeevent.data.conditions;

import java.util.Random;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class RandomCondition implements Condition {

	protected float chance;

	public RandomCondition(float chance) {
		this.chance = chance;
	}

	@Override
	public boolean apply(Level level, Player player, Random rand) {
		return rand.nextInt((int)(chance * 100f)) == 0;
	}

}
