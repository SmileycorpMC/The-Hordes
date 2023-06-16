package net.smileycorp.hordes.mixin;

import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.resource.PathPackResources;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Mixin(PackRepository.class)
public class MixinPackRepository {

	@Inject(at = @At("TAIL"), method = "openAllSelected()Ljava/util/List;", cancellable = true)
	private void openAllSelected(CallbackInfoReturnable<List<PackResources>> callback) {
		List<PackResources> packs = new ArrayList<PackResources>();
		packs.addAll(callback.getReturnValue());
		Path pack = FMLPaths.GAMEDIR.get().resolve("config").resolve("hordes");
		packs.add(new PathPackResources("hordes-config", true, pack));
		callback.setReturnValue(packs);
	}

}
