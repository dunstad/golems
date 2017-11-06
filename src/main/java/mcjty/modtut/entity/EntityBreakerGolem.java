package mcjty.modtut.entity;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityBreakerGolem extends EntityGolem
{
	private BlockPos targetBlockPos;
    private BlockPos breakBlockPos;
    private BlockPos targetPosition;

    public EntityBreakerGolem(World worldIn)
    {
        super(worldIn);
        this.setCanPickUpLoot(true);
    }

    @Override
    protected void initEntityAI()
    {
        super.initEntityAI();
        this.tasks.addTask(4, new EntityBreakerGolem.GolemAIBreakBlock(this));
        this.tasks.addTask(5, new EntityBreakerGolem.GolemAIGoToItem(this, new Predicate<ItemStack>() {
        	public boolean test(ItemStack itemstack) {
        		return itemstack.getItem() instanceof ItemBlock;
        	}
        }));
        this.tasks.addTask(6, new EntityBreakerGolem.GolemAIApproachBlock(this));
        this.tasks.addTask(7, new EntityBreakerGolem.AITargetBlock(this));
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
    
    @Override
    public void onDeath(DamageSource cause) {
    	super.onDeath(cause);
    	this.resetBreakProgress();
    }
    
    private void resetBreakProgress() {
    	if (this.breakBlockPos != null) {    		
    		this.world.sendBlockBreakProgress(this.getEntityId(), this.breakBlockPos, -1);
    		this.breakBlockPos = null;
    	}
    	this.targetBlockPos = null;
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

        if (this.canBreakerPickupItem(item))
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

    private boolean canBreakerPickupItem(Item itemIn)
    {
        return itemIn instanceof ItemBlock;
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

    static class AITargetBlock extends EntityAIBase
        {
            private final EntityBreakerGolem golem;

            public AITargetBlock(EntityBreakerGolem p_i45841_1_)
            {
                this.golem = p_i45841_1_;
            }

            /**
             * Returns whether the EntityAIBase should begin execution.
             */
            public boolean shouldExecute()
            {
            	if (!this.golem.world.getGameRules().getBoolean("mobGriefing"))
                {
                    return false;
                }
                else if (this.golem.targetBlockPos != null || this.golem.breakBlockPos != null)
                {
                    return false;
                }
                else
                {
                    return true;
                }
            }
            
            /**
             * Returns a BlockPos near the golem, or null if none are found
             */
            private BlockPos getClosestBlockPosInRadius(int radius) {
                Vec3d center = new Vec3d((double)((float)MathHelper.floor(this.golem.posX) + 0.5F), (double)((float)this.golem.posY + 0.5F), (double)((float)MathHelper.floor(this.golem.posZ) + 0.5F));
                BlockPos closestBlockPos = null;
                double closestBlockPosDistanceSq = 999.0D;
                for (int oY = (int)center.y; oY <= (int)center.y + 2; oY++) {
	                for (int oX = (int)center.x - radius - 1; oX <= (int)center.x + radius - 1; oX++) {
	                    for (int oZ = (int)center.z - radius; oZ <= (int)center.z + radius; oZ++) {
	                        
	                    	BlockPos blockpos = new BlockPos(oX, oY, oZ);
	                    	double distanceSqToBlockPos = this.golem.getDistanceSq(blockpos);
	                        
	                        RayTraceResult raytraceresult = this.golem.world.rayTraceBlocks(center, new Vec3d((double)((float)oX + 0.5F), (double)((float)oY + 0.5F), (double)((float)oZ + 0.5F)), false, true, false);
	                        boolean flag = raytraceresult != null && raytraceresult.getBlockPos().equals(blockpos);
	                        
	                        flag = flag && distanceSqToBlockPos < closestBlockPosDistanceSq;
	                    	
	                        if (
                        		!this.golem.world.isAirBlock(blockpos) &&
                        		!(this.golem.itemStackHandler.getStackInSlot(0).isEmpty())
	                        )
	                        {
	                        	ItemBlock itemblock = (ItemBlock)this.golem.itemStackHandler.getStackInSlot(0).getItem();
	             	    		// hopefully leaving the hit vector as 0 here doesn't break anything
	             	    		// not sure what it ought to be
	             	    		IBlockState iblockstate = itemblock.getBlock().getStateForPlacement(
	             	    			this.golem.world, new BlockPos(this.golem), this.golem.getHorizontalFacing(),
	             	    			0, 0, 0, this.golem.itemStackHandler.getStackInSlot(0).getMetadata(), this.golem, this.golem.getActiveHand()
	             	    		);
	                        	if (
	                        		flag &&
	                        		((iblockstate == this.golem.world.getBlockState(blockpos)))
	                        	) {
		                            closestBlockPos = blockpos;
		                            closestBlockPosDistanceSq = distanceSqToBlockPos;
		                        }
	                        }
	                    }
	                }
                }
                return closestBlockPos;
            }

            public void updateTask()
            {
                this.golem.targetBlockPos = this.getClosestBlockPosInRadius(5);
            }
        }
    
    static class GolemAIApproachBlock extends EntityAIBase
	    {
	        private final EntityBreakerGolem golem;
	        private int timeoutCounter;
	        private int maxStayTicks;
	        
	
	        public GolemAIApproachBlock(EntityBreakerGolem golemIn)
	        {
	            this.golem = golemIn;
	            this.setMutexBits(1);
	        }
	
	        /**
	         * Returns whether the EntityAIBase should begin execution.
	         */
	        public boolean shouldExecute()
	        {
	        	if (this.golem.targetBlockPos != null && this.golem.breakBlockPos == null) {
        			return true;
	        	}
	        	else {	        		
	        		return false;
	        	}
	        }
	        
	        /**
	         * Returns whether an in-progress EntityAIBase should continue executing
	         */
	        public boolean shouldContinueExecuting()
	        {
	        	return this.timeoutCounter >= -this.maxStayTicks && this.timeoutCounter <= 1200;
	        }
	
	        /**
	         * Execute a one shot task or start executing a continuous task
	         */
	        public void startExecuting()
	        {
	        	if (this.golem.targetBlockPos != null) {
	        		Vec3d entityDirectionFromBlock = new Vec3d(new BlockPos(this.golem.targetBlockPos).subtract(this.golem.getPosition())).normalize().scale(1.0D);
	        		this.golem.targetPosition = new BlockPos(this.golem.targetBlockPos).add(-entityDirectionFromBlock.x, -entityDirectionFromBlock.y, -entityDirectionFromBlock.z);
	        		if (this.golem.getDistanceSqToCenter(this.golem.targetPosition) > 1.0D) {
	                    this.golem.getNavigator().tryMoveToXYZ(this.golem.targetPosition.getX(), this.golem.targetPosition.getY(), this.golem.targetPosition.getZ(), 1);
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
	        	if (this.golem.targetBlockPos != null && this.golem.getDistanceSqToCenter(this.golem.targetPosition) > 1.0D)
	        	{
	                ++this.timeoutCounter;
	                if (this.timeoutCounter % 40 == 0) {
	                    this.golem.getNavigator().tryMoveToXYZ(this.golem.targetPosition.getX(), this.golem.targetPosition.getY(), this.golem.targetPosition.getZ(), 1);
			            this.maxStayTicks = this.golem.getRNG().nextInt(this.golem.getRNG().nextInt(1200) + 1200) + 1200;
		        	}
	            }
	            else
	            {
	            	if (this.golem.targetBlockPos != null) {
	            		this.golem.breakBlockPos = new BlockPos(this.golem.targetBlockPos); 
	            		this.golem.targetBlockPos = null;
	            	}
	                --this.timeoutCounter;
	            }
	        }
	        
	        /**
	         * Reset the task's internal state. Called when this task is interrupted by another one
	         */
	        public void resetTask()
	        {
	            this.golem.targetBlockPos = null;
	        }
	
	    }
    
    static class GolemAIBreakBlock extends EntityAIBase
	    {
	        private int breakingTime;
	        private int previousBreakProgress = -1;
	        private final EntityBreakerGolem golem;
	        private Block blockToBreak;
	
	        public GolemAIBreakBlock(EntityBreakerGolem entityIn)
	        {
	            this.golem = entityIn;
	            this.setMutexBits(1 | 2);
	        }
	
	        /**
	         * Returns whether the EntityAIBase should begin execution.
	         */
	        public boolean shouldExecute()
	        {
	        	if (
	        			this.golem.breakBlockPos == null ||
	        			!this.golem.world.getGameRules().getBoolean("mobGriefing") ||
	        			!net.minecraftforge.event.ForgeEventFactory.onEntityDestroyBlock(this.golem, this.golem.breakBlockPos, this.golem.world.getBlockState(this.golem.breakBlockPos))
	        	)
	            {
	                return false;
	            }
	            else
	            {
	                return true;
	            }
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