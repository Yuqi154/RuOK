package team.teampotato.ruok.mixin.minecraft;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.world.entity.item.ItemEntity;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import team.teampotato.ruok.config.RuOK;
import team.teampotato.ruok.util.render.ItemRender;


@Mixin(ItemEntityRenderer.class)
public abstract class ItemEntityRendererMixin extends EntityRenderer<ItemEntity> {
    protected ItemEntityRendererMixin(EntityRendererProvider.Context ctx) {
        super(ctx);
    }
    @Inject(method = "getRenderedAmount", at = @At("HEAD"), cancellable = true)
    private static void onRenderItem(int i, CallbackInfoReturnable<Integer> cir){
        if(RuOK.get().FastItemRender) cir.setReturnValue(1);
    }

    @Redirect(method = "render*", at = @At(value = "INVOKE", target ="Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionf;)V"))
    private void onRender(PoseStack matrixStack, Quaternionf quaternion) {
        // 判断是否关闭物品旋转
        if (!RuOK.get().FastItemRender) matrixStack.mulPose(quaternion);
    }

    @Redirect(method = "render*", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V"))
    private void onRender(PoseStack matrixStack, float x, float y, float z) {
        // 判断是否关闭物品高低返回
        if (!RuOK.get().FastItemRender) matrixStack.translate(x, y, z);
    }

    @Inject(method = "render(Lnet/minecraft/world/entity/item/ItemEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"))
    private void renderItemCount(ItemEntity arg, float g, float h, PoseStack matrices, MultiBufferSource arg3, int l, CallbackInfo ci) {
        ItemRender.render(arg, matrices, arg3, l,this.entityRenderDispatcher.cameraOrientation());
    }



}
