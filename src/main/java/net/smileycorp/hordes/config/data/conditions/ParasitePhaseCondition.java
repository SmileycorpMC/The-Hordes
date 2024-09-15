package net.smileycorp.hordes.config.data.conditions;

import com.dhanantry.scapeandrunparasites.world.SRPSaveData;
import com.google.gson.JsonElement;
import net.darkhax.gamestages.GameStageHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.config.data.DataType;
import net.smileycorp.hordes.config.data.values.ValueGetter;

import java.util.Random;

public class ParasitePhaseCondition implements Condition {

	protected ValueGetter<Integer> phase;

	public ParasitePhaseCondition(ValueGetter<Integer> phase) {
		this.phase = phase;
	}

	@Override
	public boolean apply(World level, EntityLivingBase entity, EntityPlayerMP player, Random rand) {
		return phase.get(level, entity, player, rand) >=
				SRPSaveData.get(level).getEvolutionPhase(level.provider.getDimension());
	}

	public static ParasitePhaseCondition deserialize(JsonElement json) {
		try {
			return new ParasitePhaseCondition(ValueGetter.readValue(DataType.INT, json));
		} catch(Exception e) {
			HordesLogger.logError("Incorrect parameters for condition srparasites:phase", e);
		}
		return null;
	}

}
