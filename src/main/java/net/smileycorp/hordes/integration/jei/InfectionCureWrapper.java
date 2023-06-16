package net.smileycorp.hordes.integration.jei;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredients;
import net.smileycorp.hordes.common.infection.InfectionRegister;

@SuppressWarnings("removal")
public class InfectionCureWrapper {

	public void getIngredients(IIngredients ingredients) {
		ingredients.setInputs(VanillaTypes.ITEM, InfectionRegister.getCureList());
	}

}
