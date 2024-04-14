package net.smileycorp.hordes.infection.jei;

import net.minecraft.world.item.ItemStack;
import net.smileycorp.hordes.infection.HordesInfection;

import java.util.List;

public class InfectionCureWrapper {
	
	public List<ItemStack> getItems() {
		return HordesInfection.getCureList();
	}
	
}