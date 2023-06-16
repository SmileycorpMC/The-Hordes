package net.smileycorp.hordes.mixin;

import net.minecraft.server.packs.FolderPackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraftforge.fml.loading.FMLPaths;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Mixin(PackRepository.class)
public class MixinPackRepository {

	@Inject(at = @At("TAIL"), method = "openAllSelected()Ljava/util/List;", cancellable = true)
	private void openAllSelected(CallbackInfoReturnable<List<PackResources>> callback) {
		List<PackResources> packs = new ArrayList<PackResources>();
		packs.addAll(callback.getReturnValue());
		File pack = FMLPaths.GAMEDIR.get().resolve("config").resolve("hordes").toFile();
		packs.add(new FolderPackResources(pack));
		callback.setReturnValue(packs);
	}

}
