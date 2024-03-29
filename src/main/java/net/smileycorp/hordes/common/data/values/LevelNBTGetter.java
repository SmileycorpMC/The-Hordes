package net.smileycorp.hordes.common.data.values;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;

public class LevelNBTGetter<T extends Comparable<T>> extends NBTGetter<T> {

	private LevelNBTGetter(ValueGetter<String> value, DataType<T> type) {
		super(value, type);
	}

	@Override
	protected CompoundTag getNBT(Level level, LivingEntity entity, RandomSource rand)  {
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
