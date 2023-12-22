package net.vulkanmod.mixin.chunk;

import net.minecraft.client.renderer.culling.Frustum;
import net.vulkanmod.interfaces.FrustumMixed;
import net.vulkanmod.render.chunk.VFrustum;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Frustum.class)
public class FrustumMixin implements FrustumMixed {

    @Shadow private double camX;
    @Shadow private double camY;
    @Shadow private double camZ;
    private final VFrustum vFrustum = new VFrustum();

    @Inject(method = "calculateFrustum", at = @At("HEAD"), cancellable = true)
    private void calculateFrustum(Matrix4f modelView, Matrix4f projection, CallbackInfo ci) {
        vFrustum.calculateFrustum(modelView, projection);
        ci.cancel();
    }

    @Inject(method = "prepare", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/culling/Frustum;setPosition(DDD)V"))
    private void setCamOffset(double x, double y, double z, CallbackInfo ci) {
        vFrustum.setCamOffset(x, y, z); // Align with cam offset setting
    }

    @Override
    public VFrustum customFrustum() {
        return vFrustum;
    }
}
