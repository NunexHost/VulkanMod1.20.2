package net.vulkanmod.mixin.screen;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.options.GameOptions; // Updated for 1.20.2
import net.vulkanmod.config.OptionScreenV;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(OptionsScreen.class)
public abstract class OptionsScreenM extends Screen {

    @Shadow @Final private Screen lastScreen;
    @Shadow @Final private GameOptions options; // Updated for 1.20.2

    protected OptionsScreenM(Component title) {
        super(title);
    }

    @Inject(method = "createVideoOptions", at = @At("HEAD"), cancellable = true) // Updated method name for 1.20.2
    private void injectVideoOptionScreen(CallbackInfoReturnable<Screen> cir) {
        cir.setReturnValue(new OptionScreenV(Component.literal("Video Setting"), this));
    }
}
