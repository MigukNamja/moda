package miguknamja.pollution.block;

import java.util.List;
import java.util.Random;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import miguknamja.pollution.Pollution;
import miguknamja.pollution.compat.waila.WailaInfoProvider;
import miguknamja.pollution.data.PollutionWorldData;
import miguknamja.pollution.tileentity.PolluterTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PolluterAdminBlock extends Block implements ITileEntityProvider, WailaInfoProvider {

	public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

	public PolluterAdminBlock() {
		super(Material.ROCK);
		setUnlocalizedName(Pollution.MODID + ".polluteradminblock");
		setRegistryName("polluteradminblock");
		GameRegistry.register(this);
		GameRegistry.register(new ItemBlock(this), getRegistryName());
		GameRegistry.registerTileEntity(PolluterTileEntity.class, Pollution.MODID + "_polluteradminblock");
	}

	@SideOnly(Side.CLIENT)
	public void initModel() {
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0,
				new ModelResourceLocation(getRegistryName(), "inventory"));
	}

	@Override
	public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
			IWailaConfigHandler config) {
		TileEntity te = accessor.getTileEntity();
		if (te instanceof PolluterTileEntity) {
			//PolluterTileEntity polluterTileEntity = (PolluterTileEntity) te;
			//currenttip.add(TextFormatting.GRAY + PollutionWorldData.getPollutionString(accessor.getWorld(), accessor.getPosition()));
		}
		return currenttip;
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new PolluterTileEntity();
	}

	/*
	private PolluterTileEntity getTE(World world, BlockPos pos) {
		return (PolluterTileEntity) world.getTileEntity(pos);
	}
	*/

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
			ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (!world.isRemote) { // Don't run on the client side

			Chunk chunk = world.getChunkFromBlockCoords(pos);
			if (side == state.getValue(FACING)) {
				if (hitY <= .45f) {
					PollutionWorldData.decrement(world, chunk);
				} else if (hitY >= .55f) {
					PollutionWorldData.increment(world, chunk);
				} else {
					return true;
				}
				
				player.addChatComponentMessage(
						new TextComponentString(TextFormatting.GREEN + PollutionWorldData.getPollutionString(world, chunk)));
			}
		}
		//player.addChatComponentMessage(new TextComponentString(TextFormatting.GREEN + "test" + this.tickRate(world)));
		
		
		// Return true also on the client to make sure that MC knows we handled
		// this and will not try to place
		// a block on the client
		return true;
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer,
			ItemStack stack) {
		world.setBlockState(pos, state.withProperty(FACING, placer.getHorizontalFacing().getOpposite()), 2);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		// Since we only allow horizontal rotation we need only 2 bits for
		// facing. North, South, West, East start at index 2 so we have to add 2
		// here.
		return getDefaultState().withProperty(FACING, EnumFacing.getFront((meta & 3) + 2));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		// Since we only allow horizontal rotation we need only 2 bits for
		// facing. North, South, West, East start at index 2 so we have to
		// subtract 2 here.
		return state.getValue(FACING).getIndex() - 2;
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING);
	}

	@Override
	public int tickRate(World world) {
		return 20;
	}
	
	@Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state)
    {
		//Logging.log("BlockAdded being called");
    	worldIn.scheduleUpdate(pos, this, 0);
    }
    
	
	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		//Logging.log("UpdateTick being called");		
		world.scheduleUpdate(pos, this, this.tickRate(world));
	}
}