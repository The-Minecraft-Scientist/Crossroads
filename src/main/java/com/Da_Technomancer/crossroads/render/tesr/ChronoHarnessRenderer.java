package com.Da_Technomancer.crossroads.render.tesr;

import com.Da_Technomancer.crossroads.api.render.CRRenderUtil;
import com.Da_Technomancer.crossroads.blocks.technomancy.ChronoHarnessTileEntity;
import com.Da_Technomancer.crossroads.render.CRRenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class ChronoHarnessRenderer extends EntropyRenderer<ChronoHarnessTileEntity>{

	protected ChronoHarnessRenderer(BlockEntityRendererProvider.Context dispatcher){
		super(dispatcher);
	}

	@Override
	public void render(ChronoHarnessTileEntity te, float partialTicks, PoseStack matrix, MultiBufferSource buffer, int combinedLight, int combinedOverlay){
		super.render(te, partialTicks, matrix, buffer, combinedLight, combinedOverlay);

		float angle = te.getRenderAngle(partialTicks);
		int medLight = CRRenderUtil.calcMediumLighting(combinedLight);
		VertexConsumer builder = buffer.getBuffer(RenderType.solid());

		//Revolving rods
		matrix.translate(0.5D, 0, 0.5D);

		float smallOffset = 0.0928F;
		float largeOffset = 5F / 16F;

		matrix.mulPose(Vector3f.YP.rotationDegrees(angle));

		TextureAtlasSprite innerSprite = CRRenderUtil.getTextureSprite(CRRenderTypes.COPSHOWIUM_TEXTURE);
		addRod(builder, matrix, smallOffset, smallOffset, innerSprite, medLight);
		addRod(builder, matrix, smallOffset, -smallOffset, innerSprite, medLight);
		addRod(builder, matrix, -smallOffset, -smallOffset, innerSprite, medLight);
		addRod(builder, matrix, -smallOffset, smallOffset, innerSprite, medLight);

		matrix.mulPose(Vector3f.YP.rotationDegrees(-2F * angle));

		TextureAtlasSprite outerSprite = CRRenderUtil.getTextureSprite(CRRenderTypes.CAST_IRON_TEXTURE);
		addRod(builder, matrix, smallOffset, largeOffset, outerSprite, medLight);
		addRod(builder, matrix, smallOffset, -largeOffset, outerSprite, medLight);
		addRod(builder, matrix, -smallOffset, largeOffset, outerSprite, medLight);
		addRod(builder, matrix, -smallOffset, -largeOffset, outerSprite, medLight);
		addRod(builder, matrix, largeOffset, smallOffset, outerSprite, medLight);
		addRod(builder, matrix, largeOffset, -smallOffset, outerSprite, medLight);
		addRod(builder, matrix, -largeOffset, smallOffset, outerSprite, medLight);
		addRod(builder, matrix, -largeOffset, -smallOffset, outerSprite, medLight);
	}

	private void addRod(VertexConsumer builder, PoseStack matrix, float x, float z, TextureAtlasSprite sprite, int light){
		float rad = 1F / 16F;
		float minY = 2F / 16F;
		float maxY = 14F / 16F;

		float uEn = sprite.getU(2 * rad * 16);

		CRRenderUtil.addVertexBlock(builder, matrix, x - rad, minY, z - rad, sprite.getU0(), sprite.getV0(), 0, 0, -1, light);
		CRRenderUtil.addVertexBlock(builder, matrix, x - rad, maxY, z - rad, sprite.getU0(), sprite.getV1(), 0, 0, -1, light);
		CRRenderUtil.addVertexBlock(builder, matrix, x + rad, maxY, z - rad, uEn, sprite.getV1(), 0, 0, -1, light);
		CRRenderUtil.addVertexBlock(builder, matrix, x + rad, minY, z - rad, uEn, sprite.getV0(), 0, 0, -1, light);

		CRRenderUtil.addVertexBlock(builder, matrix, x - rad, minY, z + rad, uEn, sprite.getV0(), 0, 0, 1, light);
		CRRenderUtil.addVertexBlock(builder, matrix, x + rad, minY, z + rad, sprite.getU0(), sprite.getV0(), 0, 0, 1, light);
		CRRenderUtil.addVertexBlock(builder, matrix, x + rad, maxY, z + rad, sprite.getU0(), sprite.getV1(), 0, 0, 1, light);
		CRRenderUtil.addVertexBlock(builder, matrix, x - rad, maxY, z + rad, uEn, sprite.getV1(), 0, 0, 1, light);

		CRRenderUtil.addVertexBlock(builder, matrix, x - rad, minY, z - rad, uEn, sprite.getV0(), -1, 0, 0, light);
		CRRenderUtil.addVertexBlock(builder, matrix, x - rad, minY, z + rad, sprite.getU0(), sprite.getV0(), -1, 0, 0, light);
		CRRenderUtil.addVertexBlock(builder, matrix, x - rad, maxY, z + rad, sprite.getU0(), sprite.getV1(), -1, 0, 0, light);
		CRRenderUtil.addVertexBlock(builder, matrix, x - rad, maxY, z - rad, uEn, sprite.getV1(), -1, 0, 0, light);

		CRRenderUtil.addVertexBlock(builder, matrix, x + rad, minY, z - rad, sprite.getU0(), sprite.getV0(), 1, 0, 0, light);
		CRRenderUtil.addVertexBlock(builder, matrix, x + rad, maxY, z - rad, sprite.getU0(), sprite.getV1(), 1, 0, 0, light);
		CRRenderUtil.addVertexBlock(builder, matrix, x + rad, maxY, z + rad, uEn, sprite.getV1(), 1, 0, 0, light);
		CRRenderUtil.addVertexBlock(builder, matrix, x + rad, minY, z + rad, uEn, sprite.getV0(), 1, 0, 0, light);
	}
}
