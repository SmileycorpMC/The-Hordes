package net.smileycorp.hordes.hordeevent;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.smileycorp.atlas.api.util.DataUtils;
import net.smileycorp.hordes.common.ConfigHandler;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.common.Hordes;

import java.util.*;
import java.util.Map.Entry;

public class WorldDataHordeEvent extends WorldSavedData {

	public static final String DATA = Constants.modid + "_HordeEvent";

	//stores legacy event data until it's needed to be loaded by a player capability
	private Map<String, NBTTagCompound> legacyEventData = new HashMap<String, NBTTagCompound>();

	private int nextDay = 0;

	protected World world = null;

	public WorldDataHordeEvent() {
		this(DATA);
	}

	public WorldDataHordeEvent(String data) {
		super(data);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		legacyEventData.clear();
		for (String uuidstring : nbt.getKeySet()) {
			if (nbt.getTagId(uuidstring) == 10 && DataUtils.isValidUUID(uuidstring)) {
				legacyEventData.put(uuidstring, (NBTTagCompound) nbt.getTag(uuidstring));
			}
		}
		if (nbt.hasKey("nextDay")) {
			int next = nbt.getInteger("nextDay");
			if (next > nextDay) {
				nextDay = next;
			}
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		for (Entry<String, NBTTagCompound> entry : legacyEventData.entrySet()) {
			nbt.setTag(entry.getKey(), entry.getValue());
		}
		nbt.setInteger("nextDay", nextDay);
		return nbt;
	}

	public int getNextDay() {
		return nextDay;
	}

	public void setNextDay(int nextDay) {
		this.nextDay = nextDay;
	}

	//legacy function -- use capabilities instead
	@Deprecated
	public Set<OngoingHordeEvent> getEvents() {
		Set<OngoingHordeEvent> events = new HashSet<OngoingHordeEvent>();
		if (!world.isRemote) {
			for (EntityPlayer player : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers()) {
				if (player.hasCapability(Hordes.HORDE_EVENT, null)) {
					events.add((OngoingHordeEvent) player.getCapability(Hordes.HORDE_EVENT, null));
				}
			}
		}
		return events;
	}

	//legacy function -- use capabilities instead
	@Deprecated
	public OngoingHordeEvent getEventForPlayer(EntityPlayer player) {
		if (player.hasCapability(Hordes.HORDE_EVENT, null)) {
			return (OngoingHordeEvent) player.getCapability(Hordes.HORDE_EVENT, null);
		}
		return null;
	}

	//legacy function -- use capabilities instead
	@Deprecated
	public OngoingHordeEvent getEventForPlayer(GameProfile profile) {
		return getEventForPlayer(profile.getId());
	}

	//legacy function -- use capabilities instead
	@Deprecated
	public  OngoingHordeEvent getEventForPlayer(String uuid) {
		if (DataUtils.isValidUUID(uuid)) {
			return getEventForPlayer(UUID.fromString(uuid));
		}
		return null;
	}

	//legacy function -- use capabilities instead
	@Deprecated
	public OngoingHordeEvent getEventForPlayer(UUID uuid) {
		if (world != null) {
			if (!world.isRemote) {
				return getEventForPlayer(FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(uuid));
			}
		}
		return null;
	}

	public boolean hasLegacyData(UUID uuid) {
		return legacyEventData.containsKey(uuid.toString());
	}

	public NBTTagCompound getLegacyData(UUID uuid) {
		NBTTagCompound nbt = legacyEventData.get(uuid.toString());
		legacyEventData.remove(uuid.toString());
		if (nbt == null) nbt = new NBTTagCompound();
		return nbt;
	}

	public void save() {
		markDirty();
	}

	public static WorldDataHordeEvent getData(World world) {
		WorldDataHordeEvent data = (WorldDataHordeEvent) world.getMapStorage().getOrLoadData(WorldDataHordeEvent.class, DATA);
		if (data == null) {
			return getCleanData(world);
		}
		if (data.world == null) {
			data.world = world;
		}
		return data;
	}

	public static WorldDataHordeEvent getCleanData(World world) {
		WorldDataHordeEvent data = new WorldDataHordeEvent();
		data.world = world;
		int day = Math.round(world.getWorldTime()/ConfigHandler.dayLength);
		double multiplier = Math.ceil(day / ConfigHandler.hordeSpawnDays);
		if (!(ConfigHandler.spawnFirstDay && day == 0)) multiplier += 1;
		int nextDay = (int) Math.floor(((multiplier*ConfigHandler.hordeSpawnDays) + world.rand.nextInt(ConfigHandler.hordeSpawnVariation + 1)));
		data.setNextDay(nextDay);
		world.getMapStorage().setData(DATA, data);
		data.save();
		return data;
	}

	public List<String> getDebugText() {
		List<String> out = new ArrayList<String>();
		out.add(toString());
		out.add("Existing events: {");
		for (OngoingHordeEvent event : getEvents()) {
			out.add("	" +event.toString());
			out.addAll(event.getEntityStrings());
		}
		out.add("}");
		return out;
	}

	@Override
	public String toString() {
		return super.toString() + "[worldTime: " + world.getWorldTime() + ", nextDay="+nextDay+"]";
	}

}
