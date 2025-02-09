package com.Da_Technomancer.crossroads.items.item_sets;

import com.Da_Technomancer.crossroads.CRConfig;
import com.Da_Technomancer.crossroads.Crossroads;
import com.Da_Technomancer.crossroads.api.MiscUtil;
import com.Da_Technomancer.crossroads.blocks.BasicBlock;
import com.Da_Technomancer.crossroads.blocks.CRBlocks;
import com.Da_Technomancer.crossroads.items.CRItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public final class OreSetup{

	public static Item ingotTin;
	public static Item nuggetTin;
	public static Item rawTin;
	public static BasicBlock blockTin;
	public static BasicBlock blockRawTin;
	public static BasicBlock oreTin;
	public static BasicBlock oreTinDeep;

	public static Item nuggetCopper;

	public static Item ingotBronze;
	public static Item nuggetBronze;
	public static BasicBlock blockBronze;

	public static Item gemRuby;
	public static BasicBlock blockRuby;
	public static BasicBlock oreRuby;

	public static Item ingotCopshowium;
	public static Item nuggetCopshowium;
	public static BasicBlock blockCopshowium;

	public static Item voidCrystal;
	public static BasicBlock oreVoid;

	private static final HashMap<String, OreProfile> metalTypes = new HashMap<>();
	private static OreProfile DEFAULT;
	
	public static OreProfile findMaterial(String id){
		return metalTypes.getOrDefault(id, getDefaultMaterial());
	}

	public static Collection<OreProfile> getMaterials(){
		return metalTypes.values();
	}

	public static void registerMaterial(OreProfile mat){
		metalTypes.put(mat.getId(), mat);
	}

	/**
	 * Fallback material if a material fails to load
	 * @return The Iron OreProfile
	 */
	public static OreProfile getDefaultMaterial(){
		return DEFAULT;
	}

	protected static void init(){
		//Register CR metal ores, blocks, ingots, nuggets manually
		ingotTin = new Item(new Item.Properties().tab(CRItems.TAB_CROSSROADS));
		CRItems.toRegister.put("ingot_tin", ingotTin);
		blockTin = new BasicBlock("block_tin", CRBlocks.getMetalProperty());
		blockRawTin = new BasicBlock("block_raw_tin", CRBlocks.getRockProperty());
		nuggetTin = new Item(new Item.Properties().tab(CRItems.TAB_CROSSROADS));
		CRItems.toRegister.put("nugget_tin", nuggetTin);
		rawTin = new Item(new Item.Properties().tab(CRItems.TAB_CROSSROADS));
		CRItems.toRegister.put("raw_tin", rawTin);
		oreTin = new BasicBlock("ore_tin", CRBlocks.getRockProperty().strength(3));
		oreTinDeep = new BasicBlock("ore_tin_deep", CRBlocks.getRockProperty().strength(4.5F, 3).sound(SoundType.DEEPSLATE));

		nuggetCopper = new Item(new Item.Properties().tab(CRItems.TAB_CROSSROADS));
		CRItems.toRegister.put("nugget_copper", nuggetCopper);

		ingotBronze = new Item(new Item.Properties().tab(CRItems.TAB_CROSSROADS));
		CRItems.toRegister.put("ingot_bronze", ingotBronze);
		blockBronze = new BasicBlock("block_bronze", CRBlocks.getMetalProperty());
		nuggetBronze = new Item(new Item.Properties().tab(CRItems.TAB_CROSSROADS));
		CRItems.toRegister.put("nugget_bronze", nuggetBronze);

		gemRuby = new Item(new Item.Properties().tab(CRItems.TAB_CROSSROADS));
		CRItems.toRegister.put("gem_ruby", gemRuby);
		blockRuby = new BasicBlock("block_ruby", CRBlocks.getRockProperty());
		oreRuby = new BasicBlock("ore_ruby", CRBlocks.getRockProperty().strength(3));

		ingotCopshowium = new Item(new Item.Properties().tab(CRItems.TAB_CROSSROADS)){
			@Override
			public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn){
				tooltip.add(Component.translatable("tt.crossroads.copshowium.quip").setStyle(MiscUtil.TT_QUIP));
			}
		};
		CRItems.toRegister.put("ingot_copshowium", ingotCopshowium);
		blockCopshowium = new BasicBlock("block_copshowium", CRBlocks.getMetalProperty());
		nuggetCopshowium = new Item(new Item.Properties().tab(CRItems.TAB_CROSSROADS));
		CRItems.toRegister.put("nugget_copshowium", nuggetCopshowium);

		voidCrystal = new Item(new Item.Properties().tab(CRItems.TAB_CROSSROADS));
		CRItems.toRegister.put("void_crystal", voidCrystal);
		oreVoid = new BasicBlock("ore_void", CRBlocks.getRockProperty().strength(3, 9));
		
		loadConfig();
	}
	
	public static void loadConfig(){
		//Clear any previously registered OreProfile if shouldReload is true. Otherwise, leave them registered
		//The old OreProfiles are wiped to allow them being defined by the server config, which could differ between the client and server
		ArrayList<String> toRemove = new ArrayList<>(metalTypes.size());
		for(OreProfile prof : metalTypes.values()){
			if(prof.wipeOnReload()){
				toRemove.add(prof.id);
			}
		}
		toRemove.forEach(metalTypes::remove);

		List<? extends String> rawInput = CRConfig.processableOres.get();

		for(String raw : rawInput){
			int spaceIndex = raw.length() - 7;
			String metal = raw.substring(0, spaceIndex);
			Color col;

			String colorString = '#' + raw.substring(spaceIndex + 1);
			try{
				col = Color.decode(colorString);
			}catch(NumberFormatException e){
				//Pick a random color because the user messed up, and if the user ends up with hot-pink lead that's their problem
				col = Color.getHSBColor((float) Math.random(), 1F, 1F);
				Crossroads.logger.error(String.format("Invalid color defined for ore profile: %s; Selecting random color", metal), e);
			}

			registerMaterial(new OreProfile(metal, col));
		}

		DEFAULT = metalTypes.get("iron");
		//If the config was modified to prevent iron being registered, crash
		if(DEFAULT == null){
			IllegalArgumentException e = new IllegalArgumentException("Default Ore Profile not registered!");
			Crossroads.logger.error("Config Modified to prevent registering default ore profile (iron)", e);
			throw e;
		}
	}

	public static class OreProfile{

		private final String id;
		private final Color color;
		private final boolean shouldReload;

		protected OreProfile(String nameIn, Color matColor){
			this(nameIn, matColor, true);
		}

		protected OreProfile(String nameIn, Color matColor, boolean shouldReload){
			id = nameIn;
			color = matColor;
			this.shouldReload = shouldReload;
		}

		public boolean wipeOnReload(){
			return shouldReload;
		}

		public String getId(){
			return id;
		}

		public Color getColor(){
			return color;
		}

		/**
		 * Gets the localized name of this material (as an adjective)- to be combined with a component
		 * Do not trust the result on the physical server side (due to missing localization maps)
		 * @return The localized name
		 */
		public String getName(){
			return MiscUtil.localize("material." + id);
		}
	}
}
