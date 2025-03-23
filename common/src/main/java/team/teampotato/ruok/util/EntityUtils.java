package team.teampotato.ruok.util;

import com.google.common.collect.Lists;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import team.teampotato.ruok.config.RuOK;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class EntityUtils {
    private static List<Entity> entities = Lists.newArrayList();
    public static void refViewEntity() {
        entities = EntityUtils.getEntitiesInView(RuOK.get().EntityDistance,RuOK.get().EntityCullFov);
    }

    public static List<Entity> getVisibleEntities() {
        return entities;
    }
    public static List<Entity> getEntitiesInView(double maxDistance, double fov) {
        Minecraft client = Minecraft.getInstance();
        Camera camera = client.gameRenderer.getMainCamera(); // 获取当前摄像机
        if (camera == null || client.level == null) return List.of();

        Vec3 cameraPos = camera.getPosition(); // 获取摄像机的位置
        Vec3 cameraLook = getCameraLookVector(camera); // 计算摄像机的朝向

        List<Entity> entities = StreamSupport.stream(client.level.entitiesForRendering().spliterator(), false).toList();

        return entities.stream()
                .filter(entity -> cameraPos.distanceTo(entity.position()) <= maxDistance) // 限制距离
                .filter(entity -> isEntityInView(cameraPos, cameraLook, entity, fov)) // 视野判断
                .collect(Collectors.toList());
    }

    /**
     * 计算摄像机的朝向矢量
     */
    public static Vec3 getCameraLookVector(Camera camera) {
        float pitch = camera.getXRot() * ((float) Math.PI / 180F); // 俯仰角
        float yaw = camera.getYRot() * ((float) Math.PI / 180F); // 偏航角

        double x = -Math.sin(yaw) * Math.cos(pitch);
        double y = -Math.sin(pitch);
        double z = Math.cos(yaw) * Math.cos(pitch);

        return new Vec3(x, y, z).normalize();
    }

    /**
     * 判断实体是否在视野范围内
     */
    public static boolean isEntityInView(Vec3 cameraPos, Vec3 cameraLook, Entity entity, double fov) {
        Vec3 entityVec = entity.position().subtract(cameraPos).normalize(); // 目标方向
        double dot = cameraLook.dot(entityVec); // 计算夹角的余弦值
        double cosFov = Math.cos(Math.toRadians(fov / 2)); // 计算视野范围的 cos 值
        return dot > cosFov; // 如果 dot 大于 cosFov，说明实体在视野范围内
    }


//    public static List<Entity> getEntitiesInView(double maxDistance, double fov) {
//        MinecraftClient client = MinecraftClient.getInstance();
//        ClientPlayerEntity player = client.player;
//        if (player == null || client.world == null) return List.of();
//
//        Vec3d playerPos = player.getPos();
//        Vec3d playerLook = player.getRotationVec(1.0F); // 玩家朝向
//        List<Entity> entities = StreamSupport.stream(client.world.getEntities().spliterator(), false).toList();
//
//        // 仅获取生物
//        return entities.stream()
//                .filter(entity -> player.distanceTo(entity) <= maxDistance) // 限制距离
//                .filter(entity -> isEntityInView(playerPos, playerLook, entity, fov)) // 视野判断
//                .collect(Collectors.toList());
//    }
//    private static boolean isEntityInView(Vec3d playerPos, Vec3d playerLook, Entity entity, double fov) {
//        Vec3d entityVec = entity.getPos().subtract(playerPos).normalize(); // 目标方向
//        double dot = playerLook.dotProduct(entityVec); // 计算夹角的余弦值
//        double cosFov = Math.cos(Math.toRadians(fov / 2)); // 计算视野范围的 cos 值
//        return dot > cosFov; // 如果 dot 大于 cosFov，说明实体在视野范围内
//    }
}
