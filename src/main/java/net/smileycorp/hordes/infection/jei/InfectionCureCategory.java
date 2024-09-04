package net.smileycorp.hordes.infection.jei;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.smileycorp.hordes.common.Constants;

import java.util.List;

@SuppressWarnings("removal")
public class InfectionCureCategory implements IRecipeCategory<InfectionCureWrapper> {

	public static final ResourceLocation ID = Constants.loc("net/smileycorp/hordes");

	private final IDrawable background;
	private final IDrawable icon;

	public static final ResourceLocation TEXTURE = Constants.loc("textures/gui/jei/cure_list.png");

	public InfectionCureCategory(IGuiHelper guiHelper) {
		background = guiHelper.createDrawable(TEXTURE, 0, 0, 167, 113);
		icon = guiHelper.createDrawable(TEXTURE, 0, 113, 18, 18);
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
	public String getTitle() {
		return new TranslationTextComponent("jei.category.hordes.InfectionCures").getString();
	}

	@Override
	public IDrawable getIcon() {
		return icon;
	}
	
	@Override
	public void setIngredients(InfectionCureWrapper recipe, IIngredients ingredients) {
		ingredients.setInputs(VanillaTypes.ITEM, recipe.getItems());
	}
	
	
	@Override
	public void setRecipe(IRecipeLayout layout, InfectionCureWrapper recipe, IIngredients ingredients) {
		IGuiItemStackGroup stacks = layout.getItemStacks();
		List<List<ItemStack>> items = ingredients.getInputs(VanillaTypes.ITEM);
		for (int i = 0; i < items.size(); i++) {
			stacks.init(i, false, (i%9)*18+4, Math.floorDiv(i, 9)*18+4);
			stacks.set(i, items.get(i));
		}
	}

}
