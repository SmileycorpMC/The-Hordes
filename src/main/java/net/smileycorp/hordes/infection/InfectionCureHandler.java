package net.smileycorp.hordes.infection;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.smileycorp.atlas.api.util.RecipeUtils;

public class InfectionCureHandler {
	
	private static List<ItemStack> cures = new ArrayList<ItemStack>();
	
	static List<ItemStack> getCureList() {
		List<ItemStack> result = new ArrayList<ItemStack>();
		for (ItemStack stack : cures) {
			result.add(new ItemStack(stack.getItem(), stack.getMetadata()));
		}
		return result;
	}
	
	public static void addCureItem(ItemStack stack) {
		cures.add(stack);
	}
	
	public static void removeCureItem(ItemStack stack) {
		for (ItemStack match : cures) {
			if (RecipeUtils.compareItemStacks(match, stack, true)) {
				cures.remove(match);
			}
		}
	}
}
