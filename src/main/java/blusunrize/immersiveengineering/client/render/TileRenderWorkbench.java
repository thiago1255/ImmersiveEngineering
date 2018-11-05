/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render;

import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.render.TileRenderAutoWorkbench.BlueprintLines;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityModWorkbench;
import blusunrize.immersiveengineering.common.items.ItemEngineersBlueprint;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;

public class TileRenderWorkbench extends TileEntitySpecialRenderer<TileEntityModWorkbench>
{
	//	static ModelIEObj model = new ModelIEObj("immersiveengineering:models/workbench.obj")
	//	{
	//		@Override
	//		public IIcon getBlockIcon(String groupName)
	//		{
	//			return IEContent.blockWoodenDevices.getIcon(0, 5);
	//		}
	//	};

	//	public void renderDynamic(TileEntity tile, double x, double y, double z, float partialTicks, int destroyStage)
	@Override
	public void render(TileEntityModWorkbench te, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
	{
		if(te.dummy||!te.getWorld().isBlockLoaded(te.getPos(), false))
			return;

		GlStateManager.pushMatrix();
		GlStateManager.translate(x+.5, y+.5, z+.5);

		int facing = te.getFacing().ordinal();
		int off = te.dummyOffset;

		float angle = facing==2?0: facing==4?90: facing==5?-90: 180;

		GlStateManager.rotate(angle, 0, 1, 0);

		ItemStack stack = te.getInventory().get(0);
		boolean showIngredients = true;
		if(!stack.isEmpty())
		{
			if(stack.getItem() instanceof ItemEngineersBlueprint)
			{
				GlStateManager.pushMatrix();
				double playerDistanceSq = ClientUtils.mc().player.getDistanceSq(te.getPos());
				if(playerDistanceSq < 120)
				{
					BlueprintCraftingRecipe[] recipes = BlueprintCraftingRecipe.findRecipes(ItemNBTHelper.getString(stack, "blueprint"));

					float lineWidth = playerDistanceSq < 25?1: playerDistanceSq < 40?.5f: .1f;
					int l = recipes.length;
					int perRow = l > 6?l-3: l > 4?l-2: l==1?2: l==2?3: l;
					angle = facing==2?0: facing==4?90: facing==5?-90: 180;
					GlStateManager.translate(0, .501, 0);
					GlStateManager.rotate(-90, 1, 0, 0);
					GlStateManager.rotate(-22.5f, 0, 0, 1);
					GlStateManager.translate(0.39, l > 4?.72: .78, 0);
					float scale = l > 4?.009375f: .012f;
					GlStateManager.scale(scale, -scale, scale);
					for(int i = 0; i < l; )
					{
						BlueprintCraftingRecipe recipe = recipes[i%recipes.length];
						BlueprintLines blueprint = recipe==null?null: TileRenderAutoWorkbench.getBlueprintDrawable(recipe, te.getWorld());
						if(blueprint!=null)
						{
							double dX = i < perRow?(.93725/scale-perRow*16.6)+i*16.6: (.70375/scale-i%perRow*16.6);
							double dY = i < perRow?0: -.15625;
							GlStateManager.translate(dX, dY/scale, 0);

							//Width depends on distance
							GlStateManager.disableCull();
							GlStateManager.disableTexture2D();
							GlStateManager.enableBlend();
							float texScale = blueprint.textureScale/16f;
							GlStateManager.scale(1/texScale, 1/texScale, 1/texScale);
							GlStateManager.color(1, 1, 1, 1);
							blueprint.draw(lineWidth);
							GlStateManager.scale(texScale, texScale, texScale);
							GlStateManager.enableAlpha();
							GlStateManager.enableTexture2D();
							GlStateManager.enableCull();
							GlStateManager.translate(-dX, -dY/scale, 0);
							i++;
						}
					}
				}
				GlStateManager.popMatrix();
			}
			else
			{
				showIngredients = false;
				GlStateManager.pushMatrix();
				GlStateManager.disableLighting();
				GlStateManager.translate(0, .5625, 0);

				GlStateManager.rotate(180, 0, 1, 0);
				GlStateManager.rotate(90, 1, 0, 0);
				GlStateManager.translate(-.875, 0, 0);
				GlStateManager.scale(.75f, .75f, .75f);
				{
					try
					{
						ClientUtils.mc().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.FIXED);
					} catch(Exception e)
					{
						e.printStackTrace();
					}
				}
				GlStateManager.enableLighting();
				GlStateManager.popMatrix();
			}
		}
		if(showIngredients)
		{
			GlStateManager.disableLighting();
			for(int i = 1; i < te.getInventory().size(); i++)
			{
				double dX, dZ;
				if(i < 5)
				{
					dX = -.5+(i==2?-.0625: i==4?.03215: 0);
					dZ = i*.25-.625;
				}
				else
				{
					dX = -1.25;
					dZ = -.125+(i-5)*-.25;
				}

				stack = te.getInventory().get(i);
				if(!stack.isEmpty())
				{
					GlStateManager.pushMatrix();
					GlStateManager.rotate(180, 0, 1, 0);
					GlStateManager.rotate(90, 1, 0, 0);
					GlStateManager.translate(dX, dZ, -.515);
					GlStateManager.scale(.25f, .25f, .25f);
					{
						try
						{
							ClientUtils.mc().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.FIXED);
						} catch(Exception e)
						{
							e.printStackTrace();
						}
					}
					GlStateManager.popMatrix();
				}
			}
			GlStateManager.enableLighting();
		}

		GlStateManager.popMatrix();

	}
	//	@Override
	//	public void renderStatic(TileEntity tile, Tessellator tes, Matrix4 translationMatrix, Matrix4 rotationMatrix)
	//	{
	//		int f = ((TileEntityModWorkbench)tile).facing;
	//		int off = ((TileEntityModWorkbench)tile).dummyOffset;
	//		float angle = f==2?180: f==4?-90: f==5?90: 0;
	//		boolean mirror = f==2?off>0: f==3?off<0: f==4?off<0: f==5?off>0 : false;
	//
	//		switch(f)
	//		{
	//		case 2:
	//			angle = 180;
	//			translationMatrix.translate(mirror?1:0,0,.5);
	//			break;
	//		case 3:
	//			translationMatrix.translate(!mirror?1:0,0,.5);
	//			break;
	//		case 4:
	//			angle = -90;
	//			translationMatrix.translate(.5,0,!mirror?1:0);
	//			break;
	//		case 5:
	//			angle = 90;
	//			translationMatrix.translate(.5,0,mirror?1:0);
	//			break;
	//		}
	//
	//		if(mirror)
	//			translationMatrix.scale(new Vertex(f<4?-1:1,1,f>3?-1:1));
	//		rotationMatrix.rotate(Math.toRadians(angle), 0.0, 1.0, 0.0);
	//		model.render(tile, tes, translationMatrix, rotationMatrix, 0, mirror);
	//	}
}