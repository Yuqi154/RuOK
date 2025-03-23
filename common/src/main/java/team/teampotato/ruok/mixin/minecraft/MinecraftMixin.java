package team.teampotato.ruok.mixin.minecraft;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.util.profiling.metrics.profiling.MetricsRecorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import team.teampotato.ruok.config.RuOK;
import team.teampotato.ruok.util.EntityUtils;
import team.teampotato.ruok.util.GameSystemMonitor;
import team.teampotato.ruok.util.clazz.StartTime;
import team.teampotato.ruok.util.render.RenderUtil;


@Mixin(value = Minecraft.class)
public class MinecraftMixin {
    @Inject(method = "buildInitialScreens", at = @At("HEAD"))
    private void onInit(Minecraft.GameLoadCookie gameLoadCookie, CallbackInfoReturnable<Runnable> cir){
        StartTime.start();
    }
    @Inject(method = "buildInitialScreens", at = @At("RETURN"))
    private void onInitReturn(Minecraft.GameLoadCookie gameLoadCookie, CallbackInfoReturnable<Runnable> cir){
        GameSystemMonitor.setInitState(true);//刷新完毕,开始监测
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void onTick(CallbackInfo ci){
        if(GameSystemMonitor.getInitState()) {//添加状态,宣布有完全加载,防止出现兼容性导致线程卡死
            RenderUtil.setTickRun(true);
            GameSystemMonitor.run();
            EntityUtils.refViewEntity();
        }

    }
    @WrapOperation(
            method = "runTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/profiling/metrics/profiling/MetricsRecorder;isRecording()Z",
                    ordinal = 0
            )
    )
    private boolean onRender(MetricsRecorder instance, Operation<Boolean> original) {
        if(RuOK.get().GuiGPU) return true;
        return original.call(instance);
    }

    @WrapOperation(
            method = "runTick",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;limitDisplayFPS(I)V")
    )
    private void onRender(int fps, Operation<Void> original) {
        RenderUtil.setMaxFps(fps,original);
    }

}