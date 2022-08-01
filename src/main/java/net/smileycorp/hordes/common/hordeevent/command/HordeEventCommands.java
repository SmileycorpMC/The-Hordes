package net.smileycorp.hordes.common.hordeevent.command;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.command.CommandSource;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.smileycorp.hordes.common.CommonConfigHandler;
import net.smileycorp.hordes.common.ModDefinitions;

@EventBusSubscriber(modid=ModDefinitions.MODID)
public class HordeEventCommands {

	@SubscribeEvent
	public static void registerCommands(RegisterCommandsEvent event) {
		if (CommonConfigHandler.enableHordeEvent.get()) {
			CommandDispatcher<CommandSource> dispatcher = event.getDispatcher();
			CommandSpawnWave.register(dispatcher);
			CommandStartHordeEvent.register(dispatcher);
			CommandStopHordeEvent.register(dispatcher);
			CommandDebugHordeEvent.register(dispatcher);
		}
	}

}
