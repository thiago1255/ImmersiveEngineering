/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.manual;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.ManualHelper;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.IEItemFontRender;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.network.MessageShaderManual;
import blusunrize.immersiveengineering.common.util.network.MessageShaderManual.MessageType;
import blusunrize.lib.manual.ManualEntry;
import blusunrize.lib.manual.ManualInstance;
import blusunrize.lib.manual.Tree;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import org.lwjgl.input.Keyboard;

import java.util.Locale;

public class IEManualInstance extends ManualInstance
{
	public IEManualInstance()
	{
		super(new IEItemFontRender(), "immersiveengineering:textures/gui/manual.png",
				new ResourceLocation(ImmersiveEngineering.MODID, "manual"));
		this.fontRenderer.colorCode[0+6] = Lib.COLOUR_I_ImmersiveOrange;
		this.fontRenderer.colorCode[16+6] = Lib.COLOUR_I_ImmersiveOrangeShadow;
		((IEItemFontRender)this.fontRenderer).createColourBackup();
		if(Minecraft.getMinecraft().gameSettings.language!=null)
		{
			this.fontRenderer.setUnicodeFlag(ClientUtils.mc().getLanguageManager().isCurrentLocaleUnicode());
			this.fontRenderer.setBidiFlag(ClientUtils.mc().getLanguageManager().isCurrentLanguageBidirectional());
		}
		((IReloadableResourceManager)ClientUtils.mc().getResourceManager()).registerReloadListener(this.fontRenderer);
	}

	@Override
	public String getDefaultResourceDomain()
	{
		return ImmersiveEngineering.MODID;
	}

	@Override
	public String formatText(String s)
	{
//		if(!s.contains(" "))//if it contains spaces, it's probably already translated.
//		{
//			s = ManualUtils.attemptStringTranslation("ie.manual.entry.%s",s);
//			String translKey =  + s;
//			String translated = I18n.format(translKey);
//			if(!translKey.equals(translated))
//				s = translated;
//		}
		String splitKey = ";";

		s = s.replaceAll("<br>", "\n");
		int start;
		int overflow = 0;
		while((start = s.indexOf("<config")) >= 0&&overflow < 50)
		{
			overflow++;
			int end = s.indexOf(">", start);
			String rep = s.substring(start, end+1);
			String result = formatConfigEntry(rep, splitKey);

			s = s.replaceFirst(rep, result);
		}
		overflow = 0;
		while((start = s.indexOf("<dim")) >= 0&&overflow < 50)
		{
			overflow++;
			int end = s.indexOf(">", start);
			String rep = s.substring(start, end+1);
			String[] segment = rep.substring(0, rep.length()-1).split(splitKey);
			if(segment.length < 2)
				break;
			String result = "";
			try
			{
				int dim = Integer.parseInt(segment[1]);
				World world = DimensionManager.getWorld(dim);
				if(world!=null&&world.provider!=null)
				{
					String name = world.provider.getDimensionType().getName();
					if(name.toLowerCase(Locale.ENGLISH).startsWith("the ")||name.toLowerCase(Locale.ENGLISH).startsWith("the_"))
						name = name.substring(4, 5).toUpperCase()+name.substring(5);
					result = name;
				}
				else
					result = "Dimension "+dim;
			} catch(Exception ex)
			{
				ex.printStackTrace();
			}
			s = s.replaceFirst(rep, result);
		}

		overflow = 0;
		while((start = s.indexOf("<keybind")) >= 0&&overflow < 50)
		{
			overflow++;
			int end = s.indexOf(">", start);
			String rep = s.substring(start, end+1);
			String[] segment = rep.substring(0, rep.length()-1).split(splitKey);
			if(segment.length < 2)
				break;
			String result = "";
			for(KeyBinding kb : ClientUtils.mc().gameSettings.keyBindings)
				if(segment[1].equalsIgnoreCase(kb.getKeyDescription()))
				{
					result = Utils.toCamelCase(Keyboard.getKeyName(kb.getKeyCode()));
					break;
				}
			s = s.replaceFirst(rep, result);
		}

		if(improveReadability())
		{
			overflow = 0;
			int end = 0;
			while((start = s.indexOf(TextFormatting.RESET.toString(), end)) >= 0&&overflow < 50)
			{
				overflow++;
				end = start+TextFormatting.RESET.toString().length();
				s = s.substring(0, end)+TextFormatting.BOLD.toString()+s.substring(end);
			}
			s = TextFormatting.BOLD+s;
		}
		return s;
	}

	@Override
	public void openManual()
	{
		if(improveReadability())
		{
			((IEItemFontRender)this.fontRenderer).spacingModifier = -.5f;
			((IEItemFontRender)this.fontRenderer).customSpaceWidth = 1f;
		}
	}

	@Override
	public void titleRenderPre()
	{
		if(improveReadability())
		{
			((IEItemFontRender)this.fontRenderer).spacingModifier = .5f;
			((IEItemFontRender)this.fontRenderer).customSpaceWidth = 4f;
		}
	}

	@Override
	public void titleRenderPost()
	{
		if(improveReadability())
		{
			((IEItemFontRender)this.fontRenderer).spacingModifier = -.5f;
			((IEItemFontRender)this.fontRenderer).customSpaceWidth = 1f;
		}
	}

	@Override
	public void entryRenderPre()
	{
		if(improveReadability())
			((IEItemFontRender)this.fontRenderer).verticalBoldness = true;
	}

