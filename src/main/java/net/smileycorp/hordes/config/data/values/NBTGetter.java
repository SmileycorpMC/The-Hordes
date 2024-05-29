package net.smileycorp.hordes.config.data.values;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.atlas.api.data.NBTExplorer;
import net.smileycorp.hordes.common.data.DataType;

import java.util.Random;

public abstract class NBTGetter<T extends Comparable<T>> implements ValueGetter<T> {

	protected final ValueGetter<String> value;
	private final DataType<T> type;
	
	public NBTGetter(ValueGetter<String> value, DataType<T> type) {
		this.value = value;
		this.type = type;
	}

	@Override
	public T get(World level, EntityLiving entity, EntityPlayerMP player, Random rand) {
		try {
			return findValue(type, value.get(level, entity, player, rand), getNBT(level, entity, player, rand));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public T findValue(DataType<T> type, String target, NBTTagCompound compound) throws Exception {
		String[] directory = target.split("\\.");
		NBTBase nbt = compound;
		for (int i = 0; i < directory.length; i++) {
			try {
				if (nbt instanceof NBTTagCompound) {
					if (i == directory.length - 1) {
						T value = type.readFromNBT((NBTTagCompound) nbt, directory[i]);
						if (value == null) throw new Exception("Value " + directory[i] + "is not of type " + type.getType());
						return value;
					} else nbt = ((NBTTagCompound) nbt).getTag(directory[i]);
				} else if (nbt instanceof NBTTagList) {
					NBTBase nextNBT = null;
					for (NBTBase tag : ((NBTTagList)nbt)) if (tag instanceof NBTTagCompound && ((NBTTagCompound) tag).getString("Name").equals(directory[i])) {
						nextNBT = tag;
						break;
					}
					if  (nextNBT == null) throw new Exception("NBTTagList " + nbt + " does not contain \"Name\":\"" + directory[i] + "\"");
					else nbt = nextNBT;
				} else throw new Exception("Value " + directory[i] + " is not an applicable type or cannot be found as nbt is " + nbt);
			} catch (Exception e) {
				StringBuilder builder = new StringBuilder();
				for (int j = 0; j < directory.length; j++) {
					if (i == j) builder.append(">"+directory[j]+"<");
					else builder.append(directory[j]);
					if (j < directory.length - 1) builder.append("\\.");
				}
				throw new Exception(builder + " " + e.getMessage(), e.getCause());
			}
		}
		throw new Exception("Could not find value " + directory);
	}

	protected abstract NBTTagCompound getNBT(World level, EntityLiving entity, EntityPlayerMP player, Random rand);

}
