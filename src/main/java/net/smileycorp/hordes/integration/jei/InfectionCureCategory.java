package net.smileycorp.hordes.integration.jei;

import java.util.List;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.smileycorp.hordes.common.ModDefinitions;
import net.smileycorp.hordes.infection.PotionInfected;

@SuppressWarnings("deprecation")
public class InfectionCureCategory implements IRecipeCategory<InfectionCureWrapper> {

	public static final String ID = ModDefinitions.getName("infection");
	
	private final IDrawable background;
	private final IDrawable icon;
	
	public static final ResourceLocation TEXTURE = ModDefinitions.getResource("textures/gui/jei/cure_list.png");
	
	public InfectionCureCategory(IGuiHelper guiHelper) {
		this.background = guiHelper.createDrawable(TEXTURE, 0, 0, 167, 113);
		this.icon = guiHelper.createDrawable(PotionInfected.TEXTURE, 1, 200, 16, 16);
	}
	
	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public String getModName() {
		return ModDefinitions.modid;
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
		for (int i = 0; i <6; i++) {
			for (int j = 0; j <9; j++) {
				items.init((i*9)+j, false, j*18+3, i*18+3);
			}
		}
		List<List<ItemStack>> stacks = ingredients.getInputs(ItemStack.class);
		for (int i = 0; i < stacks.size(); i++) {
			items.set(i, stacks.get(i));
		}
		
	}

}
