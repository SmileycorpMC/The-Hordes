package net.smileycorp.hordes.common.hordeevent.data.conditions;

import java.util.Random;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class BiomeCondition implements Condition {

	protected ResourceLocation biome;

	public BiomeCondition(ResourceLocation biome ) {
		this.biome = biome;
	}

	@Override
	public boolean apply(Level level, Player player, Random rand) {
		return level.getBiomeManager().m_204214_(player.blockPosition()).m_203373_(biome);
	}

}
