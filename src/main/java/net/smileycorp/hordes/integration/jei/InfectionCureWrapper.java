package net.smileycorp.hordes.integration.jei;

import java.util.List;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.item.ItemStack;

public class InfectionCureWrapper {

	private final List<ItemStack> items;

	public InfectionCureWrapper(List<ItemStack> items) {
		this.items = items;
	}

	public void getIngredients(IIngredients ingredients) {
		ingredients.setInputs(VanillaTypes.ITEM, items);
	}

}
