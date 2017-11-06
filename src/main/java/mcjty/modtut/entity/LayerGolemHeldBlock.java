package mcjty.modtut.entity;

import net.minecraft.client.renderer.entity.layers.LayerRenderer;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerGolemHeldBlock implements LayerRenderer<EntityGolem>
{
    private final RenderGolem golemRenderer;

    public LayerGolemHeldBlock(RenderGolem golemRendererIn)
    {
        this.golemRenderer = golemRendererIn;
    }

    public void doRenderLayer(EntityGolem entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale)
    {
    	ItemStack itemstack = entitylivingbaseIn.getHeldItemStack();

        if (!itemstack.isEmpty())
        {
        	if (itemstack.getItem() instanceof ItemBlock)
        	{
        		BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
                GlStateManager.enableRescaleNormal();
                GlStateManager.pushMatrix();
                GlStateManager.translate(0.0F, 0.6875F, -0.75F);
                GlStateManager.rotate(20.0F, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
                GlStateManager.translate(0.25F, 0.1875F, 0.25F);
                GlStateManager.scale(-0.5F, -0.5F, 0.5F);
                int i = entitylivingbaseIn.getBrightnessForRender();
                int j = i % 65536;
                int k = i / 65536;
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j, (float)k);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                this.golemRenderer.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
                
                ItemBlock itemblock = (ItemBlock)entitylivingbaseIn.getHeldItemStack().getItem();
        		// hopefully leaving the hit vector as 0 here doesn't break anything
        		// not sure what it ought to be
        		IBlockState iblockstate = itemblock.getBlock().getStateForPlacement(
    				entitylivingbaseIn.world, new BlockPos(entitylivingbaseIn), entitylivingbaseIn.getHorizontalFacing(),
        			0, 0, 0, entitylivingbaseIn.getHeldItemStack().getMetadata(), entitylivingbaseIn, entitylivingbaseIn.getActiveHand()
        		);
                
                blockrendererdispatcher.renderBlockBrightness(iblockstate, 1.0F);
                GlStateManager.popMatrix();
                GlStateManager.disableRescaleNormal();
        	}
        	else
        	{
        		
                GlStateManager.enableRescaleNormal();
                GlStateManager.pushMatrix();
                GlStateManager.translate(0.0F, 0.6875F, -0.75F);
                GlStateManager.rotate(20.0F, 1.0F, 0.0F, 0.0F);
                int i = entitylivingbaseIn.getBrightnessForRender();
                int j = i % 65536;
                int k = i / 65536;
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j, (float)k);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                this.golemRenderer.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
                
                Minecraft.getMinecraft().getItemRenderer().renderItem(entitylivingbaseIn, itemstack, ItemCameraTransforms.TransformType.FIXED);
                
                GlStateManager.popMatrix();
                GlStateManager.disableRescaleNormal();
        		
        		
        	}
        }
    }

    public boolean shouldCombineTextures()
    {
        return false;
    }
}