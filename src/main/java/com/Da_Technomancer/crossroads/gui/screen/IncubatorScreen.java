package com.Da_Technomancer.crossroads.gui.screen;

import com.Da_Technomancer.crossroads.API.MiscUtil;
import com.Da_Technomancer.crossroads.API.templates.MachineGUI;
import com.Da_Technomancer.crossroads.Crossroads;
import com.Da_Technomancer.crossroads.gui.container.IncubatorContainer;
import com.Da_Technomancer.crossroads.tileentities.witchcraft.IncubatorTileEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.awt.*;

public class IncubatorScreen extends MachineGUI<IncubatorContainer, IncubatorTileEntity>{

	private static final ResourceLocation TEXTURE = new ResourceLocation(Crossroads.MODID, "textures/gui/container/incubator_gui.png");

	public IncubatorScreen(IncubatorContainer container, PlayerInventory playerInv, ITextComponent name){
		super(container, playerInv, name);
	}

	@Override
	protected void renderBg(MatrixStack matrix, float partialTicks, int mouseX, int mouseY){
		Minecraft.getInstance().getTextureManager().bind(TEXTURE);

		blit(matrix, leftPos, topPos, 0, 0, imageWidth, imageHeight);

		blit(matrix, leftPos + 43, topPos + 35, 176, 0, menu.progressRef.get() * 54 / IncubatorTileEntity.REQUIRED, 10);

		super.renderBg(matrix, partialTicks, mouseX, mouseY);
	}

	@Override
	protected void renderLabels(MatrixStack matrix, int mouseX, int mouseY){
		super.renderLabels(matrix, mouseX, mouseY);

		//Changes color based on whether within target temperature
		int targetTemp = menu.targetRef.get();
		String s = MiscUtil.localize("container.crossroads.incubator.target", targetTemp);
		font.draw(matrix, s, imageWidth - 8 - font.width(s), 16, IncubatorTileEntity.withinTarget(menu.heatRef.get(), targetTemp) ? Color.GREEN.getRGB() : Color.RED.getRGB());
	}
}
