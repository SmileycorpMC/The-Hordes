package net.smileycorp.hordes.common.hordeevent.capability;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.smileycorp.hordes.common.ConfigHandler;
import net.smileycorp.hordes.common.Hordes;
import net.smileycorp.hordes.common.ModDefinitions;

public class HordeWorldData extends WorldSavedData {

	public static final String DATA = ModDefinitions.modid + "_HordeEvent";

	private int nextDay = 0;

	protected World world = null;

	public HordeWorldData() {
		this(DATA);
	}

	public HordeWorldData(String data) {
		super(data);
	}

	@Override
	public void readFromNBT(CompoundNBT nbt) {
		if (nbt.hasKey("nextDay")) {
			int next = nbt.getInteger("nextDay");
			if (nextDay == 0 || next > nextDay) {
				nextDay = next;
			}
		}
	}

	@Override
	public CompoundNBT writeToNBT(CompoundNBT nbt) {
		for (Entry<String, CompoundNBT> entry : legacyEventData.entrySet()) {
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

	public static HordeWorldData getData(World world) {
		HordeWorldData data = (HordeWorldData) world.get;
		if (data == null) {
			return getCleanData(world);
		}
		if (data.world == null) {
			data.world = world;
			int day = Math.round(world.getDayTime()/ConfigHandler.dayLength);
			if (!ConfigHandler.spawnFirstDay && day <1) day  = 1;
			int multiplier = (int) Math.ceil(day / ConfigHandler.hordeSpawnDays);
			data.setNextDay((day * multiplier) + world.random.nextInt(ConfigHandler.hordeSpawnVariation + 1));
		}
		return data;
	}

	public static HordeWorldData getCleanData(World world) {
		HordeWorldData data = new HordeWorldData();
		data.world = world;
		int day = Math.round(world.getDayTime()/ConfigHandler.dayLength);
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
		return super.toString() + "[worldTime: " + world.getDayTime() + ", nextDay="+nextDay+"]";
	}

}
