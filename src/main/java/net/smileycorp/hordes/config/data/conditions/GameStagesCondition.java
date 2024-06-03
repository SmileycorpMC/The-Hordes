package net.smileycorp.hordes.config.data.conditions;

import com.google.gson.JsonElement;
import net.darkhax.gamestages.GameStageHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.config.data.DataType;
import net.smileycorp.hordes.config.data.values.ValueGetter;

import java.util.Random;

public class GameStagesCondition implements Condition {

	protected ValueGetter<String> stage;

	public GameStagesCondition(ValueGetter<String> stage) {
		this.stage = stage;
	}

	@Override
	public boolean apply(World level, EntityLivingBase entity, EntityPlayerMP player, Random rand) {
		return GameStageHelper.hasStage(player, stage.get(level, entity, player, rand));
	}

	public static GameStagesCondition deserialize(JsonElement json) {
		try {
			return new GameStagesCondition(ValueGetter.readValue(DataType.STRING, json));
		} catch(Exception e) {
			HordesLogger.logError("Incorrect parameters for condition gamestages:gamestage", e);
		}
		return null;
	}

}
