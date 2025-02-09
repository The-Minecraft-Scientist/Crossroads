package com.Da_Technomancer.crossroads.items.item_sets;

import com.Da_Technomancer.crossroads.api.MiscUtil;
import com.Da_Technomancer.crossroads.items.CRItems;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public abstract class GearMatItem extends OreProfileItem{

	protected static final Properties itemProp = new Properties().tab(CRItems.TAB_GEAR);

	protected GearMatItem(String name){
		super(itemProp);
		CRItems.toRegister.put(name, this);
	}

	/**
	 * @param stack A stack with a GearMatItem
	 * @return The configured GearMaterial. Null if it has a non-existent material, iron if not configured (default)
	 */
	public static GearFactory.GearMaterial getMaterial(ItemStack stack){
		String matKey;
		if(!stack.hasTag()){
			return GearFactory.getDefaultMaterial();
		}else{
			matKey = stack.getTag().getString(KEY);
		}
		return GearFactory.findMaterial(matKey);
	}

	@Override
	protected OreSetup.OreProfile getSelfProfile(ItemStack stack){
		//Use the gear factory registry instead of the ore processing registry
		return getMaterial(stack);
	}

	@Override
	public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items){
		if(allowedIn(group)){
			//Add every material variant of this item
			for(GearFactory.GearMaterial mat : GearFactory.getMaterials()){
				items.add(withMaterial(mat, 1));
			}
		}
	}

	protected abstract double shapeFactor();

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag advanced){
		GearFactory.GearMaterial mat = getMaterial(stack);
		if(mat != null){
			tooltip.add(Component.translatable("tt.crossroads.boilerplate.inertia", MiscUtil.preciseRound(mat.getDensity() * shapeFactor(), 3)));
		}
	}
}
