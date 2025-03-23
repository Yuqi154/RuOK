package team.teampotato.ruok.mixin.minecraft;

import com.google.common.collect.EvictingQueue;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TrackingEmitter;
import net.minecraft.core.particles.ParticleOptions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import team.teampotato.ruok.config.RuOK;
import team.teampotato.ruok.util.render.ParticleRender;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;

@Mixin(ParticleEngine.class)
public abstract class ParticleManagerMixin {
    @Redirect(
            method = "createParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)Lnet/minecraft/client/particle/Particle;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/particle/ParticleEngine;add(Lnet/minecraft/client/particle/Particle;)V"
            )
    )
    private <T extends ParticleOptions> void onAddParticle(ParticleEngine instance, Particle particle, T parameters, double d, double e, double f, double g, double h, double i) {

        ParticleRender.onParticleCull(instance,particle,parameters,d,e,f);
    }

    @Inject(
            method = "add(Lnet/minecraft/client/particle/Particle;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onAddParticle(Particle particle, CallbackInfo ci) {
        if(RuOK.get().TickPerformance) {
            RuOK1211$particleTick();
            // 取消后续执行，确保只在注入点执行优化后的逻辑
            ci.cancel();
        }
    }

    @Shadow @Final private Map<ParticleRenderType, Queue<Particle>> particles;

    @Shadow protected ClientLevel level;

    @Shadow protected abstract void tickParticleList(Collection<Particle> particles);

    @Shadow @Final private Queue<TrackingEmitter> trackingEmitters;

    @Shadow @Final
    private Queue<Particle> particlesToAdd;

    @Shadow
    @Final private static int MAX_PARTICLES_PER_LAYER;


    @Unique
    private void RuOK1211$particleTick() {
        // 合并粒子处理，减少 Map 遍历次数
        if (!this.particles.isEmpty()) {
            for (Map.Entry<ParticleRenderType, Queue<Particle>> entry : this.particles.entrySet()) {
                this.level.getProfiler().push(entry.getKey().toString());
                this.tickParticleList(entry.getValue());  // 处理粒子
                this.level.getProfiler().pop();
            }
        }

        // 优化 newEmitterParticles 更新，减少对象创建和多次遍历
        if (!this.trackingEmitters.isEmpty()) {
            Iterator<TrackingEmitter> emitterIterator = this.trackingEmitters.iterator();
            while (emitterIterator.hasNext()) {
                TrackingEmitter emitterParticle = emitterIterator.next();
                emitterParticle.tick();
                if (!emitterParticle.isAlive()) {
                    emitterIterator.remove();  // 直接移除不存活的粒子
                }
            }
        }

        // 优化 newParticles 处理：减少重复计算
        if (!this.particlesToAdd.isEmpty()) {
            Particle particle;
            while ((particle = this.particlesToAdd.poll()) != null) {
                // 计算粒子类型对应的队列
                // 如果队列已经存在，就直接使用，避免重复创建 EvictingQueue
                Queue<Particle> particleQueue = this.particles.get(particle.getRenderType());
                if (particleQueue == null) {
                    particleQueue = EvictingQueue.create(MAX_PARTICLES_PER_LAYER);
                    this.particles.put(particle.getRenderType(), particleQueue);  // 缓存队列，避免重复创建
                }
                particleQueue.add(particle);
            }
        }
    }


}
