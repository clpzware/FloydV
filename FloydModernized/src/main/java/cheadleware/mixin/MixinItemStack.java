package cheadleware.mixin;

import cheadleware.Cheadleware;
import cheadleware.module.modules.Render.ESP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SideOnly(Side.CLIENT)
@Mixin({ItemStack.class})
public abstract class MixinItemStack {
    @Inject(
            method = {"hasEffect"},
            at = {@At("HEAD")},
            cancellable = true
    )
    private void hasEffect(CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if (Cheadleware.moduleManager != null) {
            ESP esp = (ESP) Cheadleware.moduleManager.modules.get(ESP.class);
            if (esp.isEnabled() && !esp.isGlowEnabled()) {
                callbackInfoReturnable.setReturnValue(false);
            }
        }
    }
}
