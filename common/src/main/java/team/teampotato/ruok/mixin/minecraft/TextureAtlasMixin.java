package team.teampotato.ruok.mixin.minecraft;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import team.teampotato.ruok.config.RuOK;

import java.util.List;

@Mixin(value = TextureAtlas.class,priority = 1200)
public class TextureAtlasMixin {
    @WrapOperation(
            method = "cycleAnimationFrames",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/texture/TextureAtlas;animatedTextures:Ljava/util/List;")
    )
    public List<TextureAtlasSprite.Ticker> animations(TextureAtlas instance, Operation<List<TextureAtlasSprite.Ticker>> original) {
        if (!RuOK.get().TextureAnimatedSprites) {
            return List.of();
        }
        return original.call(instance);
    }
}
