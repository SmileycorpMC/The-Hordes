package net.smileycorp.hordes.common.hordeevent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
	
	private Map<String, OngoingHordeEvent> ongoingEvents = new HashMap<String, OngoingHordeEvent>();
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
		ongoingEvents.clear();
		for (String uuidstring : nbt.getKeySet()) {
			if (nbt.getTagId(uuidstring) == 10 && DataUtils.isValidUUID(uuidstring)) {
				UUID uuid = UUID.fromString(uuidstring);
				EntityPlayer player = getPlayerFromUUID(uuid);
				OngoingHordeEvent event = new OngoingHordeEvent(world, player, this);
				event.readFromNBT(nbt.getCompoundTag(uuidstring));
				ongoingEvents.put(uuidstring, event);
			}
		}
		if (nbt.hasKey("nextDay")) {
			int next = nbt.getInteger("nextDay");
			if (nextDay == 0 || next > nextDay) {
				nextDay = next;
			}
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		if (FMLCommonHandler.instance().getSide() == Side.SERVER) {
			for (EntityPlayer player : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers()) {
				String uuid = player.getUniqueID().toString();
				if (!ongoingEvents.containsKey(uuid) && DataUtils.isValidUUID(uuid)) {
					OngoingHordeEvent event = new OngoingHordeEvent(world, player, this);
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
					ongoingEvents.put(player.getUniqueID().toString(), new OngoingHordeEvent(world, player, this));
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
				OngoingHordeEvent event = new OngoingHordeEvent(world, player, this);
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
		if (data.world == null) {
			data.world = world;
			int day = Math.round(world.getWorldTime()/ConfigHandler.dayLength);
			if (!ConfigHandler.spawnFirstDay && day <1) day  = 1;
			int multiplier = (int) Math.ceil(day / ConfigHandler.hordeSpawnDays);
			data.setNextDay((day * multiplier) + world.rand.nextInt(ConfigHandler.hordeSpawnVariation + 1));
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
		out.add(this.toString());
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
