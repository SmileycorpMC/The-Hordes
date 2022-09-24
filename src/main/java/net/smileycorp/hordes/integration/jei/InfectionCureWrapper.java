package net.smileycorp.hordes.integration.jei;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.world.item.ItemStack;

public class InfectionCureWrapper {

	private final List<List<ItemStack>> items = Lists.newArrayList();

	public InfectionCureWrapper(List<ItemStack> items) {
		for (int i = 0; i < items.size() / 9f; i++) {
			this.items.add(items.subList(i, Math.min(i+8, items.size())));
		}
	}

	public ItemStack getItem(int x, int y) {
		if (items.size() < y) {
			List<ItemStack> sublist = items.get(y);
			if (sublist.size() < x) {
				return sublist.get(x);
			}
		}
		return ItemStack.EMPTY;
	}

}
