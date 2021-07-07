package com.Da_Technomancer.crossroads.gui.container;

import com.Da_Technomancer.crossroads.API.templates.MachineContainer;
import com.Da_Technomancer.crossroads.Crossroads;
import com.Da_Technomancer.crossroads.tileentities.witchcraft.IncubatorTileEntity;
import com.Da_Technomancer.essentials.gui.container.IntDeferredRef;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(Crossroads.MODID)
public class IncubatorContainer extends MachineContainer<IncubatorTileEntity>{

	@ObjectHolder("incubator")
	private static ContainerType<IncubatorContainer> type = null;

	public final IntDeferredRef progressRef;
	public final IntDeferredRef targetRef;

	public IncubatorContainer(int id, PlayerInventory playerInv, PacketBuffer buf){
		super(type, id, playerInv, buf);
		progressRef = new IntDeferredRef(te::getProgress, te.getLevel().isClientSide);
		addDataSlot(progressRef);
		targetRef = new IntDeferredRef(te::getTargetTemp, te.getLevel().isClientSide);
		addDataSlot(targetRef);
	}

	@Override
	protected void addSlots(){
		addSlot(new StrictSlot(te, 0, 26, 23));//Input 1
		addSlot(new StrictSlot(te, 1, 26, 41));//Input 2
		addSlot(new OutputSlot(te, 2, 98, 32));//Output
	}
}
