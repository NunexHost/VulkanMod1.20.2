package net.vulkanmod.mixin.screen;

import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.vulkanmod.config.OptionScreenV;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(OptionsScreen.class)
public class OptionsScreenM extends Screen {

    @Shadow @Final private Screen lastScreen;

    @Shadow @Final private Options options;

    protected OptionsScreenM(Component title) {
        super(title);
    }

    /**
     * Lazy loads OptionScreenV components.
     */
    @Inject(method = "method_19828", at = @At("HEAD"), cancellable = true)
    private void injectVideoOptionScreen(CallbackInfoReturnable<Screen> cir) {
        // Only create the OptionScreenV when it's needed.
        if (!cir.isCancellable()) {
            OptionScreenV optionScreenV = new OptionScreenV(Component.literal("Video Setting"), this);
            cir.setReturnValue(optionScreenV);
        }
    }

    /**
     * Caches frequently used GUI components or data.
     */
    @Inject(method = "method_19828", at = @At("RETURN"))
    private void injectVideoOptionScreenPost(CallbackInfoReturnable<Screen> cir) {
        // Cache the Options instance for direct access.
        OptionScreenV optionScreenV = (OptionScreenV) cir.getReturnValue();
        optionScreenV.options = this.options;
    }
}
