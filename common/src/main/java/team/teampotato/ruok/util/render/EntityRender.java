package team.teampotato.ruok.util.render;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.Ghast;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import team.teampotato.ruok.config.RuOK;
import team.teampotato.ruok.util.EntityUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

public class EntityRender {
    private static final HashSet<EntityType<?>> blackEntityListCache = new HashSet<>();
    private static final HashSet<EntityType<?>> whiteEntityListCache = new HashSet<>();
    private static final Minecraft mc = Minecraft.getInstance();


    static {
        initConfigList();
    }

    // 初始化生物列表
    private static void initConfigList() {
        List<String> blackConfig = RuOK.get().blackListedEntities;
        List<String> whiteConfig = RuOK.get().whiteListedEntities;
        for (String bc : blackConfig) {
            Optional<EntityType<?>> entityTypeOpt = entityTypeGet(bc);
            entityTypeOpt.ifPresent(blackEntityListCache::add);
        }
        for (String wc : whiteConfig) {
            Optional<EntityType<?>> entityTypeOpt = entityTypeGet(wc);
            entityTypeOpt.ifPresent(whiteEntityListCache::add);
        }
    }
    public static Optional<EntityType<?>> entityTypeGet(String id) {
        return BuiltInRegistries.ENTITY_TYPE.getOptional(ResourceLocation.tryParse(id));
    }

    // 重新加载生物列表
    public static void reloadList() {
        // 清空当前的缓存
        blackEntityListCache.clear();
        whiteEntityListCache.clear();
        // 重新加载生物列表
        initConfigList();

    }

    public static boolean isBlacklisted(@NotNull Entity entity) {
        return blackEntityListCache.contains(entity.getType()); // 返回 true 表示在黑名单中 - 删除渲染
    }
    public static boolean isWhitelisted(@NotNull Entity entity) {
        return whiteEntityListCache.contains(entity.getType()); // 返回 true 表示在白名单中 - 添加渲染
    }
    public static void entityCull(Entity entity, CallbackInfo ci) {
        if (!RuOK.get().onCull || !RuOK.get().EntityRender) {
            return;
        }
        if (entity.equals(mc.player) || mc.level == null || mc.player == null) {
            return;
        }

        double minDistance = RuOK.get().MinDistance; // 获取最小剔除距离 (5 格)
        double entityDistance = Math.sqrt(entity.distanceToSqr(mc.player)); // 计算实体到玩家的距离

        // **保护距离内的生物不会被剔除**
        if (entityDistance <= minDistance) {
            return; // 生物距离玩家过近，不剔除
        }

        // 获取视野内的所有生物，仅获取一次，避免重复计算
        List<Entity> visibleEntities = EntityUtils.getVisibleEntities();
        int maxEntities = RuOK.get().MaxEntityEntities;

        // 白名单 & Boss 优先保留
        if (isWhitelisted(entity) || isBossEntity(entity)) return;

        // 黑名单直接剔除
        if (isBlacklisted(entity)) {
            ci.cancel();
            return;
        }

        // 如果实体不在视野内，则剔除
        if (!visibleEntities.contains(entity)) {
            ci.cancel();
            return;
        }

        // **当生物超过 maxEntities 时，优先保留最近的 maxEntities 个**
        if (visibleEntities.size() > maxEntities) {
            // **寻找第 maxEntities 近的生物**
            double maxAllowedDistance = visibleEntities.stream()
                    .mapToDouble(e -> e.distanceToSqr(mc.player))
                    .sorted()
                    .limit(maxEntities)
                    .max()
                    .orElse(Double.MAX_VALUE); // 允许的最大距离

            // **如果当前实体比第 `maxEntities` 远的那个更远，则剔除**
            if (entity.distanceToSqr(mc.player) > maxAllowedDistance) {
                ci.cancel();
            }
        }
    }



    /**
     * 判断是否是 Boss 实体
     */
    private static boolean isBossEntity(@NotNull Entity entity) {
        return entity instanceof Ghast || entity instanceof EnderDragon;
    }

}
