package net.smileycorp.hordes.integration.jei;

import com.google.common.collect.Lists;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.smileycorp.hordes.common.infection.HordesInfection;
import net.smileycorp.hordes.common.infection.InfectionRegister;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("removal")
public class InfectionCureWrapper {

	public void getIngredients(IIngredients ingredients) {
		ingredients.setInputs(VanillaTypes.ITEM, InfectionRegister.getCureList());
	}

}
