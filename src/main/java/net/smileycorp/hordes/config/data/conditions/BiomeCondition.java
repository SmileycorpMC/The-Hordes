package net.smileycorp.hordes.config.data.conditions;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.smileycorp.atlas.api.data.Either;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.config.data.hordeevent.HordeContext;

import java.util.List;

public class BiomeCondition implements Condition {
	
	protected final List<Either<BiomeDictionary.Type, ResourceLocation>> biomes;
	
	public BiomeCondition(List<Either<BiomeDictionary.Type, ResourceLocation>> biomes) {
		this.biomes = biomes;
	}

	@Override
	public boolean apply(HordeContext<? extends HordePlayerEvent> ctx) {
		Biome biome = ctx.getWorld().getBiome(ctx.getPlayer().getPosition());
		for (Either<BiomeDictionary.Type, ResourceLocation> either : biomes) if (either.map(t -> BiomeDictionary.hasType(biome, t),
				biome.getRegistryName()::equals)) return true;
		return false;
	}

	public static BiomeCondition deserialize(JsonElement json) {
		try {
			if (json.isJsonArray()) {
				List<Either<BiomeDictionary.Type, ResourceLocation>> biomes = Lists.newArrayList();
				for (JsonElement element : json.getAsJsonArray()) biomes.add(either(element.getAsString()));
				return new BiomeCondition(biomes);
			}
			return new BiomeCondition(Lists.newArrayList(either(json.getAsString())));
		} catch(Exception e) {
			HordesLogger.logError("Incorrect parameters for condition hordes:biome", e);
		}
		return null;
	}
	
	private static Either<BiomeDictionary.Type, ResourceLocation> either(String string) {
		return string.contains("#") ? Either.left(BiomeDictionary.Type.getType(string.replace("#", "")))
				: Either.right(new ResourceLocation(string));
	}
	
}
