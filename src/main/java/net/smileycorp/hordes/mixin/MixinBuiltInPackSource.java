package net.smileycorp.hordes.mixin;

import net.minecraft.client.resources.ClientPackSource;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.BuiltInPackSource;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.resource.PathPackResources;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;
import java.util.function.Consumer;

@Mixin(BuiltInPackSource.class)
public class MixinBuiltInPackSource {

	@Inject(at = @At("TAIL"), method = "loadPacks", cancellable = true)
	private void loadPacks(Consumer<Pack> packConsumer, CallbackInfo callback) {
		Path pack = FMLPaths.GAMEDIR.get().resolve("config").resolve("hordes");
		PathPackResources resources = new PathPackResources("hordes-config", true, pack);
		packConsumer.accept(Pack.readMetaAndCreate("hordes-config", Component.literal("Hordes Config"), true,
				(str)->resources, (Object)this instanceof ServerPacksSource ? PackType.SERVER_DATA : PackType.CLIENT_RESOURCES, Pack.Position.TOP, PackSource.BUILT_IN));
	}

}
