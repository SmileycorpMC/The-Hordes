package net.smileycorp.hordes.infection.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.smileycorp.hordes.common.Constants;

import java.util.List;

public class InfectionCureCategory implements IRecipeCategory<InfectionCureWrapper> {

	public static final RecipeType<InfectionCureWrapper> TYPE = RecipeType.create(Constants.MODID, "infection_cures", InfectionCureWrapper.class);

	private final IDrawable background;
	private final IDrawable icon;

	public static final ResourceLocation BACKGROUND_TEXTURE = Constants.loc("textures/gui/jei/cure_list.png");

	public InfectionCureCategory(IGuiHelper guiHelper) {
		background = guiHelper.createDrawable(BACKGROUND_TEXTURE, 0, 0, 167, 113);
		icon = guiHelper.createDrawable(BACKGROUND_TEXTURE, 0, 113, 18, 18);
	}

	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public MutableComponent getTitle() {
		return Component.translatable("jei.category.hordes.InfectionCures");
	}

	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder layout, InfectionCureWrapper recipe, IFocusGroup focuses) {
		List<ItemStack> items = recipe.getItems();
		for (int i = 0; i < items.size(); i++) layout.addSlot(RecipeIngredientRole.INPUT,
				(i%9)*18+4, Math.floorDiv(i, 9)*18+4).addItemStack(items.get(i));
	}

	@Override
	public RecipeType<InfectionCureWrapper> getRecipeType() {
		return TYPE;
	}

}
