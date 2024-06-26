package net.smileycorp.hordes.hordeevent.data.values;

import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.event.HordePlayerEvent;

public class LevelNBTGetter<T extends Comparable<T>> extends NBTGetter<T> {

	private LevelNBTGetter(ValueGetter<String> value, DataType<T> type) {
		super(value, type);
	}

	@Override
	protected CompoundTag getNBT(HordePlayerEvent event)  {
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		CompoundTag nbt = server.getWorldData().createTag(server.registryAccess(), new CompoundTag());
		return nbt;
	}
	
	public static <T extends Comparable<T>> ValueGetter deserialize(JsonObject object, DataType<T> type) {
		try {
			if (object.has("value")) return new LevelNBTGetter<T>(ValueGetter.readValue(DataType.STRING, object.get("value")), type);
		} catch (Exception e) {
			HordesLogger.logError("invalid value for hordes:level_nbt", e);
		}
		return null;
	}
	
}
