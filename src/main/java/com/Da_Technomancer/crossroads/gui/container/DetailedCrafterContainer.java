package com.Da_Technomancer.crossroads.gui.container;

import com.Da_Technomancer.crossroads.Crossroads;
import com.Da_Technomancer.crossroads.api.EnumPath;
import com.Da_Technomancer.crossroads.api.MiscUtil;
import com.Da_Technomancer.crossroads.api.crafting.CraftingUtil;
import com.Da_Technomancer.crossroads.blocks.CRBlocks;
import com.Da_Technomancer.crossroads.crafting.CRRecipes;
import com.Da_Technomancer.crossroads.crafting.DetailedCrafterRec;
import com.Da_Technomancer.essentials.api.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static net.minecraftforge.common.ForgeHooks.setCraftingPlayer;

public class DetailedCrafterContainer extends RecipeBookMenu<CraftingContainer>{

	private static final Supplier<MenuType<?>> TYPE_SPL = MiscUtil.getCRRegistryObject("detailed_crafter", ForgeRegistries.Keys.MENU_TYPES);

	@SuppressWarnings("unchecked")
	private static final TagKey<Item>[] unlockKeys = new TagKey[3];
	private static final TagKey<Item> fillerMats = CraftingUtil.getTagKey(ForgeRegistries.Keys.ITEMS, new ResourceLocation(Crossroads.MODID, "path_unlock_filler"));

	static{
		unlockKeys[0] = CraftingUtil.getTagKey(ForgeRegistries.Keys.ITEMS, new ResourceLocation(Crossroads.MODID, "technomancy_unlock_key"));
		unlockKeys[1] = CraftingUtil.getTagKey(ForgeRegistries.Keys.ITEMS, new ResourceLocation(Crossroads.MODID, "alchemy_unlock_key"));
		unlockKeys[2] = CraftingUtil.getTagKey(ForgeRegistries.Keys.ITEMS, new ResourceLocation(Crossroads.MODID, "witchcraft_unlock_key"));
	}

	private final CraftingContainer inInv = new CraftingContainer(this, 3, 3);
	private final ResultContainer outInv = new ResultContainer();
	private final Level world;
	private final Player player;

	private final boolean fake;//True for goggles- used to shortcut canInteractWith checks
	@Nullable
	private final BlockPos pos;//Null if fake, nonnull otherwise- used for canInteractWith

	public DetailedCrafterContainer(int id, Inventory playerInv, FriendlyByteBuf buf){
		super(TYPE_SPL.get(), id);
		player = playerInv.player;
		world = player.level;

		fake = buf.readBoolean();
		if(fake){
			pos = null;
		}else{
			pos = buf.readBlockPos();
		}

		//Output 0
		addSlot(new SlotCraftingFlexible(playerInv.player, inInv, outInv, 0, 124, 35));

		//Input 0-8
		for(int i = 0; i < 3; ++i){
			for(int j = 0; j < 3; ++j){
				addSlot(new Slot(inInv, j + i * 3, 30 + j * 18, 17 + i * 18));
			}
		}

		//Player inventory
		for(int k = 0; k < 3; ++k){
			for(int i1 = 0; i1 < 9; ++i1){
				addSlot(new Slot(playerInv, i1 + k * 9 + 9, 8 + i1 * 18, 84 + k * 18));
			}
		}

		//Player hotbar
		for(int l = 0; l < 9; ++l){
			addSlot(new Slot(playerInv, l, 8 + l * 18, 142));
		}

	}

	@Override
	public void fillCraftSlotsStackedContents(StackedContents helper){
		inInv.fillStackedContents(helper);
	}

	@Override
	public void clearCraftingContent(){
		inInv.clearContent();
		outInv.clearContent();
	}

	@Override
	public boolean recipeMatches(Recipe<? super CraftingContainer> recipeIn){
		if(recipeIn instanceof DetailedCrafterRec){
			return ((DetailedCrafterRec) recipeIn).getPath().isUnlocked(player) && recipeIn.matches(inInv, player.level);
		}else{
			return recipeIn.matches(inInv, player.level);
		}
	}

