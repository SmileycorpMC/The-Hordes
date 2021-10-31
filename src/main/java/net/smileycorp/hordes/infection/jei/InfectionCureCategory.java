package net.smileycorp.hordes.infection.jei;

import java.util.List;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.smileycorp.hordes.common.ModDefinitions;
import net.smileycorp.hordes.infection.InfectedEffect;

public class InfectionCureCategory implements IRecipeCategory<InfectionCureWrapper> {

	public static final ResourceLocation ID = ModDefinitions.getResource("infection");

	private final IDrawable background;
	private final IDrawable icon;

	public static final ResourceLocation TEXTURE = ModDefinitions.getResource("textures/gui/jei/cure_list.png");

	public InfectionCureCategory(IGuiHelper guiHelper) {
		this.background = guiHelper.createDrawable(TEXTURE, 0, 0, 167, 113);
		this.icon = guiHelper.createDrawable(InfectedEffect.TEXTURE, 1, 200, 16, 16);
	}

	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public Class<? extends InfectionCureWrapper> getRecipeClass() {
		return InfectionCureWrapper.class;
	}

	@Override
	public ResourceLocation getUid() {
		return ID;
	}

	@Override
	public void setIngredients(InfectionCureWrapper wrapper, IIngredients ingredients) {
		wrapper.getIngredients(ingredients);
	}

	@Override
	public ITextComponent getTitleAsTextComponent() {
		return new TranslationTextComponent("jei.category.hordes.InfectionCures");
	}

	@Override
	public String getTitle() {
		return "jei.category.hordes.InfectionCures";
	}

	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, InfectionCureWrapper recipeWrapper, IIngredients ingredients) {
		IGuiItemStackGroup items = recipeLayout.getItemStacks();
		for (int i = 0; i <6; i++) {
			for (int j = 0; j <9; j++) {
				items.init((i*9)+j, false, j*18+3, i*18+3);
			}
		}
		List<List<ItemStack>> stacks = ingredients.getInputs(VanillaTypes.ITEM);
		for (int i = 0; i < stacks.size(); i++) {
			items.set(i, stacks.get(i));
		}

	}

}
