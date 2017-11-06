package mcjty.modtut.entity;

import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;

public class InventoryVariableSlotSize extends InventoryBasic
{
	private final int slotSize;
	private final int slotsCount;
	
	public InventoryVariableSlotSize(String title, boolean customName, int slotCount, int slotSize)
    {
		super(title, customName, slotCount);
        this.slotSize = slotSize;
        this.slotsCount = slotCount;
    }
	
	@Override
	public int getInventoryStackLimit()
    {
        return this.slotSize;
    }
	
	@Override
	public ItemStack addItem(ItemStack stack)
    {
        ItemStack itemstack = stack.copy();

        for (int i = 0; i < this.slotsCount; ++i)
        {
            ItemStack itemstack1 = this.getStackInSlot(i);

            if (itemstack1.isEmpty())
            {
            	this.setInventorySlotContents(i, itemstack.copy());
            	int count = Math.min(this.getInventoryStackLimit(), itemstack.getCount());
            	this.getStackInSlot(i).setCount(count);
            	itemstack.shrink(count);
            	if (itemstack.isEmpty())
                {
                    this.markDirty();
                    return ItemStack.EMPTY;
                }
            }

            if (ItemStack.areItemsEqual(itemstack1, itemstack))
            {
                int j = Math.min(this.getInventoryStackLimit(), itemstack1.getMaxStackSize());
                int k = Math.min(itemstack.getCount(), j - itemstack1.getCount());

                if (k > 0)
                {
                    itemstack1.grow(k);
                    itemstack.shrink(k);

                    if (itemstack.isEmpty())
                    {
                        this.markDirty();
                        return ItemStack.EMPTY;
                    }
                }
            }
        }

        if (itemstack.getCount() != stack.getCount())
        {
            this.markDirty();
        }

        return itemstack;
    }

}
