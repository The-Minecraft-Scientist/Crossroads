package com.Da_Technomancer.crossroads.blocks.beams;

import com.Da_Technomancer.crossroads.blocks.CRBlocks;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;

import javax.annotation.Nullable;
import java.util.List;

public class PermeableObsidian extends Block{

	public PermeableObsidian(){
		super(Properties.of(Material.STONE, MaterialColor.COLOR_BLACK).strength(50, 1200));
		String name = "permeable_obsidian";
		CRBlocks.toRegister.put(name, this);
		CRBlocks.blockAddQue(name, this);
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable BlockGetter player, List<Component> tooltip, TooltipFlag advanced){
		tooltip.add(Component.translatable("tt.crossroads.boilerplate.beam_permeable"));
		tooltip.add(Component.translatable("tt.crossroads.boilerplate.blast_resist"));
		tooltip.add(Component.translatable("tt.crossroads.boilerplate.decor"));
	}
}
