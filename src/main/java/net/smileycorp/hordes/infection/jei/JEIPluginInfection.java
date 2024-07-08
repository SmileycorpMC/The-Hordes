package net.smileycorp.hordes.infection.jei;

import com.google.common.collect.Lists;
import mezz.jei.api.*;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import mezz.jei.recipes.RecipeRegistry;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

@JEIPlugin
public class JEIPluginInfection implements IModPlugin {

	public static IJeiHelpers jeiHelpers;
	public static IIngredientRegistry ingredientRegistry = null;
	private static InfectionCureCategory infectionCategory;
	private static List<InfectionCureWrapper> recipes = Lists.newArrayList();
	private static RecipeRegistry recipeRegistry = null;

	@Override
	public void registerCategories(IRecipeCategoryRegistration registry) {
		jeiHelpers = registry.getJeiHelpers();
		IGuiHelper guiHelper = jeiHelpers.getGuiHelper();
		infectionCategory = new InfectionCureCategory(guiHelper);
		registry.addRecipeCategories(infectionCategory);
	}

	@Override
	public void register(@Nonnull IModRegistry registry) {
		ingredientRegistry = registry.getIngredientRegistry();
		registry.handleRecipes(InfectionCureWrapper.class, r -> r, InfectionCureCategory.ID);
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime runtime) {
		if (runtime.getRecipeRegistry() instanceof RecipeRegistry) recipeRegistry = (RecipeRegistry) runtime.getRecipeRegistry();
	}

	@SuppressWarnings("deprecation")
	public static void setRecipes(List<ItemStack> cures) {
		if (recipeRegistry != null) {
			for (InfectionCureWrapper recipe : recipes) recipeRegistry.removeRecipe(recipe, InfectionCureCategory.ID);
			recipes.clear();
			List<ItemStack> items = Lists.newArrayList();
			for (ItemStack stack : cures) {
				items.add(stack);
				if (items.size() == 54) {
					recipes.add(new InfectionCureWrapper(items));
					ingredientRegistry.addIngredientsAtRuntime(VanillaTypes.ITEM, items);
					items = new ArrayList<>();
				}
			}
			if (!items.isEmpty()) {
				recipes.add(new InfectionCureWrapper(items));
				ingredientRegistry.addIngredientsAtRuntime(VanillaTypes.ITEM, items);
			}
			for (InfectionCureWrapper recipe : recipes) {
				recipeRegistry.addRecipe(recipe, InfectionCureCategory.ID);
			}
		}
	}


}
