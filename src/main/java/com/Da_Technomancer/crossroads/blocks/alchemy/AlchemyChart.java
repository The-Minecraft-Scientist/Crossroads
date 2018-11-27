package com.Da_Technomancer.crossroads.blocks.alchemy;

import com.Da_Technomancer.crossroads.API.Properties;
import com.Da_Technomancer.crossroads.Main;
import com.Da_Technomancer.crossroads.blocks.ModBlocks;
import com.Da_Technomancer.crossroads.gui.GuiHandler;
import com.Da_Technomancer.crossroads.items.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;

public class AlchemyChart extends Block{
	
	private static final AxisAlignedBB BBNORTH = new AxisAlignedBB(0, 0, 0.9375D, 1, 1, 1);
	private static final AxisAlignedBB BBSOUTH = new AxisAlignedBB(0, 0, 0, 1, 1, 0.0625D);
	private static final AxisAlignedBB BBWEST = new AxisAlignedBB(0.9375D, 0, 0, 1, 1, 1);
	private static final AxisAlignedBB BBEAST = new AxisAlignedBB(0, 0, 0, 0.0625D, 1, 1);
	
	public AlchemyChart(){
		super(Material.WOOD);
		String name = "alchemy_chart";
		setTranslationKey(name);
		setRegistryName(name);
		setCreativeTab(ModItems.TAB_CROSSROADS);
		setHardness(3);
		ModBlocks.toRegister.add(this);
		ModBlocks.blockAddQue(this);
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ){
		if(!worldIn.isRemote){
			playerIn.openGui(Main.instance, GuiHandler.ALCHEMY_CHART_GUI, worldIn, pos.getX(), pos.getY(), pos.getZ());
		}
		return true;
	}
	
	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing blockFaceClickedOn, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer){
		return getDefaultState().withProperty(Properties.HORIZ_FACING, (placer == null) ? EnumFacing.NORTH : placer.getHorizontalFacing().getOpposite());
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos){
		switch(state.getValue(Properties.HORIZ_FACING)){
			case EAST:
				return BBEAST;
			case SOUTH:
				return BBSOUTH;
			case WEST:
				return BBWEST;
			default:
				return BBNORTH;
		}
	}
	
	@Override
	public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity, boolean thingamijiger){
		addCollisionBoxToList(pos, mask, list, getBoundingBox(state, worldIn, pos));
	}

	@Override
	protected BlockStateContainer createBlockState(){
		return new BlockStateContainer(this, Properties.HORIZ_FACING);
	}

	@Override
	public IBlockState getStateFromMeta(int meta){
		return getDefaultState().withProperty(Properties.HORIZ_FACING, EnumFacing.byIndex(meta == 0 || meta == 1 ? 2 : meta));
	}

	@Override
	public int getMetaFromState(IBlockState state){
		return state.getValue(Properties.HORIZ_FACING).getIndex();
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state){
		return false;
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face){
		return face == state.getValue(Properties.HORIZ_FACING).getOpposite() ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
	}

	@Override
	public boolean isFullCube(IBlockState state){
		return false;
	}
}
