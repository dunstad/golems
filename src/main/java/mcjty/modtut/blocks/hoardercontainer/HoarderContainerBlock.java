package mcjty.modtut.blocks.hoardercontainer;

import mcjty.modtut.ModTut;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class HoarderContainerBlock extends Block implements ITileEntityProvider {

    public HoarderContainerBlock() {
        super(Material.ROCK);
        setUnlocalizedName(ModTut.MODID + ".hoardercontainerblock");
        setRegistryName("hoardercontainerblock");
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new HoarderContainerTileEntity();
    }
    
    /**
     * Called serverside after this block is replaced with another in Chunk, but before the Tile Entity is updated
     */
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
    {
        TileEntity tileentity = worldIn.getTileEntity(pos);

        if (tileentity.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null))
        {
        	ItemStackHandler itemstackhandler = (ItemStackHandler) tileentity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        	for (int slot = 0; slot < itemstackhandler.getSlots(); slot++)
        	{
        		ItemStack itemstack = itemstackhandler.extractItem(slot, itemstackhandler.getSlotLimit(slot), false);
        		InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), itemstack);
        	}
        	
            worldIn.updateComparatorOutputLevel(pos, this);
        }

        super.breakBlock(worldIn, pos, state);
    }
}
