package mcjty.modtut.entity;

import java.util.function.Predicate;

import javax.annotation.Nullable;

import mcjty.modtut.blocks.hoardercontainer.HoarderContainerBlock;
import net.minecraft.block.Block;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityHoarderGolem extends EntityGolem
{
	
    public EntityHoarderGolem(World worldIn)
    {
        super(worldIn);
        this.setCanPickUpLoot(true);
    }

    @Override
    protected void initEntityAI()
    {
        super.initEntityAI();
        this.tasks.addTask(5, new EntityHoarderGolem.GolemAIGoToItem(this, new Predicate<ItemStack>() {
        	public boolean test(ItemStack itemstack) {
        		return true;
        	}
        }));
        this.tasks.addTask(6, new EntityHoarderGolem.GolemAIApproachBlock(this));
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn)
    {
        return SoundEvents.ENTITY_ENDERMEN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound()
    {
        return SoundEvents.ENTITY_ENDERMEN_DEATH;
    }
    
    @Nullable
    protected ResourceLocation getLootTable()
    {
        return null;
    }
    

    /**
     * Tests if this entity should pickup a weapon or an armor. Entity drops current weapon or armor if the new one is
     * better.
     */
    protected void updateEquipmentIfNeeded(EntityItem itemEntity)
    {
        ItemStack itemstack = itemEntity.getItem();
        Item item = itemstack.getItem();

        if (this.canHoarderPickupItem(item))
        {
            ItemStack itemstack1 = this.itemStackHandler.insertItem(0, itemstack, false);

            if (itemstack1.isEmpty())
            {
                itemEntity.setDead();
            }
            else
            {
                itemstack.setCount(itemstack1.getCount());
            }
        }
    }

    private boolean canHoarderPickupItem(Item itemIn)
    {
        return true;
    }


    /*===================================== Forge Start ==============================*/
    public static void setCarriable(Block block, boolean canCarry)
    {
        if (canCarry) CARRIABLE_BLOCKS.add(block);
        else          CARRIABLE_BLOCKS.remove(block);
    }
    public static boolean getCarriable(Block block)
    {
        return CARRIABLE_BLOCKS.contains(block);
    }
    /*===================================== Forge End ==============================*/

    static
    {
        CARRIABLE_BLOCKS.add(Blocks.GRASS);
        CARRIABLE_BLOCKS.add(Blocks.DIRT);
        CARRIABLE_BLOCKS.add(Blocks.SAND);
        CARRIABLE_BLOCKS.add(Blocks.GRAVEL);
        CARRIABLE_BLOCKS.add(Blocks.YELLOW_FLOWER);
        CARRIABLE_BLOCKS.add(Blocks.RED_FLOWER);
        CARRIABLE_BLOCKS.add(Blocks.BROWN_MUSHROOM);
        CARRIABLE_BLOCKS.add(Blocks.RED_MUSHROOM);
        CARRIABLE_BLOCKS.add(Blocks.TNT);
        CARRIABLE_BLOCKS.add(Blocks.CACTUS);
        CARRIABLE_BLOCKS.add(Blocks.CLAY);
        CARRIABLE_BLOCKS.add(Blocks.PUMPKIN);
        CARRIABLE_BLOCKS.add(Blocks.MELON_BLOCK);
        CARRIABLE_BLOCKS.add(Blocks.MYCELIUM);
        CARRIABLE_BLOCKS.add(Blocks.NETHERRACK);
    }
    
    /**
     * Returns a BlockPos near the golem, or null if none are found
     */
    private BlockPos getClosestBlockPosInRadius(int radius) {
        Vec3d center = new Vec3d((double)((float)MathHelper.floor(this.posX) + 0.5F), (double)((float)this.posY + 0.5F), (double)((float)MathHelper.floor(this.posZ) + 0.5F));
        BlockPos closestBlockPos = null;
        double closestBlockPosDistanceSq = 999.0D;
        for (int oY = (int)center.y; oY <= (int)center.y + 2; oY++) {
            for (int oX = (int)center.x - radius - 1; oX <= (int)center.x + radius - 1; oX++) {
                for (int oZ = (int)center.z - radius; oZ <= (int)center.z + radius; oZ++) {
                    
                	BlockPos blockpos = new BlockPos(oX, oY, oZ);
                	double distanceSqToBlockPos = this.getDistanceSq(blockpos);
                    
                    RayTraceResult raytraceresult = this.world.rayTraceBlocks(center, new Vec3d((double)((float)oX + 0.5F), (double)((float)oY + 0.5F), (double)((float)oZ + 0.5F)), false, true, false);
                    boolean flag = raytraceresult != null && raytraceresult.getBlockPos().equals(blockpos);
                    
                    flag = flag && distanceSqToBlockPos < closestBlockPosDistanceSq;
                	
                    if (!this.world.isAirBlock(blockpos) &&
                    	this.world.getBlockState(blockpos).getBlock() instanceof HoarderContainerBlock)
                    {
                    	if (flag)
                    	{
                            closestBlockPos = blockpos;
                            closestBlockPosDistanceSq = distanceSqToBlockPos;
                        }
                    }
                }
            }
        }
        return closestBlockPos;
    }
    
    static class GolemAIApproachBlock extends EntityAIBase
	    {
	        private final EntityHoarderGolem golem;
	        private int timeoutCounter;
	        private int maxStayTicks;
	        
	
	        public GolemAIApproachBlock(EntityHoarderGolem golemIn)
	        {
	            this.golem = golemIn;
	            this.setMutexBits(1);
	        }
	
	        /**
	         * Returns whether the EntityAIBase should begin execution.
	         */
	        public boolean shouldExecute()
	        {
	        	return this.golem.getClosestBlockPosInRadius(5) != null;
	        }
	        
	        /**
	         * Returns whether an in-progress EntityAIBase should continue executing
	         */
	        public boolean shouldContinueExecuting()
	        {
	        	return this.timeoutCounter >= -this.maxStayTicks && this.timeoutCounter <= 1200;
	        }
	
	        /**
	         * Used to get a point slightly away from a block to move to, so we're not on top of it.
	         * @param targetBlockPos
	         * @return BlockPos
	         */
	        private BlockPos findTargetPosition(BlockPos targetBlockPos) {
	        	Vec3d entityDirectionFromBlock = new Vec3d(new BlockPos(targetBlockPos).subtract(this.golem.getPosition())).normalize().scale(-1.0D);
        		BlockPos targetPosition = new BlockPos(targetBlockPos).add(entityDirectionFromBlock.x, entityDirectionFromBlock.y, entityDirectionFromBlock.z);
	        	return targetPosition;
	        }
	        
	        /**
	         * Execute a one shot task or start executing a continuous task
	         */
	        public void startExecuting()
	        {
	        	BlockPos targetBlockPos = this.golem.getClosestBlockPosInRadius(5);
	        	if (targetBlockPos != null) {
	        		BlockPos targetPosition = findTargetPosition(targetBlockPos);
	        		if (this.golem.getDistanceSqToCenter(targetPosition) > 1.0D) {
	                    this.golem.getNavigator().tryMoveToXYZ(targetPosition.getX(), targetPosition.getY(), targetPosition.getZ(), 1);
			            this.timeoutCounter = 0;
			            this.maxStayTicks = this.golem.getRNG().nextInt(this.golem.getRNG().nextInt(1200) + 1200) + 1200;
	        		}
	        	}
	        }
	
	        /**
	         * Keep ticking a continuous task that has already been started
	         */
	        public void updateTask()
	        {
	        	BlockPos targetBlockPos = this.golem.getClosestBlockPosInRadius(5);
	        	if (targetBlockPos != null)
	        	{
	        		BlockPos targetPosition = findTargetPosition(targetBlockPos);
		        	if (this.golem.getDistanceSqToCenter(targetPosition) > 1.0D)
		        	{
		                ++this.timeoutCounter;
		                if (this.timeoutCounter % 40 == 0) {
		                    this.golem.getNavigator().tryMoveToXYZ(targetPosition.getX(), targetPosition.getY(), targetPosition.getZ(), 1);
				            this.maxStayTicks = this.golem.getRNG().nextInt(this.golem.getRNG().nextInt(1200) + 1200) + 1200;
			        	}
		            }
		            else
		            {
		            	targetBlockPos = null;
		                --this.timeoutCounter;
		            }
	        	}
	        }
	        
	        /**
	         * Reset the task's internal state. Called when this task is interrupted by another one
	         */
	        public void resetTask()
	        {
	            
	        }
	
	    }
    
    static class GolemAIStoreItems extends EntityAIBase
    {
        private final EntityHoarderGolem golem;

        public GolemAIStoreItems(EntityHoarderGolem entityIn)
        {
            this.golem = entityIn;
            this.setMutexBits(1 | 2);
        }

        /**
         * Returns whether the EntityAIBase should begin execution.
         */
        public boolean shouldExecute()
        {
        	BlockPos closestContainer = this.golem.getClosestBlockPosInRadius(5);
        	return (!this.golem.itemStackHandler.getStackInSlot(0).isEmpty() &&
        			closestContainer != null &&
        			);
        }

        /**
         * Execute a one shot task or start executing a continuous task
         */
        public void startExecuting()
        {
        	if (this.golem.breakBlockPos != null) {
	            super.startExecuting();
	            this.breakingTime = 0;
	            this.blockToBreak = this.golem.world.getBlockState(this.golem.breakBlockPos).getBlock();
        	}
        }

        /**
         * Returns whether an in-progress EntityAIBase should continue executing
         */
        public boolean shouldContinueExecuting()
        {
        	if (this.golem.breakBlockPos != null) {
	            double d0 = this.golem.getDistanceSq(this.golem.breakBlockPos);
	            boolean flag;
	
            	if (d0 < 4.0D)
                {
                    flag = true;
                    return flag;
                }
	
	            flag = false;
	            this.resetTask();
	            return flag;
        	}
        	else {
        		return false;
        	}
        }

        /**
         * Reset the task's internal state. Called when this task is interrupted by another one
         */
        public void resetTask()
        {
        	this.golem.resetBreakProgress();
            super.resetTask();
        }
        
        /**
         * Keep ticking a continuous task that has already been started
         */
        public void updateTask()
        {
            super.updateTask();
            
            if (this.golem.breakBlockPos == null || this.golem.world.isAirBlock(this.golem.breakBlockPos)) {
            	this.resetTask();
            }
            else if (this.golem.breakBlockPos != null) {
            	
            	this.golem.getLookHelper().setLookPosition(
            		this.golem.breakBlockPos.getX() + 0.5D,
            		this.golem.breakBlockPos.getY() + 1.0D,
            		this.golem.breakBlockPos.getZ() + 0.5D,
            		(float)this.golem.getHorizontalFaceSpeed(),
            		(float)this.golem.getVerticalFaceSpeed()
            	);
	
	            this.breakingTime += 4;
	            int i = (int)((float)this.breakingTime / 240.0F * 10.0F);
	
	            if (i != this.previousBreakProgress)
	            {
	                this.golem.world.sendBlockBreakProgress(this.golem.getEntityId(), this.golem.breakBlockPos, i);
	                this.previousBreakProgress = i;
	            }
	
	            if (this.breakingTime == 240)
	            {
	            	this.blockToBreak.dropBlockAsItem(this.golem.world, this.golem.breakBlockPos, this.golem.world.getBlockState(this.golem.breakBlockPos), 0);
	                this.golem.world.setBlockToAir(this.golem.breakBlockPos);
	                this.golem.world.playEvent(1021, this.golem.breakBlockPos, 0);
	                this.golem.world.playEvent(2001, this.golem.breakBlockPos, Block.getIdFromBlock(this.blockToBreak));
	                this.golem.breakBlockPos = null;
	                this.blockToBreak = null;
	            }
            }
        }
    }

}