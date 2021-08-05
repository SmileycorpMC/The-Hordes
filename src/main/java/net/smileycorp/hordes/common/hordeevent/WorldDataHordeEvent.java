package net.smileycorp.hordes.common.hordeevent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.smileycorp.atlas.api.util.DataUtils;
import net.smileycorp.hordes.client.ClientHandler;
import net.smileycorp.hordes.common.ConfigHandler;
import net.smileycorp.hordes.common.ModDefinitions;

import com.mojang.authlib.GameProfile;

public class WorldDataHordeEvent extends WorldSavedData {

	public static final String DATA = ModDefinitions.modid + "_HordeEvent";
	
	private Map<String, OngoingHordeEvent> ongoingEvents =  new HashMap<String, OngoingHordeEvent>();
	private int nextDay;
	
	protected World world = null;
	
	public WorldDataHordeEvent() {
		this(DATA);
	}
	
	public WorldDataHordeEvent(String data) {
		super(data);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		ongoingEvents.clear();
		world = null;
		for (String uuidstring : nbt.getKeySet()) {
			if (nbt.getTagId(uuidstring) == 10 && DataUtils.isValidUUID(uuidstring)) {
				UUID uuid = UUID.fromString(uuidstring);
				EntityPlayer player = getPlayerFromUUID(uuid);
				OngoingHordeEvent event = new OngoingHordeEvent(player);
				event.readFromNBT(nbt.getCompoundTag(uuidstring));
				ongoingEvents.put(uuidstring, event);
			}
		}
		if (nbt.hasKey("nextDay")) {
			nextDay = nbt.getInteger("nextDay");
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		if (FMLCommonHandler.instance().getSide() == Side.SERVER) {
			for (EntityPlayer player : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers()) {
				String uuid = player.getUniqueID().toString();
				if (!ongoingEvents.containsKey(uuid) && DataUtils.isValidUUID(uuid)) {
					OngoingHordeEvent event = new OngoingHordeEvent(player);
					ongoingEvents.put(uuid, event);
				}
			}
		}
		for (Entry<String, OngoingHordeEvent> entry : ongoingEvents.entrySet()) {
			String uuid = entry.getKey().toString();
			OngoingHordeEvent event = entry.getValue();
			nbt.setTag(uuid, event.writeToNBT(new NBTTagCompound()));
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
	
	public Set<OngoingHordeEvent> getEvents() {
		Set<OngoingHordeEvent> events = new HashSet<OngoingHordeEvent>();
		if (!world.isRemote) {
			for (EntityPlayer player : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers()) {
				boolean toAdd = true;
				for (OngoingHordeEvent event : ongoingEvents.values()) {
					if (event.getPlayer() == player) {
						toAdd = false;
						break;
					}
				}
				if (toAdd) {
					ongoingEvents.put(player.getUniqueID().toString(), new OngoingHordeEvent(player));
				}
			}
		}
		for (Entry<String, OngoingHordeEvent> entry : ongoingEvents.entrySet()) {
			if (DataUtils.isValidUUID(entry.getKey()) &!(entry.getValue().getPlayer() == null)) {
				events.add(entry.getValue());
			}
		}
		return events;
	}
	
	public OngoingHordeEvent getEventForPlayer(EntityPlayer player) {
		return getEventForPlayer(player.getUniqueID());
	}
	
	public OngoingHordeEvent getEventForPlayer(GameProfile profile) {
		return getEventForPlayer(profile.getId());
	}
	
	public OngoingHordeEvent getEventForPlayer(String uuid) {
		if (DataUtils.isValidUUID(uuid)) {
			if (! ongoingEvents.containsKey(uuid)) {
				EntityPlayer player = getPlayerFromUUID(UUID.fromString(uuid));
				OngoingHordeEvent event = new OngoingHordeEvent(player);
				ongoingEvents.put(uuid, event);
				markDirty();
			}
			return ongoingEvents.get(uuid);
		} return null;
	}
	
	public OngoingHordeEvent getEventForPlayer(UUID uuid) {
		return getEventForPlayer(uuid.toString());
	}
	
	private EntityPlayer getPlayerFromUUID(UUID uuid) {
		if (world == null) return null;
		if (world.isRemote) {
			return ClientHandler.getPlayer();
		}
        return FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(uuid);
	}
	
	public void save() {
		markDirty();
	}

	public static WorldDataHordeEvent getData(World world) {
		WorldDataHordeEvent data = (WorldDataHordeEvent) world.getMapStorage().getOrLoadData(WorldDataHordeEvent.class, DATA);
		if (data == null) {
			return getCleanData(world);
		} 
		if (data.world==null)data.world = world;
		return data;
	}

	public static WorldDataHordeEvent getCleanData(World world) {
		WorldDataHordeEvent data = new WorldDataHordeEvent();
		int day = Math.round(world.getWorldTime()/24000);
		int multiplier = (int) Math.ceil(day / ConfigHandler.hordeSpawnDays);
		data.setNextDay(day * multiplier);
		data.world = world;
		world.getMapStorage().setData(DATA, data);
		data.getEvents();
		data.save();
		return data;
	}
	
}
