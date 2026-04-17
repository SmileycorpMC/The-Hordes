package net.smileycorp.hordes.common.data.values;

import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.hordeevent.data.HordeContext;

public class LevelNBTGetter<T extends Comparable<T>> extends NBTGetter<T> {

	private LevelNBTGetter(ValueGetter<String> value, DataType<T> type) {
		super(value, type);
	}

	@Override
	protected CompoundTag getNBT(HordeContext<? extends HordePlayerEvent> ctx)  {
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return server.getWorldData().createTag(server.registryAccess(), new CompoundTag());
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
