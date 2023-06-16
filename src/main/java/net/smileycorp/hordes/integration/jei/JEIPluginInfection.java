package net.smileycorp.hordes.integration.jei;

import com.google.common.collect.Lists;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.smileycorp.hordes.common.Constants;

@JeiPlugin
public class JEIPluginInfection implements IModPlugin {

	@Override
	public void registerCategories(IRecipeCategoryRegistration registry) {
		registry.addRecipeCategories(new InfectionCureCategory(registry.getJeiHelpers().getGuiHelper()));
	}

	@Override
	public void registerRecipes(IRecipeRegistration registry) {
		registry.addRecipes(InfectionCureCategory.TYPE, Lists.newArrayList(new InfectionCureWrapper()));
	}

	@Override
	public ResourceLocation getPluginUid() {
		return Constants.loc("infection");
	}


}
