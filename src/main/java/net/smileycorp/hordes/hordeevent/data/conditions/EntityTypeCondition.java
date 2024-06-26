package net.smileycorp.hordes.hordeevent.data.conditions;

import com.google.gson.JsonElement;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.hordeevent.data.values.ValueGetter;

public class EntityTypeCondition implements Condition {

	protected ValueGetter<String> getter;

	public EntityTypeCondition(ValueGetter<String> getter) {
		this.getter = getter;
	}

	@Override
	public boolean apply(HordePlayerEvent event) {
		ResourceLocation type = ResourceLocation.tryParse(getter.get(event));
		return BuiltInRegistries.ENTITY_TYPE.getKey(event.getEntity().getType()).equals(type);
	}

	public static EntityTypeCondition deserialize(JsonElement json) {
		try {
			return new EntityTypeCondition(ValueGetter.readValue(DataType.STRING, json));
		} catch(Exception e) {
			HordesLogger.logError("Incorrect parameters for condition hordes:entity_type", e);
		}
		return null;
	}

}
