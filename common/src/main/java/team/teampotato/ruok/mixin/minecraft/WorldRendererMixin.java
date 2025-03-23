package team.teampotato.ruok.mixin.minecraft;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import team.teampotato.ruok.config.RuOK;
import team.teampotato.ruok.util.render.EntityRender;

import java.util.SortedSet;


@Mixin(value = LevelRenderer.class, priority = 1200)
public abstract class WorldRendererMixin {


    @Shadow private int ticks;

    @Shadow @Final private Long2ObjectMap<SortedSet<BlockDestructionProgress>> destructionProgress;


    @Shadow @Final
    private Int2ObjectMap<BlockDestructionProgress> destroyingBlocks;

    @Shadow
    protected abstract void removeProgress(BlockDestructionProgress info);


    @Inject(method = "renderEntity", at = @At("HEAD"), cancellable = true)
    private void onRender(Entity entity, double d, double e, double f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, CallbackInfo ci) {
        EntityRender.entityCull(entity,ci);
    }

    @Inject(method = "tick", at = @At("RETURN"), cancellable = true)
    private void onTick(CallbackInfo ci) {
        if (!RuOK.get().TickPerformance) {
            return;
        }
        ci.cancel();

        if ((++this.ticks & 0b11111) == 0 && !this.destroyingBlocks.isEmpty()) { // 20 的优化为位运算
            this.destroyingBlocks.entrySet().removeIf(entry -> {
                BlockDestructionProgress info = entry.getValue();
                if (this.ticks - info.getUpdatedRenderTick() > 400) {
                    this.removeProgress(info);
                    return true;
                }
                return false;
            });
        }
    }
}







