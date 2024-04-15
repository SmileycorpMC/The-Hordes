package net.smileycorp.hordes.common.data.values;

import com.google.gson.JsonObject;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;

import java.util.Random;

public class LevelNBTGetter<T extends Comparable<T>> extends NBTGetter<T> {

	private LevelNBTGetter(ValueGetter<String> value, DataType<T> type) {
		super(value, type);
	}

	@Override
	protected CompoundNBT getNBT(World level, LivingEntity entity, ServerPlayerEntity player, Random rand)  {
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		CompoundNBT nbt = server.getWorldData().createTag(server.registryAccess(), new CompoundNBT());
		return nbt;
	}
	
	public static <T extends Comparable<T>> ValueGetter deserialize(JsonObject object, DataType<T> type) {
		try {
			if (object.has("value")) return new LevelNBTGetter<T>(readValue(DataType.STRING, object.get("value")), type);
		} catch (Exception e) {
			HordesLogger.logError("invalid value for hordes:level_nbt", e);
		}
		return null;
	}
	
}
