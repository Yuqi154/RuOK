package team.teampotato.ruok.util.render;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import team.teampotato.ruok.config.RuOK;
import team.teampotato.ruok.gui.modern.mode.BlockBreakParticleType;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ParticleRender {
    private static final HashSet<ParticleType<?>> blackParticleListCache = new HashSet<>();
    private static final HashSet<ParticleType<?>> whiteParticleListCache = new HashSet<>();
    private static final Minecraft mc = Minecraft.getInstance();

    static {
        initConfigList();
    }
    // 初始化生物列表
    private static void initConfigList() {
        List<String> blackParticleConfig = RuOK.get().BlackListedParticle;
        List<String> whiteParticleConfig = RuOK.get().WhiteListedParticle;
        for (String bc : blackParticleConfig) {
            Optional<ParticleType<?>> entityTypeOpt = particleTypeGet(bc);
            entityTypeOpt.ifPresent(blackParticleListCache::add);
        }
        for (String wc : whiteParticleConfig) {
            Optional<ParticleType<?>> entityTypeOpt = particleTypeGet(wc);
            entityTypeOpt.ifPresent(whiteParticleListCache::add);
        }
    }

    public static Optional<ParticleType<?>> particleTypeGet(String id) {
        return BuiltInRegistries.PARTICLE_TYPE.getOptional(ResourceLocation.tryParse(id));
    }

    // 重新加载生物列表
    public static void reloadList() {
        // 清空当前的缓存
        blackParticleListCache.clear();
        whiteParticleListCache.clear();
        // 重新加载生物列表
        initConfigList();

    }
    public static boolean isParticleWhitelisted(@NotNull ParticleOptions particle) {
        return whiteParticleListCache.contains(particle.getType()); // 返回 true 表示在白名单中 - 添加渲染
    }
    public static boolean isParticleBlacklisted(@NotNull ParticleOptions particle) {
        return blackParticleListCache.contains(particle.getType()); // 返回 true 表示在白名单中 - 添加渲染
    }
    public static boolean isParticleBlacklisted(@NotNull ParticleType<?> particle) {
        return blackParticleListCache.contains(particle); // 返回 true 表示在白名单中 - 添加渲染
    }
    public static void addBlockBreakParticles(BlockPos pos, @NotNull BlockState state) {
        // 提前检查配置，避免重复计算
        BlockBreakParticleType currentQuality = RuOK.get().BlockBreakParticleMode;
        if (state.isAir() || !state.shouldSpawnTerrainParticles() || !RuOK.get().Particle || isParticleBlacklisted(ParticleTypes.BLOCK)) {
            return;  // 提前返回，避免不必要的计算
        }

        // 获取玩家位置和最大粒子生成距离
        Vec3 playerPos = Objects.requireNonNull(mc.player).position();
        double maxDistance = RuOK.get().MaxParticleDistance;
        double distance = playerPos.distanceToSqr(new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));

        // 如果玩家距离方块太远，不生成粒子
        if (distance > maxDistance * maxDistance) {
            return;
        }

        // 计算方块的体积和粒子密度
        VoxelShape voxelShape = state.getShape(mc.level, pos);

        voxelShape.forAllEdges((minX, minY, minZ, maxX, maxY, maxZ) -> {
            // 计算粒子数量
            double d = Math.min(1.0, maxX - minX);
            double e = Math.min(1.0, maxY - minY);
            double f = Math.min(1.0, maxZ - minZ);

            int i = Math.max(2, Mth.ceil(d / 0.25));
            int j = Math.max(2, Mth.ceil(e / 0.25));
            int k = Math.max(2, Mth.ceil(f / 0.25));

            // 根据画质等级调整粒子数量
            i = Math.min(i, currentQuality.getX());
            j = Math.min(j, currentQuality.getY());
            k = Math.min(k, currentQuality.getZ());

            // 生成粒子
            double baseX = pos.getX() + minX;
            double baseY = pos.getY() + minY;
            double baseZ = pos.getZ() + minZ;

            for (int l = 0; l < i; ++l) {
                for (int m = 0; m < j; ++m) {
                    for (int n = 0; n < k; ++n) {
                        // 计算粒子相对位置
                        double g = ((double) l + 0.5) / i;
                        double h = ((double) m + 0.5) / j;
                        double o = ((double) n + 0.5) / k;

                        // 计算粒子世界坐标
                        double p = g * d + baseX;
                        double q = h * e + baseY;
                        double r = o * f + baseZ;
                        TerrainParticle particle = new TerrainParticle(
                                mc.level,
                                p,
                                q,
                                r,
                                g - 0.5,
                                h - 0.5,
                                o - 0.5,
                                state,
                                pos
                        );
                        mc.particleEngine.add(particle);
                    }
                }
            }
        });
    }
    public static <T extends ParticleOptions> void onParticleCull(ParticleEngine instance, Particle particle, T parameters, double x, double y, double z) {
        if(!RuOK.get().onCull) {//关闭剔除功能被关闭,直接执行原逻辑
            instance.add(particle);
            return;
        }
        // 调用 ParticleRender 进行剔除检查
        Boolean par = ParticleRender.isParticleCull(parameters, x, y, z);
        if (par) {
            instance.add(particle);
        } else {
            particle.remove();
        }
        if(RuOK.get().SetParticleMaxAge) {
            int max = particle.getLifetime();
            particle.setLifetime(max / RuOK.get().ParticleMaxAge);
        }


    }

    private static <T extends ParticleOptions> Boolean isParticleCull(T parameters, double x, double y, double z) {
        Minecraft mc = Minecraft.getInstance();
        Camera camera = mc.gameRenderer.getMainCamera();

        // 白名单粒子不剔除
        if (ParticleRender.isParticleWhitelisted(parameters)) {
            return true;
        }

        // 黑名单粒子直接剔除
        if (ParticleRender.isParticleBlacklisted(parameters)) {
            return false;
        }

        // 是否开启粒子剔除
        if (!RuOK.get().Particle) {
            return false;
        }

        // 计算粒子与相机的距离
        double distanceSquared = camera.getPosition().distanceToSqr(x, y, z);
        double maxDistanceSquared = RuOK.get().MaxParticleDistance * RuOK.get().MaxParticleDistance;

        // 超过最大距离剔除
        if (distanceSquared > maxDistanceSquared) {
            return false;
        }

        // **如果通过所有剔除检查，则保留**
        return true;
    }


}
