package net.smileycorp.hordes.integration.jei;

import com.google.common.collect.Lists;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.recipes.RecipeManager;
import mezz.jei.recipes.RecipeManagerInternal;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.smileycorp.hordes.common.Constants;

import java.util.ArrayList;
import java.util.List;

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
		return Constants.loc("infection");
	}


}
