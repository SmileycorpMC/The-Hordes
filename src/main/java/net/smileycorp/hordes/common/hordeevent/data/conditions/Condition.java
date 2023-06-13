package net.smileycorp.hordes.common.hordeevent.data.conditions;

import java.util.Random;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public interface Condition {

	public boolean apply(Level level, Player player, Random rand);

}
