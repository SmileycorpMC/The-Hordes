package net.smileycorp.hordes.common.data.conditions.astages;

import com.alessandro.astages.infrastructure.capability.PlayerStageWrapper;
import com.google.gson.JsonElement;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.data.conditions.Condition;
import net.smileycorp.hordes.common.data.values.ValueGetter;
import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.hordeevent.data.HordeContext;

import java.util.Locale;

public class PlayerStageCondition implements Condition {

	protected ValueGetter<String> stage;

	public PlayerStageCondition(ValueGetter<String> stage) {
		this.stage = stage;
	}

	@Override
	public boolean apply(HordeContext<? extends HordePlayerEvent> ctx) {
		return PlayerStageWrapper.getStages(ctx.getPlayer()).contains(stage.get(ctx).toLowerCase(Locale.US));
	}

	public static PlayerStageCondition deserialize(JsonElement json) {
		try {
			return new PlayerStageCondition(ValueGetter.readValue(DataType.STRING, json));
		} catch(Exception e) {
			HordesLogger.logError("Incorrect parameters for condition astages:player_stage", e);
		}
		return null;
	}

}
