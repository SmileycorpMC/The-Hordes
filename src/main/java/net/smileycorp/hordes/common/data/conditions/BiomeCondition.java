package net.smileycorp.hordes.common.data.conditions;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.smileycorp.hordes.common.HordesLogger;

import java.util.List;
import java.util.Random;

public class BiomeCondition implements Condition {
	
	protected final List<Either<BiomeDictionary.Type, ResourceLocation>> biomes;
	
	public BiomeCondition(List<Either<BiomeDictionary.Type, ResourceLocation>> biomes) {
		this.biomes = biomes;
	}

	@Override
	public boolean apply(World level, LivingEntity entity, ServerPlayerEntity player, Random rand) {
		Biome biome = level.getBiomeManager().getBiome(player.blockPosition());
		for (Either<BiomeDictionary.Type, ResourceLocation> either : biomes) if (either.map(t -> BiomeDictionary.hasType(RegistryKey.create(Registry.BIOME_REGISTRY, biome.getRegistryName()), t), biome::equals)) return true;
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
