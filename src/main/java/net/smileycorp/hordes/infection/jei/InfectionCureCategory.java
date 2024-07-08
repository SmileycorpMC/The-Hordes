package net.smileycorp.hordes.infection.jei;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.smileycorp.hordes.common.Constants;

import java.util.List;

@SuppressWarnings("deprecation")
public class InfectionCureCategory implements IRecipeCategory<InfectionCureWrapper> {

	public static final String ID = Constants.name("infection");
	
	private final IDrawable background;
	private final IDrawable icon;
	
	public static final ResourceLocation TEXTURE = Constants.loc("textures/gui/jei/cure_list.png");
	
	public InfectionCureCategory(IGuiHelper guiHelper) {
		background = guiHelper.createDrawable(TEXTURE, 0, 0, 167, 113);
		icon = guiHelper.createDrawable(Constants.loc("textures/mob_effect/infected.png"), 1, 1, 16, 16, 18, 18);
	}
	
	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public String getModName() {
		return Constants.MODID;
	}

	@Override
	public String getTitle() {
		return I18n.translateToLocal("jei.category.hordes.InfectionCures").trim();
	}

	@Override
	public String getUid() {
		return ID;
	}
	
	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, InfectionCureWrapper recipeWrapper, IIngredients ingredients) {
		IGuiItemStackGroup items = recipeLayout.getItemStacks();
		for (int i = 0; i <6; i++) for (int j = 0; j <9; j++) items.init((i * 9) + j, false, j * 18 + 3, i * 18 + 3);
		List<List<ItemStack>> stacks = ingredients.getInputs(ItemStack.class);
		for (int i = 0; i < stacks.size(); i++) items.set(i, stacks.get(i));
	}

}
