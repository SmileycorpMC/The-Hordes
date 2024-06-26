package net.smileycorp.hordes.hordeevent.data.conditions;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.hordeevent.data.values.ValueGetter;

public class AdvancementCondition implements Condition {

	protected ValueGetter<String> getter;

	public AdvancementCondition(ValueGetter<String> getter) {
		this.getter = getter;
	}

	@Override
	public boolean apply(HordePlayerEvent event) {
		ResourceLocation advancement = ResourceLocation.tryParse(getter.get(event));
		return event.getPlayer().getAdvancements().getOrStartProgress(event.getPlayer().getServer().getAdvancements().get(advancement)).isDone();
	}

	public static AdvancementCondition deserialize(JsonElement json) {
		try {
			return new AdvancementCondition(ValueGetter.readValue(DataType.STRING, json));
		} catch(Exception e) {
			HordesLogger.logError("Incorrect parameters for condition hordes:advancement", e);
		}
		return null;
	}

}
