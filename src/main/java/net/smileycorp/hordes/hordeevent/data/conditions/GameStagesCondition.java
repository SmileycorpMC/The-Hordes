package net.smileycorp.hordes.hordeevent.data.conditions;

import com.google.gson.JsonElement;
import net.darkhax.gamestages.data.GameStageSaveHandler;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.hordeevent.data.values.ValueGetter;

public class GameStagesCondition implements Condition {

	protected ValueGetter<String> stage;

	public GameStagesCondition(ValueGetter<String> stage) {
		this.stage = stage;
	}

	@Override
	public boolean apply(HordePlayerEvent event) {
		return GameStageSaveHandler.getPlayerData(event.getPlayer().getUUID()).hasStage(stage.get(event));
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
