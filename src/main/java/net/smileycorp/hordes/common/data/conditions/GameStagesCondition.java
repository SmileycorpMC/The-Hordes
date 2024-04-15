package net.smileycorp.hordes.common.data.conditions;

import com.google.gson.JsonElement;
import net.darkhax.gamestages.data.GameStageSaveHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.World;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.data.values.ValueGetter;

import java.util.Random;

public class GameStagesCondition implements Condition {

	protected ValueGetter<String> stage;

	public GameStagesCondition(ValueGetter<String> stage) {
		this.stage = stage;
	}

	@Override
	public boolean apply(World level, LivingEntity entity, ServerPlayerEntity player, Random rand) {
		return GameStageSaveHandler.getPlayerData(player.getUUID()).hasStage(stage.get(level, entity, player, rand));
	}

	public static GameStagesCondition deserialize(JsonElement json) {
		try {
			return new GameStagesCondition(ValueGetter.readValue(DataType.STRING, json));
		} catch(Exception e) {
			HordesLogger.logError("Incorrect parameters for condition gamestages:gamestage", e);
		}
		return null;
	}

}
