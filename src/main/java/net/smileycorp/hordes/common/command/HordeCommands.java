package net.smileycorp.hordes.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.config.HordeEventConfig;
import net.smileycorp.hordes.hordeevent.command.*;

@Mod.EventBusSubscriber(modid= Constants.MODID)
public class HordeCommands {

	@SubscribeEvent
	public static void registerCommands(RegisterCommandsEvent event) {
		CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
		LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("hordes");
		CommandSpawnZombie.register(command);
		CommandListEntities.register(command);
		if (HordeEventConfig.enableHordeEvent.get()) {
			CommandSpawnWave.register(command);
			CommandStartHordeEvent.register(command);
			CommandStopHordeEvent.register(command);
			CommandDebugHordeEvent.register(command);
			CommandResetHordeEvent.register(command);
		}
		dispatcher.register(command);
	}

}
