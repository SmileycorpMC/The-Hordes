package net.smileycorp.hordes.infection.jei;

import com.google.common.collect.Lists;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.util.ResourceLocation;
import net.smileycorp.hordes.common.Constants;

@JeiPlugin
public class JEIPluginInfection implements IModPlugin {

	@Override
	public void registerCategories(IRecipeCategoryRegistration registry) {
		registry.addRecipeCategories(new InfectionCureCategory(registry.getJeiHelpers().getGuiHelper()));
	}

	@Override
	public void registerRecipes(IRecipeRegistration registry) {
		registry.addRecipes(Lists.newArrayList(new InfectionCureWrapper()), InfectionCureCategory.ID);
	}

	@Override
	public ResourceLocation getPluginUid() {
		return Constants.loc("net/smileycorp/hordes");
	}


}
