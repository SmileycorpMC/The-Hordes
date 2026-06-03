package net.smileycorp.hordes.config.data.conditions;

import com.google.gson.JsonElement;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.config.data.hordeevent.HordeContext;
import net.smileycorp.hordes.config.data.values.ValueGetter;

public class EntityTypeCondition implements Condition {

	protected ValueGetter<String> getter;

	public EntityTypeCondition(ValueGetter<String> getter) {
		this.getter = getter;
	}

	@Override
	public boolean apply(HordeContext<? extends HordePlayerEvent> ctx) {
		EntityEntry entry = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(getter.get(ctx)));
		return entry != null && entry.getEntityClass() == ctx.getEntity().getClass();
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
