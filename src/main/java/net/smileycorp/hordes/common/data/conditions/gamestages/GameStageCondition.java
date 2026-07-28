package net.smileycorp.hordes.common.data.conditions.gamestages;

import com.google.gson.JsonElement;
import net.darkhax.gamestages.data.GameStageSaveHandler;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.data.conditions.Condition;
import net.smileycorp.hordes.common.data.values.ValueGetter;
import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.hordeevent.data.HordeContext;

public class GameStageCondition implements Condition {

	protected ValueGetter<String> stage;

	public GameStageCondition(ValueGetter<String> stage) {
		this.stage = stage;
	}

	@Override
	public boolean apply(HordeContext<? extends HordePlayerEvent> ctx) {
		return GameStageSaveHandler.getPlayerData(ctx.getPlayer().getUUID()).hasStage(stage.get(ctx));
	}

	public static GameStageCondition deserialize(JsonElement json) {
		try {
			return new GameStageCondition(ValueGetter.readValue(DataType.STRING, json));
		} catch(Exception e) {
			HordesLogger.logError("Incorrect parameters for condition gamestages:gamestage", e);
		}
		return null;
	}

}
