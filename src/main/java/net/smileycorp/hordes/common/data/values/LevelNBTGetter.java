package net.smileycorp.hordes.common.data.values;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.smileycorp.atlas.api.data.DataType;

public class LevelNBTGetter<T extends Comparable<T>> extends NBTGetter<T> {

	public LevelNBTGetter(String value, DataType<T> type) {
		super(value, type);
	}

	@Override
	protected CompoundTag getNBT(Level level, LivingEntity entity, RandomSource rand)  {
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		CompoundTag nbt = server.getWorldData().createTag(server.registryAccess(), new CompoundTag());
		return nbt;
	}

}
