package net.smileycorp.hordes.integration.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.smileycorp.atlas.api.util.TextUtils;
import net.smileycorp.hordes.common.Constants;

import java.util.List;

public class InfectionCureCategory implements IRecipeCategory<InfectionCureWrapper> {

	public static final RecipeType<InfectionCureWrapper> TYPE = RecipeType.create(Constants.MODID, "infection_cures", InfectionCureWrapper.class);

	private final IDrawable background;
	private final IDrawable icon;

	public static final ResourceLocation TEXTURE = Constants.loc("textures/gui/jei/cure_list.png");

	public InfectionCureCategory(IGuiHelper guiHelper) {
		background = guiHelper.createDrawable(TEXTURE, 0, 0, 167, 113);
		icon = guiHelper.createDrawable(TEXTURE, 168, 0, 18, 18);
	}

	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public MutableComponent getTitle() {
		return TextUtils.translatableComponent("jei.category.hordes.InfectionCures", "Infection Cures");
	}

	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder recipeLayout, InfectionCureWrapper recipe, IFocusGroup focuses) {
		List<ItemStack> items = recipe.getItems();
		for (int i = 0; i < items.size(); i++) {
			recipeLayout.addSlot(RecipeIngredientRole.INPUT, (i%9)*18+3, Math.floorDiv(i, 9)*18+3).addItemStack(items.get(i));
		}
	}

	@Override
	public RecipeType<InfectionCureWrapper> getRecipeType() {
		return TYPE;
	}

}
