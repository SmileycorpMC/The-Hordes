package net.smileycorp.hordes.integration.jei;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.world.item.ItemStack;

import java.util.List;

@SuppressWarnings("removal")
public class InfectionCureWrapper {

	private final List<ItemStack> items;

	public InfectionCureWrapper(List<ItemStack> items) {
		this.items = items;
	}

	public void getIngredients(IIngredients ingredients) {
		ingredients.setInputs(VanillaTypes.ITEM, items);
	}

}
