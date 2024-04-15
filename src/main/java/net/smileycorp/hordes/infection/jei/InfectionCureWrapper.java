package net.smileycorp.hordes.infection.jei;

import net.minecraft.item.ItemStack;
import net.smileycorp.hordes.infection.HordesInfection;

import java.util.List;

@SuppressWarnings("removal")
public class InfectionCureWrapper {
	
	public List<ItemStack> getItems() {
		return HordesInfection.getCureList();
	}

}
