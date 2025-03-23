package team.teampotato.ruok.mixin.minecraft;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import team.teampotato.ruok.config.RuOK;
import team.teampotato.ruok.util.render.ParticleRender;

import java.util.List;
import java.util.function.Supplier;

@Mixin(ClientLevel.class)
public abstract class ClientWorldMixin extends Level {

    @Shadow public abstract List<AbstractClientPlayer> players();

    protected ClientWorldMixin(WritableLevelData properties, ResourceKey<Level> registryRef, RegistryAccess registryManager, Holder<DimensionType> dimensionEntry, Supplier<ProfilerFiller> profiler, boolean isClient, boolean debugWorld, long biomeAccess, int maxChainedNeighborUpdates) {
        super(properties, registryRef, registryManager, dimensionEntry, profiler, isClient, debugWorld, biomeAccess, maxChainedNeighborUpdates);
    }

    @Override
    public float getRainLevel(float delta) {
        return switch(RuOK.get().RenderWeather) {
            case LOW -> super.getRainLevel(delta) / 2;
            case CLOSE -> 0;
            default -> super.getRainLevel(delta);
        };
    }

    @Override
    public float getThunderLevel(float delta) {
        return switch (RuOK.get().RenderWeather) {
            case LOW -> super.getThunderLevel(delta) / 2;
            case CLOSE -> 0;
            default -> super.getThunderLevel(delta);//渲染下雪
        };
    }

    @Inject(method = "addDestroyBlockEffect", at = @At("HEAD"), cancellable = true)
    public void onAddBlockBreakParticles(BlockPos blockPos, BlockState blockState, CallbackInfo ci) {
        ParticleRender.addBlockBreakParticles(blockPos, blockState);
        ci.cancel();
    }
}