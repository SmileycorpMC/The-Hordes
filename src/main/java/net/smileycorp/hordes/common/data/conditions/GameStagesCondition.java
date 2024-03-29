package net.smileycorp.hordes.common.data.conditions;

import com.google.gson.JsonElement;
import net.darkhax.gamestages.data.GameStageSaveHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.smileycorp.hordes.common.HordesLogger;

public class GameStagesCondition implements Condition {

	protected String stage;

	public GameStagesCondition(String stage) {
		this.stage = stage;
	}

	@Override
	public boolean apply(Level level, LivingEntity entity, ServerPlayer player, RandomSource rand) {
		return GameStageSaveHandler.getPlayerData(player.getUUID()).hasStage(stage);
	}

	public static GameStagesCondition deserialize(JsonElement json) {
		try {
			return new GameStagesCondition(json.getAsString());
		} catch(Exception e) {
			HordesLogger.logError("Incorrect parameters for condition gamestages:gamestage", e);
		}
		return null;
	}

}