	@Override
	public void entryRenderPost()
	{
		if(improveReadability())
			((IEItemFontRender)this.fontRenderer).verticalBoldness = false;
	}

	@Override
	public void tooltipRenderPre()
	{
		if(improveReadability())
		{
			((IEItemFontRender)this.fontRenderer).spacingModifier = 0f;
			((IEItemFontRender)this.fontRenderer).customSpaceWidth = 4f;
			((IEItemFontRender)this.fontRenderer).verticalBoldness = false;
		}
	}

	@Override
	public void tooltipRenderPost()
	{
		if(improveReadability())
		{
			((IEItemFontRender)this.fontRenderer).spacingModifier = -.5f;
			((IEItemFontRender)this.fontRenderer).customSpaceWidth = 1f;
			((IEItemFontRender)this.fontRenderer).verticalBoldness = true;
		}
	}


	@Override
	public String getManualName()
	{
		return I18n.format("item.immersiveengineering.tool.manual.name");
	}

	@Override
	public String formatCategoryName(String s)
	{
		return (improveReadability()?TextFormatting.BOLD: "")+I18n.format("ie.manual.category."+s+".name");
	}

	@Override
	public String formatEntryName(String s)
	{
		return (improveReadability()?TextFormatting.BOLD: "")+s;
	}

	@Override
	public String formatEntrySubtext(String s)
	{
		return s;
	}

	//TODO this was changed to snake_case. Where else do I need to change it
	private static final ResourceLocation SHADER_ENTRY = new ResourceLocation(ImmersiveEngineering.MODID, "shader_list");

	@Override
	public boolean showNodeInList(Tree.AbstractNode<ResourceLocation, ManualEntry> node)
	{
		ResourceLocation nodeLoc = node.isLeaf()?node.getLeafData().getLocation(): node.getNodeData();
		if(ImmersiveEngineering.MODID.equals(nodeLoc.getResourceDomain())&&
				nodeLoc.getResourcePath().startsWith(ManualHelper.CAT_UPDATE))
			return IEConfig.showUpdateNews;
		return !nodeLoc.equals(SHADER_ENTRY);
	}

	@Override
	public boolean showCategoryInList(String category)
	{
		return true;
	}

	@Override
	public String formatLink(ManualLink link)
	{
		return TextFormatting.GOLD+"  -> "+link.getKey().getTitle()+", "+
				(link.getPage()+1);
	}

	@Override
	public void openEntry(ManualEntry entry)
	{
		if(SHADER_ENTRY.equals(entry.getLocation()))
			ImmersiveEngineering.packetHandler.sendToServer(new MessageShaderManual(MessageType.SYNC));
	}

	@Override
	public int getTitleColour()
	{
		return 0xf78034;
	}

	@Override
	public int getSubTitleColour()
	{
		return 0xf78034;
	}

	@Override
	public int getTextColour()
	{
		return improveReadability()?0: 0x555555;
	}

	@Override
	public int getHighlightColour()
	{
		return 0xd4804a;
	}

	@Override
	public int getPagenumberColour()
	{
		return 0x9c917c;
	}

	@Override
	public boolean allowGuiRescale()
	{
		return IEConfig.adjustManualScale;
	}

	@Override
	public boolean improveReadability()
	{
		return IEConfig.badEyesight;
	}

	public String formatConfigEntry(String rep, String splitKey)
	{
		String[] segment = rep.substring(0, rep.length()-1).split(splitKey);
		if(segment.length < 3)
			return "~ERROR0~";
		if(segment[1].equalsIgnoreCase("b"))
		{
			if(segment.length > 3)
				return (Config.manual_bool.get(segment[2])?segment[3]: segment.length > 4?segment[4]: "");
			else
				return ""+Config.manual_bool.get(segment[2]);
		}
		else if(segment[1].equalsIgnoreCase("i"))
			return ""+Config.manual_int.get(segment[2]);
		else if(segment[1].equalsIgnoreCase("iA"))
		{
			int[] iA = Config.manual_intA.get(segment[2]);
			if(segment.length > 3)
				try
				{
					if(segment[3].startsWith("l"))
					{
						int limiter = Integer.parseInt(segment[3].substring(1));
						StringBuilder result = new StringBuilder();
						for(int i = 0; i < limiter; i++)
							result.append(i > 0?", ": "").append(iA[i]);
						return result.toString();
					}
					else
					{
						int idx = Integer.parseInt(segment[3]);
						return ""+iA[idx];
					}
				} catch(Exception ex)
				{
					return "~ERROR1~";
				}
			else
			{
				StringBuilder result = new StringBuilder();
				for(int i = 0; i < iA.length; i++)
					result.append(i > 0?", ": "").append(iA[i]);
				return result.toString();
			}
		}
		else if(segment[1].equalsIgnoreCase("d"))
			return ""+Config.manual_double.get(segment[2]);
		else if(segment[1].equalsIgnoreCase("dA"))
		{
			double[] iD = Config.manual_doubleA.get(segment[2]);
			if(segment.length > 3)
				try
				{
					int idx = Integer.parseInt(segment[3]);
					return ""+Utils.formatDouble(iD[idx], "##0.0##");
				} catch(Exception ex)
				{
					return "~ERROR2~";
				}
			else
			{
				StringBuilder result = new StringBuilder();
				for(int i = 0; i < iD.length; i++)
					result.append(i > 0?", ": "").append(Utils.formatDouble(iD[i], "##0.0##"));
				return result.toString();
			}
		}
		return "~ERROR3~";
	}
}