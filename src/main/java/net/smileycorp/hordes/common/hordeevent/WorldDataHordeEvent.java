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
	
	private Map<UUID, OngoingHordeEvent> ongoingEvents =  new HashMap<UUID, OngoingHordeEvent>();
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
		for (String uuidstring : nbt.getKeySet()) {
			if (nbt.getTagId(uuidstring) == 10 && DataUtils.isValidUUID(uuidstring)) {
				UUID uuid = UUID.fromString(uuidstring);
				EntityPlayer player = getPlayerFromUUID(uuid);
				OngoingHordeEvent event = new OngoingHordeEvent(player);
				event.readFromNBT(nbt.getCompoundTag(uuidstring));
				ongoingEvents.put(uuid, event);
			}
		}
		if (nbt.hasKey("nextDay")) {
			nextDay = nbt.getInteger("nextDay");
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
			for (EntityPlayer player : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers()) {
				String uuid = player.getUniqueID().toString();
				if (!ongoingEvents.containsKey(uuid) && DataUtils.isValidUUID(uuid)) {
					OngoingHordeEvent event = new OngoingHordeEvent(player);
					ongoingEvents.put(UUID.fromString(uuid), event);
				}
			}
		}
		for (Entry<UUID, OngoingHordeEvent> entry : ongoingEvents.entrySet()) {
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
				boolean toadd = true;
				for (OngoingHordeEvent event : ongoingEvents.values()) {
					if (event.getPlayer() == player) {
						toadd = false;
						break;
					}
				}
				if (toadd == true) {
					ongoingEvents.put(player.getUniqueID(), new OngoingHordeEvent(player));
					markDirty();
				}
			}
		}
		for (OngoingHordeEvent event : ongoingEvents.values()) {
			events.add(event);
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
			return getEventForPlayer(UUID.fromString(uuid));
		} return null;
	}
	
	public OngoingHordeEvent getEventForPlayer(UUID uuid) {
		if (! ongoingEvents.containsKey(uuid)) {
			EntityPlayer player = getPlayerFromUUID(uuid);
			OngoingHordeEvent event = new OngoingHordeEvent(player);
			ongoingEvents.put(uuid, event);
			markDirty();
		}
		return ongoingEvents.get(uuid);
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
		world.getMapStorage().setData(DATA, this);
	}

	public static WorldDataHordeEvent get(World world) {
		WorldDataHordeEvent data = (WorldDataHordeEvent) world.getMapStorage().getOrLoadData(WorldDataHordeEvent.class, DATA);
		if (data== null) {
			data = new WorldDataHordeEvent();
			int day = Math.round(world.getWorldTime()/24000);
			int multiplier = (int) Math.ceil(day / ConfigHandler.hordeSpawnDays);
			data.setNextDay(day * multiplier);
			data.save();
		}
		if (data.world==null)data.world = world;
		return data;
	}
	
}
