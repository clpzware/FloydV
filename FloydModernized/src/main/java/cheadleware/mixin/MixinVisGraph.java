package cheadleware.mixin;

import cheadleware.Cheadleware;
import cheadleware.module.modules.Render.Chams;
import cheadleware.module.modules.Render.ViewClip;
import cheadleware.module.modules.Render.Xray;
import net.minecraft.client.renderer.chunk.SetVisibility;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SideOnly(Side.CLIENT)
@Mixin({VisGraph.class})
public abstract class MixinVisGraph {
    @Inject(
            method = {"func_178606_a"},
            at = {@At("HEAD")},
            cancellable = true
    )
    private void func_178606_a(CallbackInfo callbackInfo) {
        if (Cheadleware.moduleManager != null) {
            if (Cheadleware.moduleManager.modules.get(Chams.class).isEnabled()
                    || Cheadleware.moduleManager.modules.get(ViewClip.class).isEnabled()
                    || Cheadleware.moduleManager.modules.get(Xray.class).isEnabled()) {
                callbackInfo.cancel();
            }
        }
    }

    @Inject(
            method = {"computeVisibility"},
            at = {@At("HEAD")},
            cancellable = true
    )
    private void computeVisibility(CallbackInfoReturnable<SetVisibility> callbackInfoReturnable) {
        if (Cheadleware.moduleManager != null) {
            if (Cheadleware.moduleManager.modules.get(Chams.class).isEnabled()
                    || Cheadleware.moduleManager.modules.get(ViewClip.class).isEnabled()
                    || Cheadleware.moduleManager.modules.get(Xray.class).isEnabled()) {
                SetVisibility setVisibility = new SetVisibility();
                setVisibility.setAllVisible(true);
                callbackInfoReturnable.setReturnValue(setVisibility);
            }
        }
    }
}
