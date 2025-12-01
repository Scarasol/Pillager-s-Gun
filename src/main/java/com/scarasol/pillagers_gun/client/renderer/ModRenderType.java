package com.scarasol.pillagers_gun.client.renderer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

import java.util.OptionalDouble;

public class ModRenderType extends RenderType {

    public static net.minecraft.client.renderer.RenderType TRANSLUCENT_LINE = net.minecraft.client.renderer.RenderType.create(
            "translucent_lines",
            DefaultVertexFormat.POSITION_COLOR_NORMAL,
            VertexFormat.Mode.LINES,
            256,
            true,
            true,
            net.minecraft.client.renderer.RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getRendertypeLinesShader))
                    .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.of(1)))
                    .setTransparencyState(RenderStateShard.LIGHTNING_TRANSPARENCY)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .createCompositeState(false)
    );

    public static net.minecraft.client.renderer.RenderType translucentLines() {
        return TRANSLUCENT_LINE;
    }

    public static net.minecraft.client.renderer.RenderType translucentLines(double width) {
        return net.minecraft.client.renderer.RenderType.create(
                "translucent_lines",
                DefaultVertexFormat.POSITION_COLOR_NORMAL,
                VertexFormat.Mode.LINES,
                256,
                true,
                true,
                net.minecraft.client.renderer.RenderType.CompositeState.builder()
                        .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getRendertypeLinesShader))
                        .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.of(width)))
                        .setTransparencyState(RenderStateShard.LIGHTNING_TRANSPARENCY)
                        .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                        .setCullState(RenderStateShard.NO_CULL)
                        .setLightmapState(RenderStateShard.LIGHTMAP)
                        .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                        .createCompositeState(false)
        );
    }

    public static RenderType specularStar() {
        return RenderType.create(
                "specular_star",
                DefaultVertexFormat.POSITION_COLOR,
                VertexFormat.Mode.TRIANGLES,
                256,
                true,
                true,
                RenderType.CompositeState.builder()
                        .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getPositionColorShader))
                        .setTransparencyState(RenderStateShard.LIGHTNING_TRANSPARENCY)
                        .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                        .setCullState(RenderStateShard.NO_CULL)
                        .setLightmapState(RenderStateShard.NO_LIGHTMAP)
                        .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                        .createCompositeState(false)
        );
    }



    public ModRenderType(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, RenderStateShard setupState, RenderStateShard clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState::setupRenderState, clearState::clearRenderState);
    }
}
