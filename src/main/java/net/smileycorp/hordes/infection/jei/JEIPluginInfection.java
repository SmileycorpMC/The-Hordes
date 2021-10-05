package net.smileycorp.hordes.infection.jei;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import mezz.jei.recipes.RecipeRegistry;
import net.minecraft.item.ItemStack;

@JEIPlugin
public class JEIPluginInfection implements IModPlugin {
	
	public static IJeiHelpers jeiHelpers;
	public static IIngredientRegistry ingredientRegistry = null;
	
	private static InfectionCureCategory infectionCategory;
	private static List<InfectionCureWrapper> recipes = new ArrayList<InfectionCureWrapper>();
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
		registry.handleRecipes(InfectionCureWrapper.class, (r) -> r, InfectionCureCategory.ID);
	}
	
	@Override
	public void onRuntimeAvailable(IJeiRuntime runtime) {
		if (runtime.getRecipeRegistry() instanceof RecipeRegistry) {
			recipeRegistry = (RecipeRegistry) runtime.getRecipeRegistry();
		}
	}

	public static void setRecipes(List<ItemStack> cures) {
		if (recipeRegistry != null) {
			for (InfectionCureWrapper recipe : recipes) recipeRegistry.removeRecipe(recipe, InfectionCureCategory.ID); 
			recipes.clear();
			List<ItemStack> items = new ArrayList<ItemStack>();
			for (ItemStack stack : cures) {
				items.add(stack);
				if (items.size() == 54) {
					recipes.add(new InfectionCureWrapper(items));
					ingredientRegistry.addIngredientsAtRuntime(ItemStack.class, items);
					items = new ArrayList<ItemStack>();
				}
			}
			if (!items.isEmpty()) {
				recipes.add(new InfectionCureWrapper(items));
				ingredientRegistry.addIngredientsAtRuntime(ItemStack.class, items);
			}
			for (InfectionCureWrapper recipe : recipes) {
				recipeRegistry.addRecipe(recipe, InfectionCureCategory.ID); 
			}
		}
	}
	
	
}
