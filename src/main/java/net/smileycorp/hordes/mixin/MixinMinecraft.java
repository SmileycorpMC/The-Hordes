package net.smileycorp.hordes.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.FolderResourcePack;
import net.minecraft.client.resources.IResourcePack;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.config.data.ConfigDataManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Minecraft.class)
public class MixinMinecraft {

    @Shadow
    @Final
    private List<IResourcePack> defaultResourcePacks;

    //generate the config folder
    //then set it as a locked resource pack
    @Inject(at = @At("HEAD"), method = "init")
    public void hordes$init(CallbackInfo callback) {
        try {
            ConfigDataManager.init();
            defaultResourcePacks.add(new FolderResourcePack(ConfigDataManager.CONFIG_FOLDER.toFile()));
        } catch (Exception e) {
            HordesLogger.logError("Failed loading config resources", e);
        }
    }

}
