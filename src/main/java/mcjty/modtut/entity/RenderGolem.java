package mcjty.modtut.entity;

import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.item.ItemStack;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.Render;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraft.client.renderer.GlStateManager;

import net.minecraft.client.model.ModelEnderman;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderGolem extends RenderLiving<EntityGolem>
{
    private static ResourceLocation GOLEM_TEXTURES;
    private final float scale;

    public static final Factory FACTORY = new Factory();

    public RenderGolem(RenderManager renderManagerIn)
    {
        super(renderManagerIn, new ModelEnderman(0.0F), 0.5F);
        this.scale = 0.5F;
        this.addLayer(new LayerGolemEyes(this));
        this.addLayer(new LayerGolemHeldBlock(this));
    }

    public ModelEnderman getMainModel()
    {
        return (ModelEnderman)super.getMainModel();
    }

    /**
     * Renders the desired {@code T} type Entity.
     */
    public void doRender(EntityGolem entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
    	ItemStack itemstack = entity.getHeldItemStack();
        ModelEnderman modelEnderman = this.getMainModel();
        modelEnderman.isCarrying = !itemstack.isEmpty();
        modelEnderman.isAttacking = entity.isScreaming();

        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
     */
    protected ResourceLocation getEntityTexture(EntityGolem entity)
    {
    	if (entity instanceof EntityBreakerGolem) {
    		GOLEM_TEXTURES = new ResourceLocation("modtut:textures/entity/golem.png");
    	}
    	else if (entity instanceof EntityHoarderGolem) {
    		GOLEM_TEXTURES = new ResourceLocation("modtut:textures/entity/hoarder.png");
    	}
        return GOLEM_TEXTURES;
    }

    public static class Factory implements IRenderFactory<EntityGolem> {

        @Override
        public Render<? super EntityGolem> createRenderFor(RenderManager manager) {
            return new RenderGolem(manager);
        }

    }

    protected void preRenderCallback(EntityGolem entitylivingbaseIn, float partialTickTime)
    {
        GlStateManager.scale(this.scale, this.scale, this.scale);
    }
}