	/**
	 * Called when the container is closed.
	 */
	@Override
	public void removed(Player playerIn){
		super.removed(playerIn);
		clearContainer(playerIn, inInv);
	}

	@Override
	public boolean stillValid(Player playerIn){
		return fake || pos == null || playerIn.level.getBlockState(pos).getBlock() == CRBlocks.detailedCrafter && playerIn.distanceToSqr((pos.getX()) + .5D, (pos.getY()) + .5D, (pos.getZ()) + .5D) <= 64;
	}

	/**
	 * Handle when the stack in slot {@code index} is shift-clicked. Normally this moves the stack between the player
	 * inventory and the other inventory(s).
	 */
	@Override
	public ItemStack quickMoveStack(Player playerIn, int index){
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = slots.get(index);
		if(slot != null && slot.hasItem()){
			ItemStack itemstack1 = slot.getItem();
			itemstack = itemstack1.copy();
			if(index == 0){
				itemstack1.getItem().onCraftedBy(itemstack1, world, playerIn);
				if(!moveItemStackTo(itemstack1, 10, 46, true)){
					return ItemStack.EMPTY;
				}

				slot.onQuickCraft(itemstack1, itemstack);
			}else if(index >= 10 && index < 37){
				if(!moveItemStackTo(itemstack1, 37, 46, false)){
					return ItemStack.EMPTY;
				}
			}else if(index >= 37 && index < 46){
				if(!moveItemStackTo(itemstack1, 10, 37, false)){
					return ItemStack.EMPTY;
				}
			}else if(!moveItemStackTo(itemstack1, 10, 46, false)){
				return ItemStack.EMPTY;
			}

			if(itemstack1.isEmpty()){
				slot.set(ItemStack.EMPTY);
			}else{
				slot.setChanged();
			}

			if(itemstack1.getCount() == itemstack.getCount()){
				return ItemStack.EMPTY;
			}

			slot.onTake(playerIn, itemstack1);
			if(index == 0){
				playerIn.drop(itemstack1, false);
			}
		}

		return itemstack;
	}

	/**
	 * Called to determine if the current slot is valid for the stack merging (double-click) code. The stack passed in is
	 * null for the initial slot that was double-clicked.
	 */
	@Override
	public boolean canTakeItemForPickAll(ItemStack stack, Slot slotIn){
		return slotIn.container != outInv && super.canTakeItemForPickAll(stack, slotIn);
	}

	@Override
	public int getResultSlotIndex(){
		return 0;
	}

	@Override
	public int getGridWidth(){
		return inInv.getWidth();
	}

	@Override
	public int getGridHeight(){
		return inInv.getHeight();
	}

	@Override
	public int getSize(){
		return 10;
	}

	@Override
	public RecipeBookType getRecipeBookType(){
		return RecipeBookType.CRAFTING;
	}

	@Override
	public boolean shouldMoveToInventory(int slotIndex){
		return slotIndex != getResultSlotIndex();
	}

	/**
	 * On the client side, there can be a delay before the client is informed when unlocking a path.
	 * This represents the last path unlocked in this UI by this player on the client, and is wiped every time the ui is re-opened
	 * It exists to prevent unlocking the same path several times on the client side during this delay- a minor visual inventory-desync glitch when unlocking while holding shift
	 */
	private byte lastUnlock = -1;

