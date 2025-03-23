package team.teampotato.ruok.mixin.minecraft.gui;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import team.teampotato.ruok.config.RuOK;
import team.teampotato.ruok.gui.modern.MainScreen;
import team.teampotato.ruok.util.ModLoadState;

import java.util.function.Supplier;

@Mixin(OptionsScreen.class)
public abstract class OptionsScreenMixin extends Screen {

    @Shadow protected abstract Button openScreenButton(Component message, Supplier<Screen> screenSupplier);

    @Shadow @Final
    public Options options;

    protected OptionsScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init()V", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/layouts/GridLayout$RowHelper;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;)Lnet/minecraft/client/gui/layouts/LayoutElement;",
            shift = At.Shift.AFTER, ordinal = 9)
    )
    private void onInit(CallbackInfo ci, @Local GridLayout.RowHelper adder) {
        // 如果玩家加载了Sodium模组，且不希望使用VanillaGui，显示ListScreen
        if(RuOK.get().UseAui || !ModLoadState.isSodium()){
            adder.addChild(this.openScreenButton(Component.translatable("ruok.options.gui.ruok"), () -> new MainScreen(Component.empty(), this)));
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        final int KEY_O = InputConstants.KEY_O; // 目标按键

        if (keyCode == KEY_O && Screen.hasShiftDown() && Screen.hasAltDown()) {
            RuOK.get().UseAui =!RuOK.get().UseAui;
            RuOK.save();
            this.rebuildWidgets();
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
