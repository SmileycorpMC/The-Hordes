package net.smileycorp.hordes.integration.jei;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;

import java.util.List;

public class InfectionCureWrapper implements IRecipeWrapper {

	private final List<ItemStack> items;

	public InfectionCureWrapper(List<ItemStack> items) {
		this.items = items;
	}

	@Override
	public void getIngredients(IIngredients ingredients) {
		ingredients.setInputs(VanillaTypes.ITEM, items);
	}

}
