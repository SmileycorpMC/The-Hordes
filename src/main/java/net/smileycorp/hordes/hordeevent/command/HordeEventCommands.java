package net.smileycorp.hordes.hordeevent.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.smileycorp.hordes.common.CommonConfigHandler;
import net.smileycorp.hordes.common.Constants;

@EventBusSubscriber(modid=Constants.MODID)
public class HordeEventCommands {

	@SubscribeEvent
	public static void registerCommands(RegisterCommandsEvent event) {
		if (CommonConfigHandler.enableHordeEvent.get()) {
			CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
			LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("hordes");
			CommandSpawnWave.register(command);
			CommandStartHordeEvent.register(command);
			CommandStopHordeEvent.register(command);
			CommandDebugHordeEvent.register(command);
			CommandResetHordeEvent.register(command);
			dispatcher.register(command);
		}
	}

}
