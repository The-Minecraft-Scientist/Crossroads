package com.Da_Technomancer.crossroads.api.technomancy;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;
import java.util.ArrayList;

public interface IGoggleEffect{

	public static final IGoggleEffect EMPTY = (world, player, chat, ray) -> {};

	/**
	 * Called every tick on the server side while goggles with the correct lens are worn.
	 * Instead of printing chat directly (except in special cases like element discovery),
	 * each line of chat should be added to the List separately. 
	 */
	public void armorTick(Level world, Player player, ArrayList<Component> chat, @Nullable BlockHitResult ray);
	
}
