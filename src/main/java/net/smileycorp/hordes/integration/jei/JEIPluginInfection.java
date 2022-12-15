package net.smileycorp.hordes.integration.jei;

import java.util.ArrayList;
import java.util.List;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.common.recipes.RecipeManager;
import mezz.jei.common.recipes.RecipeManagerInternal;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.smileycorp.hordes.common.ModDefinitions;

@JeiPlugin
public class JEIPluginInfection implements IModPlugin {

	public static IJeiHelpers jeiHelpers;

	private static InfectionCureCategory infectionCategory;
	private static List<InfectionCureWrapper> recipes = new ArrayList<InfectionCureWrapper>();
	private static RecipeManager recipeManager = null;

	@Override
	public void registerCategories(IRecipeCategoryRegistration registry) {
		jeiHelpers = registry.getJeiHelpers();
		IGuiHelper guiHelper = jeiHelpers.getGuiHelper();
		infectionCategory = new InfectionCureCategory(guiHelper);
		registry.addRecipeCategories(infectionCategory);
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime runtime) {
		if (runtime.getRecipeManager() instanceof RecipeManager) {
			recipeManager = (RecipeManager) runtime.getRecipeManager();
		}
	}

	public static void setRecipes(List<ItemStack> cures) {
		if (recipeManager != null) {
			RecipeManagerInternal manager = ObfuscationReflectionHelper.getPrivateValue(RecipeManager.class, recipeManager , "internal");
			if (manager!=null) {
				manager.hideRecipes(InfectionCureCategory.TYPE, recipes);
				recipes.clear();
				List<ItemStack> items = new ArrayList<ItemStack>();
				for (ItemStack stack : cures) {
					items.add(stack);
					if (items.size() == 54) {
						recipes.add(new InfectionCureWrapper(items));
						items = new ArrayList<ItemStack>();
					}
				}
				if (!items.isEmpty()) {
					recipes.add(new InfectionCureWrapper(items));
				}
				recipeManager.addRecipes(InfectionCureCategory.TYPE, recipes);
			}
		}
	}

	@Override
	public ResourceLocation getPluginUid() {
		return ModDefinitions.getResource("infection");
	}


}
