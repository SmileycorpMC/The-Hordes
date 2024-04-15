package net.smileycorp.hordes.common.data.conditions;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.smileycorp.hordes.common.HordesLogger;

import java.util.List;
import java.util.Random;

public class BiomeCondition implements Condition {
	
	protected final List<Either<TagKey<Biome>, ResourceLocation>> biomes;
	
	public BiomeCondition(List<Either<TagKey<Biome>, ResourceLocation>> biomes) {
		this.biomes = biomes;
	}

	@Override
	public boolean apply(World level, LivingEntity entity, ServerPlayerEntity player, Random rand) {
		Holder<Biome> biome = level.getBiomeManager().m_204214_(player.blockPosition());
		for (Either<TagKey<Biome>, ResourceLocation> either : biomes) if (either.map(biome::containsTag, biome::m_203373_)) return true;
		return false;
	}

	public static BiomeCondition deserialize(JsonElement json) {
		try {
			if (json.isJsonArray()) {
				List<Either<TagKey<Biome>, ResourceLocation>> biomes = Lists.newArrayList();
				for (JsonElement element : json.getAsJsonArray()) biomes.add(either(element.getAsString()));
				return new BiomeCondition(biomes);
			}
			return new BiomeCondition(Lists.newArrayList(either(json.getAsString())));
		} catch(Exception e) {
			HordesLogger.logError("Incorrect parameters for condition hordes:biome", e);
		}
		return null;
	}
	
	private static Either<TagKey<Biome>, ResourceLocation> either(String string) {
		return string.contains("#") ? Either.left(TagKey.m_203882_(Registry.BIOME_REGISTRY, new ResourceLocation(string.replace("#", ""))))
				: Either.right(new ResourceLocation(string));
	}
	
}
