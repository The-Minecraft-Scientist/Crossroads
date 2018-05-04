package com.Da_Technomancer.crossroads.tileentities.alchemy;

import com.Da_Technomancer.crossroads.API.Capabilities;
import com.Da_Technomancer.crossroads.API.EnergyConverters;
import com.Da_Technomancer.crossroads.API.IInfoDevice;
import com.Da_Technomancer.crossroads.API.MiscOp;
import com.Da_Technomancer.crossroads.API.alchemy.*;
import com.Da_Technomancer.crossroads.API.heat.IHeatHandler;
import com.Da_Technomancer.crossroads.API.packets.ModPackets;
import com.Da_Technomancer.crossroads.API.packets.SendLooseArcToClient;
import com.Da_Technomancer.crossroads.API.technomancy.EnumGoggleLenses;
import com.Da_Technomancer.crossroads.Main;
import com.Da_Technomancer.crossroads.items.ModItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class ReactionChamberTileEntity extends AlchemyReactorTE{

	private double cableTemp = 0;
	private boolean init = false;
	private int energy = 0;
	private static final int ENERGY_CAPACITY = 1000;

	/**
	 * @param chat Add info to this list, 1 line per entry. 
	 * @param device The device type calling this method. 
	 * @param player The player using the info device.
	 * @param side The viewed EnumFacing (only used by goggles).
	 */
	@Override
	public void addInfo(ArrayList<String> chat, IInfoDevice device, EntityPlayer player, @Nullable EnumFacing side){
		if(device == ModItems.omnimeter || device == EnumGoggleLenses.RUBY){
			chat.add("Temp: " + MiscOp.betterRound(cableTemp, 3) + "°C");
		}
		if(device == ModItems.omnimeter || device == EnumGoggleLenses.DIAMOND){
			if(amount == 0){
				chat.add("No reagents");
			}
			for(ReagentStack reag : contents){
				if(reag != null){
					chat.add(reag.toString());
				}
			}
		}
	}

	public ReactionChamberTileEntity(){
		super();
	}

	public ReactionChamberTileEntity(boolean glass){
		super(glass);
	}

	public double getAmount(){
		return amount;
	}

	public NBTTagCompound getContentNBT(){
		NBTTagCompound nbt = new NBTTagCompound();
		for(int i = 0; i < AlchemyCore.REAGENT_COUNT; i++){
			if(contents[i] != null){
				nbt.setDouble(i + "_am", contents[i].getAmount());
			}
		}
		nbt.setDouble("heat", heat);
		return nbt;
	}

	public void writeContentNBT(NBTTagCompound nbt){
		heat = nbt.getDouble("heat");
		for(int i = 0; i < AlchemyCore.REAGENT_COUNT; i++){
			contents[i] = nbt.hasKey(i + "_am") ? new ReagentStack(AlchemyCore.REAGENTS[i], nbt.getDouble(i + "_am")) : null;
		}
		dirtyReag = true;
	}

	@Override
	public void update(){
		if(world.isRemote){
			return;
		}
		if(!init){
			cableTemp = EnergyConverters.convertBiomeTemp(world.getBiomeForCoordsBody(pos).getTemperature(pos));
			init = true;
		}

		if(energy > 0){
			energy = Math.max(0, energy - 10);
			if(world.getTotalWorldTime() % 10 == 0){
				NBTTagCompound nbt = new NBTTagCompound();
				new LooseArcRenderable(pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, pos.getX() + world.rand.nextFloat(), pos.getY() + world.rand.nextFloat(), pos.getZ() + world.rand.nextFloat(), 1, 0F, 0.18F, TeslaCoilTileEntity.COLOR_CODES[(int) (world.getTotalWorldTime() % 3)]).saveToNBT(nbt);
				ModPackets.network.sendToAllAround(new SendLooseArcToClient(nbt), new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 512));
				world.playSound(null, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT, SoundCategory.BLOCKS, 0.05F, 0F);
			}
		}

		super.update();
	}

	@Override
	public boolean isCharged(){
		return energy > 0;
	}

	@Override
	public double transferCapacity(){
		return 1000D;
	}

	@Override
	protected double correctTemp(){
		//Shares heat between internal cable & contents
		cableTemp = amount <= 0 ? cableTemp : (cableTemp + EnergyConverters.ALCHEMY_TEMP_CONVERSION * amount * ((heat / amount) - 273D)) / (EnergyConverters.ALCHEMY_TEMP_CONVERSION * amount + 1D);
		heat = (cableTemp + 273D) * amount;
		return cableTemp;
	}

	@Override
	protected EnumTransferMode[] getModes(){
		return new EnumTransferMode[] {EnumTransferMode.BOTH, EnumTransferMode.BOTH, EnumTransferMode.BOTH, EnumTransferMode.BOTH, EnumTransferMode.BOTH, EnumTransferMode.BOTH};
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt){
		super.readFromNBT(nbt);
		cableTemp = nbt.getDouble("temp");
		init = nbt.getBoolean("init");
		energy = nbt.getInteger("ener");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt){
		super.writeToNBT(nbt);
		nbt.setDouble("temp", cableTemp);
		nbt.setBoolean("init", init);
		nbt.setInteger("ener", energy);
		return nbt;
	}

	@Override
	public boolean hasCapability(Capability<?> cap, EnumFacing side){
		if(cap == Capabilities.CHEMICAL_HANDLER_CAPABILITY){
			return true;
		}
		if(cap == Capabilities.HEAT_HANDLER_CAPABILITY){
			return true;
		}
		if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY){
			return true;
		}
		if(cap == CapabilityEnergy.ENERGY && (side == null || side.getAxis() != Axis.Y)){
			return true;
		}
		return super.hasCapability(cap, side);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getCapability(Capability<T> cap, EnumFacing side){
		if(cap == Capabilities.CHEMICAL_HANDLER_CAPABILITY){
			return (T) handler;
		}
		if(cap == Capabilities.HEAT_HANDLER_CAPABILITY){
			return (T) heatHandler;
		}
		if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY){
			return (T) itemHandler;
		}
		if(cap == CapabilityEnergy.ENERGY && (side == null || side.getAxis() != Axis.Y)){
			return (T) energyHandler;
		}
		return super.getCapability(cap, side);
	}

	private final HeatHandler heatHandler = new HeatHandler();
	private final ItemHandler itemHandler = new ItemHandler();
	private final EnergyHandler energyHandler  = new EnergyHandler();

	private class EnergyHandler implements IEnergyStorage{

		@Override
		public int receiveEnergy(int maxReceive, boolean simulate){
			int toMove = Math.min(ENERGY_CAPACITY - energy, maxReceive);

			if(!simulate && toMove > 0){
				energy += toMove;
				markDirty();
			}

			return toMove;
		}

		@Override
		public int extractEnergy(int maxExtract, boolean simulate){
			return 0;
		}

		@Override
		public int getEnergyStored(){
			return energy;
		}

		@Override
		public int getMaxEnergyStored(){
			return ENERGY_CAPACITY;
		}

		@Override
		public boolean canExtract(){
			return false;
		}

		@Override
		public boolean canReceive(){
			return true;
		}
	}

	private class ItemHandler implements IItemHandler{

		private ItemStack[] fakeInventory = new ItemStack[AlchemyCore.ITEM_TO_REAGENT.size()];

		private void updateFakeInv(){
			fakeInventory = new ItemStack[AlchemyCore.ITEM_TO_REAGENT.size()];
			int index = 0;
			double endTemp = handler.getTemp();
			for(IReagent reag : AlchemyCore.ITEM_TO_REAGENT.values()){
				fakeInventory[index] = contents[reag.getIndex()] != null && contents[reag.getIndex()].getPhase(endTemp) == EnumMatterPhase.SOLID ? reag.getStackFromReagent(contents[reag.getIndex()]) : ItemStack.EMPTY;
				index++;
			}
		}

		@Override
		public int getSlots(){
			return AlchemyCore.ITEM_TO_REAGENT.size();
		}

		@Override
		public ItemStack getStackInSlot(int slot){
			updateFakeInv();
			return fakeInventory[slot];
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate){
			if(!stack.isEmpty()){
				IReagent reag = AlchemyCore.ITEM_TO_REAGENT.get(stack.getItem());
				if(reag != null){
					if(dirtyReag){
						correctReag();
					}
					ItemStack testStack = stack.copy();
					testStack.setCount(1);
					double perAmount = reag.getReagentFromStack(testStack).getAmount();
					int trans = Math.min(stack.getCount(), (int) ((transferCapacity() - amount) / perAmount));
					if(!simulate){
						if(contents[reag.getIndex()] == null){
							contents[reag.getIndex()] = new ReagentStack(reag, perAmount * (double) trans);
						}else{
							contents[reag.getIndex()].increaseAmount(perAmount * (double) trans);
						}
						heat += Math.min(reag.getMeltingPoint() + 263D, 290D) * perAmount * (double) trans;
						dirtyReag = true;
						markDirty();
					}
					testStack.setCount(stack.getCount() - trans);
					return testStack.isEmpty() ? ItemStack.EMPTY : testStack;
				}
			}
			return stack;
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate){
			updateFakeInv();
			int canExtract = Math.min(fakeInventory[slot].getCount(), amount);
			if(canExtract > 0){
				try{
					ItemStack outStack = fakeInventory[slot].copy();
					outStack.setCount(canExtract);
					if(!simulate){
						int reagIndex = AlchemyCore.ITEM_TO_REAGENT.get(fakeInventory[slot].getItem()).getIndex();
						double endTemp = handler.getTemp();
						double reagAmount = AlchemyCore.REAGENTS[reagIndex].getReagentFromStack(outStack).getAmount();
						if(contents[reagIndex].increaseAmount(-reagAmount) <= 0){
							contents[reagIndex] = null;
						}
						heat -= (endTemp + 273D) * reagAmount;
						dirtyReag = true;
						markDirty();
					}
					return outStack;
				}catch(NullPointerException e){
					Main.logger.log(Level.FATAL, "Alchemy Item/Reagent map error. Slot: " + slot + ", Stack: " + fakeInventory[slot], e);
				}
			}

			return ItemStack.EMPTY;
		}

		@Override
		public int getSlotLimit(int slot){
			return 10;
		}
	}

	private class HeatHandler implements IHeatHandler{

		private void init(){
			if(!init){
				init = true;
				cableTemp = EnergyConverters.convertBiomeTemp(world.getBiomeForCoordsBody(pos).getTemperature(pos));
			}
		}

		@Override
		public double getTemp(){
			init();
			return cableTemp;
		}

		@Override
		public void setTemp(double tempIn){
			init = true;
			cableTemp = tempIn;
			//Shares heat between internal cable & contents
			if(amount != 0){
				cableTemp = (cableTemp + EnergyConverters.ALCHEMY_TEMP_CONVERSION * amount * ((heat / amount) - 273D)) / (EnergyConverters.ALCHEMY_TEMP_CONVERSION * amount + 1D);
				heat = (cableTemp + 273D) * amount;
				dirtyReag = true;
			}
			markDirty();
		}

		@Override
		public void addHeat(double tempChange){
			init();
			cableTemp += tempChange;
			//Shares heat between internal cable & contents
			if(amount != 0){
				cableTemp = (cableTemp + EnergyConverters.ALCHEMY_TEMP_CONVERSION * amount * ((heat / amount) - 273D)) / (EnergyConverters.ALCHEMY_TEMP_CONVERSION * amount + 1D);
				heat = (cableTemp + 273D) * amount;
				dirtyReag = true;
			}
			markDirty();
		}
	}
}
