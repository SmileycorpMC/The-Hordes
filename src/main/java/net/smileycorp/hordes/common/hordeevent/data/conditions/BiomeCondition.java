package net.smileycorp.hordes.common.hordeevent.data.conditions;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.smileycorp.hordes.common.HordesLogger;

public class BiomeCondition implements Condition {

	protected ResourceLocation biome;

	public BiomeCondition(ResourceLocation biome ) {
		this.biome = biome;
	}

	@Override
	public boolean apply(Level level, Player player, RandomSource rand) {
		return level.getBiomeManager().getBiome(player.blockPosition()).is(biome);
	}

	public static BiomeCondition deserialize(JsonElement json) {
		try {
			return new BiomeCondition(new ResourceLocation(json.getAsString()));
		} catch(Exception e) {
			HordesLogger.logError("Incorrect parameters for condition hordes:biome", e);
		}
		return null;
	}

}
