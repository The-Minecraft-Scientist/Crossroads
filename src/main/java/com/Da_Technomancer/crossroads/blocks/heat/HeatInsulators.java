package com.Da_Technomancer.crossroads.blocks.heat;

import com.Da_Technomancer.crossroads.api.beams.IBeamEffect;
import com.Da_Technomancer.crossroads.effects.beam_effects.BlockEffect;
import com.Da_Technomancer.crossroads.effects.beam_effects.DirtEffect;
import com.Da_Technomancer.crossroads.effects.beam_effects.SlimeEffect;
import net.minecraft.world.level.block.Blocks;

public enum HeatInsulators{

	WOOL(.25D, 300D, new BlockEffect(Blocks.FIRE.defaultBlockState())),
	SLIME(.2D, 500D, new SlimeEffect()),
	DIRT(.5D, 42D, new DirtEffect()),
	ICE(.001D, 0D, new BlockEffect(Blocks.WATER.defaultBlockState())),
	OBSIDIAN(.015D, 2_000D, new BlockEffect(Blocks.LAVA.defaultBlockState())),
	CERAMIC(.05D, 3_000D, new BlockEffect(Blocks.LAVA.defaultBlockState())),
	DENSUS(0, 10_000D, new BlockEffect(Blocks.LAVA.defaultBlockState()));

	private final double rate;
	private final double limit;
	private final IBeamEffect effect;

	HeatInsulators(double rate, double limit, IBeamEffect effect){
		this.rate = rate;
		this.limit = limit;
		this.effect = effect;
	}

	public double getRate(){
		return rate;
	}

	public double getLimit(){
		return limit;
	}

	public IBeamEffect getEffect(){
		return effect;
	}

	/**This will return the name with all but the first char being lowercase,
	 * so COPPER becomes Copper, which is good for oreDict and registry
	 */
	@Override
	public String toString(){
		String name = name();
		char char1 = name.charAt(0);
		name = name.substring(1);
		name = name.toLowerCase();
		name = char1 + name;
		return name;
	}
}
