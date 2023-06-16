package net.smileycorp.hordes.integration.jei;

import net.minecraft.world.item.ItemStack;
import net.smileycorp.hordes.common.infection.HordesInfection;

import java.util.List;

public class InfectionCureWrapper {

	public List<ItemStack> getItems() {
		return HordesInfection.getCureList();
	}

}