	@Override
	public void slotsChanged(Container inventoryIn){
		for(EnumPath path : EnumPath.values()){
			//Check for path unlocking
			if(!path.isUnlocked(player) && path.pathGatePassed(player) && unlockRecipe(path) && (!world.isClientSide || lastUnlock != path.getIndex())){
				if(world.isClientSide){
					lastUnlock = path.getIndex();
					playUnlockSound();
				}else{
					path.setUnlocked(player, true);
				}
				for(int i = 0; i < 9; i++){
					inInv.removeItem(i, 1);
				}
				return;
			}
		}

		if(!world.isClientSide){
			ServerPlayer serverplayerentity = (ServerPlayer) player;
			ItemStack itemstack = ItemStack.EMPTY;
			List<DetailedCrafterRec> recipes = world.getRecipeManager().getRecipesFor(CRRecipes.DETAILED_TYPE, inInv, world);
			//Find a detailed crafter specific recipe first
			Optional<? extends CraftingRecipe> recipeOpt = recipes.stream().filter(rec -> rec.getPath().isUnlocked(player)).findFirst();
			//If there is no valid detailed crafter recipe, try vanilla crafting
			if(!recipeOpt.isPresent()){
				recipeOpt = world.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, inInv, world);
			}

			if(recipeOpt.isPresent()){
				CraftingRecipe recipe = recipeOpt.get();
				if(outInv.setRecipeUsed(world, serverplayerentity, recipe)){
					itemstack = recipe.assemble(inInv);
				}
			}
			outInv.setItem(0, itemstack);
			serverplayerentity.connection.send(new ClientboundContainerSetSlotPacket(containerId, incrementStateId(), 0, itemstack));
		}
	}

	/**
	 * Checks if an "unlock recipe" is set
	 * @param path The path to check for
	 * @return Whether the current recipe is the correct one for unlocked the passed path
	 */
	private boolean unlockRecipe(EnumPath path){
		for(int i = 0; i < 9; i++){
			if(i != 4 && !CraftingUtil.tagContains(fillerMats, inInv.getItem(i).getItem())){
				return false;
			}
		}
		return CraftingUtil.tagContains(unlockKeys[path.getIndex()], inInv.getItem(4).getItem());
	}

	private void playUnlockSound(){
		world.playSound(player, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 2, 0);
	}

	private static class SlotCraftingFlexible extends ResultSlot{

		// The craft matrix inventory linked to this result slot.
		private final CraftingContainer craftMatrix;//We keep a copy because the superclass field is private

		public SlotCraftingFlexible(Player player, CraftingContainer craftingInventory, Container inventoryIn, int slotIndex, int xPosition, int yPosition){
			super(player, craftingInventory, inventoryIn, slotIndex, xPosition, yPosition);
			craftMatrix = craftingInventory;
		}

		@Override
		public void onTake(Player thePlayer, ItemStack stack){
			checkTakeAchievements(stack);
			setCraftingPlayer(thePlayer);
			List<DetailedCrafterRec> recipes = thePlayer.level.getRecipeManager().getRecipesFor(CRRecipes.DETAILED_TYPE, craftMatrix, thePlayer.level);
			Optional<? extends CraftingRecipe> recipeOpt = recipes.stream().filter(rec -> rec.getPath().isUnlocked(thePlayer)).findFirst();
			if(!recipeOpt.isPresent()){
				recipeOpt = thePlayer.level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, craftMatrix, thePlayer.level);
			}
			if(recipeOpt.isPresent()){
				//Remove items if there is a matching recipe
				NonNullList<ItemStack> remaining = recipeOpt.get().getRemainingItems(craftMatrix);
				for(int i = 0; i < remaining.size(); i++){
					craftMatrix.removeItem(i, 1);//Consume crafting ingredients

					ItemStack remainStack = remaining.get(i);
					ItemStack invStack = craftMatrix.getItem(i);
					//Return any remaining items (ex. empty buckets)
					if(!remainStack.isEmpty()){
						if(invStack.isEmpty()){
							craftMatrix.setItem(i, remaining.get(i));//Put it back into the crafting slot if it's empty
						}else if(BlockUtil.sameItem(invStack, remainStack)){
							invStack.grow(remainStack.getCount());//Try stacking it into the crafting slot
							craftMatrix.setItem(i, invStack);
						}else if(!thePlayer.getInventory().add(remainStack)){//Try returning it to the player inventory
							thePlayer.drop(remainStack, false);//Drop it as an item into the world
						}
					}
				}
			}

//			return stack;
		}
	}
}
