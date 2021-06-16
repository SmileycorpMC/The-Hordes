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
import net.smileycorp.hordes.common.ModDefinitions;

import com.mojang.authlib.GameProfile;

public class WorldSaveHordeEvent extends WorldSavedData {

	public static final String DATA = ModDefinitions.modid + "_HordeEvent";
	
	private Map<String, OngoingHordeEvent> ongoingEvents =  new HashMap<String, OngoingHordeEvent>();
	
	public WorldSaveHordeEvent() {
		super(DATA);
	}
	
	public WorldSaveHordeEvent(String data) {
		super(data);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		for (String uuid : nbt.getKeySet()) {
			if (nbt.getTagId(uuid) == 10) {
				EntityPlayer player = getPlayerFromUUID(uuid);
				OngoingHordeEvent event = new OngoingHordeEvent(player);
				event.readFromNBT(nbt.getCompoundTag(uuid));
				ongoingEvents.put(uuid, event);
			}
		}
	}

	private EntityPlayer getPlayerFromUUID(String uuid) {
		Side side = FMLCommonHandler.instance().getEffectiveSide();
		return side == Side.CLIENT ? null :
            FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(UUID.fromString(uuid));
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
			for (EntityPlayer player : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers()) {
				String uuid = player.getUniqueID().toString();
				if (!ongoingEvents.containsKey(uuid)) {
					OngoingHordeEvent event = new OngoingHordeEvent(player);
					ongoingEvents.put(uuid, event);
				}
			}
			for (Entry<String, OngoingHordeEvent> entry : ongoingEvents.entrySet()) {
				String uuid = entry.getKey();
				OngoingHordeEvent event = entry.getValue();
				nbt.setTag(uuid, event.writeToNBT(new NBTTagCompound()));
			}
		}
		return nbt;
	}
	
	public Set<OngoingHordeEvent> getEvents() {
		Set<OngoingHordeEvent> events = new HashSet<OngoingHordeEvent>();
		for (OngoingHordeEvent event : ongoingEvents.values()) {
			if (event.getPlayer()!=null) events.add(event);
		}
		return events;
	}
	
	public OngoingHordeEvent getEventForPlayer(EntityPlayer player) {
		return getEventForPlayer(player.getUniqueID());
	}
	
	public OngoingHordeEvent getEventForPlayer(GameProfile profile) {
		return getEventForPlayer(profile.getId());
	}
	
	public OngoingHordeEvent getEventForPlayer(UUID uuid) {
		return getEventForPlayer(uuid.toString());
	}
	
	public OngoingHordeEvent getEventForPlayer(String uuid) {
		if (! ongoingEvents.containsKey(uuid)) {
			EntityPlayer player = getPlayerFromUUID(uuid);
			OngoingHordeEvent event = new OngoingHordeEvent(player);
			ongoingEvents.put(uuid, event);
			markDirty();
		}
		return ongoingEvents.get(uuid);
	}

	public static WorldSaveHordeEvent get(World world) {
		WorldSaveHordeEvent data = (WorldSaveHordeEvent) world.getMapStorage().getOrLoadData(WorldSaveHordeEvent.class, WorldSaveHordeEvent.DATA);
		if (data== null) {
			data = new WorldSaveHordeEvent();
			world.getMapStorage().setData(WorldSaveHordeEvent.DATA, data);
		}
		return data;
	}
	
	
}
