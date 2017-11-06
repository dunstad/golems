package mcjty.modtut.entity;

import net.minecraft.entity.monster.EntityMob;

import com.google.common.collect.Sets;

import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class EntityGolem extends EntityMob
{
	protected final ItemStackHandler itemStackHandler;
	
	protected static final Set<Block> CARRIABLE_BLOCKS = Sets.<Block>newIdentityHashSet();
	protected static final DataParameter<ItemStack> CARRIED_ITEM_STACK = EntityDataManager.<ItemStack>createKey(EntityGolem.class, DataSerializers.ITEM_STACK);
	protected static final DataParameter<Boolean> SCREAMING = EntityDataManager.<Boolean>createKey(EntityGolem.class, DataSerializers.BOOLEAN);

    public EntityGolem(World worldIn)
    {
        super(worldIn);
        this.setSize(0.3F, 1.45F);
        this.stepHeight = 1.0F;
        
        itemStackHandler = new ItemStackHandler() {
            
        	@Override
            protected void onContentsChanged(int slot) {
        		ItemStack itemstack = EntityGolem.this.itemStackHandler.getStackInSlot(slot);
        		EntityGolem.this.setHeldItemStack(itemstack);
            }
            
        	@Override
            public int getSlotLimit(int slot)
            {
                return 16;
            }
        	
        };
    }
    
    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(itemStackHandler);
        }
        return super.getCapability(capability, facing);
    }

    protected boolean canDespawn()
    {
        return false;
    }

    protected void initEntityAI()
    {
        this.tasks.addTask(2, new EntityAIAttackMelee(this, 1.0D, false));
        this.tasks.addTask(8, new EntityAIWanderAvoidWater(this, 1.0D, 0.0F));
        this.tasks.addTask(9, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(10, new EntityAILookIdle(this));
        this.tasks.addTask(11, new GolemAIChamp(this));
        this.targetTasks.addTask(2, new EntityAIHurtByTarget(this, false, new Class[0]));
    }

    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6.0D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(15.0D);
    }

    protected void entityInit()
    {
        super.entityInit();
        this.dataManager.register(CARRIED_ITEM_STACK, ItemStack.EMPTY);
        this.dataManager.register(SCREAMING, Boolean.valueOf(false));
    }

    public static void registerFixesGolem(DataFixer fixer)
    {
        EntityLiving.registerFixesMob(fixer, EntityGolem.class);
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    public void writeEntityToNBT(NBTTagCompound compound)
    {
        super.writeEntityToNBT(compound);
        ItemStack itemstack = this.itemStackHandler.getStackInSlot(0);

        NBTTagList nbttaglist = new NBTTagList();

        if (!itemstack.isEmpty())
        {
            nbttaglist.appendTag(itemstack.writeToNBT(new NBTTagCompound()));
        }

        compound.setTag("Inventory", nbttaglist);
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readEntityFromNBT(NBTTagCompound compound)
    {
        super.readEntityFromNBT(compound);
        ItemStack itemstack;

        NBTTagList nbttaglist = compound.getTagList("Inventory", 10);

        for (int i = 0; i < nbttaglist.tagCount(); ++i)
        {
            itemstack = new ItemStack(nbttaglist.getCompoundTagAt(i));

            if (!itemstack.isEmpty())
            {
            	this.itemStackHandler.setStackInSlot(0, itemstack);
            }
            else
            {
            	this.itemStackHandler.setStackInSlot(0, ItemStack.EMPTY);
            }
        }
    }

    public float getEyeHeight()
    {
        return 1.275F;
    }

    /**
     * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons
     * use this to react to sunlight and start to burn.
     */
    public void onLivingUpdate()
    {
        this.isJumping = false;
        super.onLivingUpdate();
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn)
    {
        return SoundEvents.ENTITY_ENDERMEN_HURT;
    }

    protected SoundEvent getDeathSound()
    {
        return SoundEvents.ENTITY_ENDERMEN_DEATH;
    }
    
    public void onDeath(DamageSource cause) {
    	super.onDeath(cause);
    }

    /**
     * Drop the equipment for this entity.
     */
    protected void dropEquipment(boolean wasRecentlyHit, int lootingModifier)
    {
        super.dropEquipment(wasRecentlyHit, lootingModifier);

        if (this.itemStackHandler.getStackInSlot(0) != ItemStack.EMPTY)
        {
            this.entityDropItem(this.itemStackHandler.getStackInSlot(0), 0.0F);
            this.itemStackHandler.setStackInSlot(0, ItemStack.EMPTY);
        }
    }

    @Nullable
    protected ResourceLocation getLootTable()
    {
        return null;
    }

    /**
     * Sets this golem's held block state
     */
    public void setHeldItemStack(ItemStack itemstack)
    {
        this.dataManager.set(CARRIED_ITEM_STACK, itemstack);
    }

    /**
     * Gets this golem's held block state
     */
    @Nullable
    public ItemStack getHeldItemStack()
    {
        return this.dataManager.get(CARRIED_ITEM_STACK);
    }

    /**
     * Called when the entity is attacked.
     */
    public boolean attackEntityFrom(DamageSource source, float amount)
    {
        if (this.isEntityInvulnerable(source) || source == DamageSource.DROWN)
        {
            return false;
        }
        else
        {
        	
        	if (!this.world.isRemote) {
        		
        		if (this.itemStackHandler.getStackInSlot(0) != ItemStack.EMPTY)
        		{
        			this.dropEquipment(true, 0);
        			this.itemStackHandler.setStackInSlot(0, ItemStack.EMPTY);
        		}        		
        	}
        	
            boolean flag = super.attackEntityFrom(source, amount);
            return flag;
        }
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

    public boolean isScreaming()
    {
        return ((Boolean)this.dataManager.get(SCREAMING)).booleanValue();
    }

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

    static class GolemAIChamp extends EntityAIBase
	    {
	        /** The entity that is looking idle. */
	        private final EntityGolem golem;
	        /** A decrementing tick that stops the entity from being idle once it reaches 0. */
	        private int idleTime;
	
	        public GolemAIChamp(EntityGolem golemIn)
	        {
	            this.golem = golemIn;
	        }
	
	        /**
	         * Returns whether the EntityAIBase should begin execution.
	         */
	        public boolean shouldExecute()
	        {
	            return true;
	        }
	
	        /**
	         * Returns whether an in-progress EntityAIBase should continue executing
	         */
	        public boolean shouldContinueExecuting()
	        {
	            return true;
	        }
	
	        /**
	         * Execute a one shot task or start executing a continuous task
	         */
	        public void startExecuting()
	        {
	            this.idleTime = 20 + this.golem.getRNG().nextInt(20);
	        }
	
	        /**
	         * Keep ticking a continuous task that has already been started
	         */
	        public void updateTask()
	        {
	        	if (!this.golem.getNavigator().noPath()) {
	        		this.idleTime -= 3;
	        	}
	            --this.idleTime;
	            if (idleTime < 0) {
	            	this.golem.dataManager.set(SCREAMING, Boolean.valueOf(!this.golem.isScreaming()));
	            	if (this.golem.isScreaming()) {
	            		this.idleTime = 3 + this.golem.getRNG().nextInt(2);
	            	}
	            	else {
	            		this.idleTime = 5 + this.golem.getRNG().nextInt(60);	            	
	            		this.golem.playSound(SoundEvents.BLOCK_STONE_PLACE, this.golem.getSoundVolume(), this.golem.getSoundPitch());
	            	}
	            }
	        }
	    }
    
    static class GolemAIGoToItem extends EntityAIBase
		{
		    private final EntityGolem golem;
		    private Entity targetEntity;
		    private EntityItem closestItemBlockEntity;
		    private double movePosX;
		    private double movePosY;
		    private double movePosZ;
		    private double speed;
		    /** If the distance to the target entity is further than this, this AI task will not run. */
		    private final float maxTargetDistance;
		    private final Predicate<ItemStack> itemCondition;
		
		    public GolemAIGoToItem(EntityGolem golemIn, Predicate<ItemStack> itemCondition)
		    {
		        this.golem = golemIn;
		        this.maxTargetDistance = 10.0F;
		        this.speed = 1.0D;
		        this.setMutexBits(1);
		        this.itemCondition = itemCondition;
		    }
		
		    /**
		     * Returns whether the EntityAIBase should begin execution.
		     */
		    public boolean shouldExecute()
		    {
		    	ItemStackHandler itemstackhandler = this.golem.itemStackHandler;
		    	Boolean noHeldItem = this.golem.itemStackHandler.getStackInSlot(0) == ItemStack.EMPTY;
		    	Boolean heldStackNotFull = itemstackhandler.getStackInSlot(0).getCount() < itemstackhandler.getSlotLimit(0); 
		    	if (noHeldItem || heldStackNotFull) {
			    	this.closestItemBlockEntity = null;
			    	this.targetEntity = null;
			    	AxisAlignedBB axis = new AxisAlignedBB(
		    			this.golem.posX - maxTargetDistance,
		    			this.golem.posY - maxTargetDistance,
		    			this.golem.posZ - maxTargetDistance,
		    			this.golem.posX + maxTargetDistance,
		    			this.golem.posY + maxTargetDistance,
		    			this.golem.posZ + maxTargetDistance
			    	);
			    	for (EntityItem entityitem : this.golem.world.getEntitiesWithinAABB(EntityItem.class, axis))
		            {
		                if (!entityitem.isDead && !entityitem.getItem().isEmpty() && !entityitem.cannotPickup())
		                {
		                	ItemStack itemstack = entityitem.getItem();
		                	Boolean sameAsHeld = itemstack.getItem().equals(this.golem.itemStackHandler.getStackInSlot(0).getItem());
		                	if (this.itemCondition.test(itemstack) &&
		                		(noHeldItem ||
		                		(heldStackNotFull && sameAsHeld))) {
		                		if ((this.closestItemBlockEntity == null) ||
	        						(this.golem.getDistanceSqToEntity(this.closestItemBlockEntity) > this.golem.getDistanceSqToEntity(entityitem))) {
		                			this.closestItemBlockEntity = entityitem;
		                		}
		                	}
		                }
		            }
			        this.targetEntity = this.closestItemBlockEntity;
			
			        if (this.targetEntity == null)
			        {
			            return false;
			        }
			        else if (this.targetEntity.getDistanceSqToEntity(this.golem) > (double)(this.maxTargetDistance * this.maxTargetDistance))
			        {
			            return false;
			        }
			        else
			        {
			        	BlockPos entityLocation = new BlockPos(targetEntity);
		                this.movePosX = entityLocation.getX();
		                this.movePosY = entityLocation.getY();
		                this.movePosZ = entityLocation.getZ();
		                return true;
			        }
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
		        return !this.golem.getNavigator().noPath() && this.targetEntity.isEntityAlive() && this.targetEntity.getDistanceSqToEntity(this.golem) < (double)(this.maxTargetDistance * this.maxTargetDistance);
		    }
		
		    /**
		     * Reset the task's internal state. Called when this task is interrupted by another one
		     */
		    public void resetTask()
		    {
		        this.targetEntity = null;
		    }
		
		    /**
		     * Execute a one shot task or start executing a continuous task
		     */
		    public void startExecuting()
		    {
		        this.golem.getNavigator().tryMoveToXYZ(this.movePosX, this.movePosY, this.movePosZ, this.speed);
		    }
		    
		}
    
}