package net.smileycorp.hordes.integration.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.smileycorp.hordes.common.Constants;

import java.util.List;

@SuppressWarnings("removal")
public class InfectionCureCategory implements IRecipeCategory<InfectionCureWrapper> {

	public static final ResourceLocation ID = Constants.loc("infection");

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
	public BaseComponent getTitle() {
		return new TranslatableComponent("jei.category.hordes.InfectionCures");
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

	@Override
	public void draw(InfectionCureWrapper recipe, PoseStack matrixStack, double mouseX, double mouseY) {
		/*Minecraft mc = Minecraft.getInstance();
		Font font = mc.font;
		MutableComponent text = new TranslatableComponent("jei.category.hordes.InfectionCures").setStyle(Style.EMPTY.withBold(true).withColor(TextColor.fromRgb(0x440002)));
		font.draw(matrixStack, text, 0, 0, 0);
		font.drawShadow(matrixStack, text, 0, 0, 0);*/
	}

}
