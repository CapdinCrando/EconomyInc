package fr.fifoube.gui;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;

import fr.fifoube.blocks.tileentity.TileEntityBlockSeller;
import fr.fifoube.gui.container.ContainerSeller;
import fr.fifoube.items.ItemCreditCard;
import fr.fifoube.main.ModEconomyInc;
import fr.fifoube.main.capabilities.CapabilityMoney;
import fr.fifoube.packets.PacketCardChangeSeller;
import fr.fifoube.packets.PacketSellerFundsTotal;
import fr.fifoube.packets.PacketsRegistery;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.Button.IPressable;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class GuiSellerBuy extends ContainerScreen<ContainerSeller>
{
	private TileEntityBlockSeller tile;
	
	public GuiSellerBuy(ContainerSeller container, PlayerInventory playerInventory, ITextComponent name) 
	{
		super(container, playerInventory, name);
		this.tile = container.getTile();
	}
	
	private static final ResourceLocation background = new ResourceLocation(ModEconomyInc.MOD_ID ,"textures/gui/screen/gui_item.png");
	protected int xSize = 256;
	protected int ySize = 124;
	protected int guiLeft;
	protected int guiTop;
	
	private Button slot1;
	private Button takeFunds;
	private String owner = "";
	private String itemName = "";
	private double cost;
	private int amount;
	private double fundsTotalRecovery;
	private int sizeInventoryCheckCard;
	
	@Override
	public void tick() 
	{
		super.tick();
		amount = tile.getAmount();
		fundsTotalRecovery = tile.getFundsTotal();	
		tile.setFundsTotal(fundsTotalRecovery);
		tile.setAmount(amount);
		tile.markDirty();
	}
	
	@Override
	protected void init() {
		
		this.guiLeft = (this.width - this.xSize) / 2;
	    this.guiTop = (this.height - this.ySize) / 2;
		if(tile != null)
		{
			this.owner = tile.getOwnerName();
			this.itemName = tile.getItem();
			this.cost = tile.getCost();
			this.slot1 = this.addButton(new Button(width / 2 - 50, height / 2 + 27, 100, 20, I18n.format("title.buy"), actionPerformed()));
             
			String sellerOwner = tile.getOwner();
			String worldPlayer = minecraft.player.getUniqueID().toString();
			if(sellerOwner.equals(worldPlayer))
			{
				this.takeFunds = this.addButton(new Button(width / 2 + 20, height / 2 - 74, 100, 13, I18n.format("title.recover"), actionPerformed()));
			}
			
		}
	}
	
	@Override
	public boolean isPauseScreen() {
		return false;
	}
	
	protected IPressable actionPerformed()
	{		
		minecraft.player.getCapability(CapabilityMoney.MONEY_CAPABILITY).ifPresent(data -> {
			System.out.println("data présente client side");
			if(tile != null) // WE CHECK IF TILE IS NOT NULL FOR AVOID CRASH
			{	
				if(buttons == slot1) //IF PLAYER BUY
				{
					if(data.getLinked())
					{
						if(data.getMoney() >= tile.getCost() && tile.getAmount() >= 1)
						{
							if(!tile.getAdmin())
							{
								double fundTotal = tile.getFundsTotal(); // WE GET THE TOTAL FUNDS
								tile.setFundsTotal(fundTotal + tile.getCost()); // CLIENT ADD TOTAL FUNDS + THE COST OF THE ITEM
								final int x = tile.getPos().getX(); // GET X COORDINATES
								final int y = tile.getPos().getY(); // GET Y COORDINATES
								final int z = tile.getPos().getZ(); // GET Z COORDINATES
								final double cost = tile.getCost(); // GET COST OF THE TILE ENTITY
								int amount = tile.getAmount(); // GET AMOUNT OF THE TILE ENTITY
								tile.setAmount(amount -1); // CLIENT SET AMOUNT MINUS ONE EACH TIME HE BUY
								PacketsRegistery.CHANNEL.sendToServer(new PacketSellerFundsTotal((fundTotal + tile.getCost()), x,y,z, amount, false)); //SENDING PACKET TO LET SERVER KNOW CHANGES WITH TOTAL FUNDS, COORDINATES AND AMOUNT
								PacketsRegistery.CHANNEL.sendToServer(new PacketCardChangeSeller(cost)); // SENDING ANOTHER PACKET TO UPDATE CLIENT'S CARD IN SERVER KNOWLEDGE
								tile.markDirty();
							}
							else
							{
								double fundTotal = tile.getFundsTotal(); // WE GET THE TOTAL FUNDS
								tile.setFundsTotal(fundTotal + tile.getCost()); // CLIENT ADD TOTAL FUNDS + THE COST OF THE ITEM
								final int x = tile.getPos().getX(); // GET X COORDINATES
								final int y = tile.getPos().getY(); // GET Y COORDINATES
								final int z = tile.getPos().getZ(); // GET Z COORDINATES
								final double cost = tile.getCost(); // GET COST OF THE TILE ENTITY
								int amount = tile.getAmount(); // GET AMOUNT OF THE TILE ENTITY
								tile.setAmount(amount); // CLIENT SET AMOUNT MINUS ONE EACH TIME HE BUY
								PacketsRegistery.CHANNEL.sendToServer(new PacketSellerFundsTotal((fundTotal + tile.getCost()), x,y,z, amount, false)); //SENDING PACKET TO LET SERVER KNOW CHANGES WITH TOTAL FUNDS, COORDINATES AND AMOUNT
								PacketsRegistery.CHANNEL.sendToServer(new PacketCardChangeSeller(cost)); // SENDING ANOTHER PACKET TO UPDATE CLIENT'S CARD IN SERVER KNOWLEDGE
								tile.markDirty();
							}
						}
						else
						{
							minecraft.player.sendMessage(new StringTextComponent(I18n.format("title.noEnoughFunds")));
						}
					}
					else
					{
						for(int i = 0; i < minecraft.player.inventory.getSizeInventory(); i++)
						{
							if(minecraft.player.inventory.getStackInSlot(i).getItem() instanceof ItemCreditCard)
							{
								ItemStack creditCard = minecraft.player.inventory.getStackInSlot(i);
								if(creditCard.hasTag())
								if(minecraft.player.getUniqueID().toString().equals(creditCard.getTag().getString("OwnerUUID")))
								{
									if(data.getMoney() >= tile.getCost())
									{
										if(tile.getAmount() >= 1)
										{
											boolean admin = tile.getAdmin();
											if(admin == false)
											{
												double fundTotal = tile.getFundsTotal(); // WE GET THE TOTAL FUNDS
												tile.setFundsTotal(fundTotal + tile.getCost()); // CLIENT ADD TOTAL FUNDS + THE COST OF THE ITEM
												final int x = tile.getPos().getX(); // GET X COORDINATES
												final int y = tile.getPos().getY(); // GET Y COORDINATES
												final int z = tile.getPos().getZ(); // GET Z COORDINATES
												final double cost = tile.getCost(); // GET COST OF THE TILE ENTITY
												int amount = tile.getAmount(); // GET AMOUNT OF THE TILE ENTITY
												tile.setAmount(amount -1); // CLIENT SET AMOUNT MINUS ONE EACH TIME HE BUY
												PacketsRegistery.CHANNEL.sendToServer(new PacketSellerFundsTotal((fundTotal + tile.getCost()), x,y,z, amount, false)); //SENDING PACKET TO LET SERVER KNOW CHANGES WITH TOTAL FUNDS, COORDINATES AND AMOUNT
												PacketsRegistery.CHANNEL.sendToServer(new PacketCardChangeSeller(cost)); // SENDING ANOTHER PACKET TO UPDATE CLIENT'S CARD IN SERVER KNOWLEDGE
												tile.markDirty();
											}
											else if(admin == true)
											{
												double fundTotal = tile.getFundsTotal(); // WE GET THE TOTAL FUNDS
												tile.setFundsTotal(fundTotal + tile.getCost()); // CLIENT ADD TOTAL FUNDS + THE COST OF THE ITEM
												final int x = tile.getPos().getX(); // GET X COORDINATES
												final int y = tile.getPos().getY(); // GET Y COORDINATES
												final int z = tile.getPos().getZ(); // GET Z COORDINATES
												final double cost = tile.getCost(); // GET COST OF THE TILE ENTITY
												int amount = tile.getAmount(); // GET AMOUNT OF THE TILE ENTITY
												tile.setAmount(amount); // CLIENT SET AMOUNT MINUS ONE EACH TIME HE BUY
												PacketsRegistery.CHANNEL.sendToServer(new PacketSellerFundsTotal((fundTotal + tile.getCost()), x,y,z, amount, false)); //SENDING PACKET TO LET SERVER KNOW CHANGES WITH TOTAL FUNDS, COORDINATES AND AMOUNT
												PacketsRegistery.CHANNEL.sendToServer(new PacketCardChangeSeller(cost)); // SENDING ANOTHER PACKET TO UPDATE CLIENT'S CARD IN SERVER KNOWLEDGE
												tile.markDirty();
											}
										}
									}
									else
									{
										minecraft.player.sendMessage(new StringTextComponent(I18n.format("title.noEnoughFunds")));
									}
								}
								else
								{
									minecraft.player.sendMessage(new StringTextComponent(I18n.format("title.noSameOwner")));
								}
							}
							else if(!(minecraft.player.inventory.getStackInSlot(i).getItem() instanceof ItemCreditCard))
							{
								this.sizeInventoryCheckCard = this.sizeInventoryCheckCard + 1;
								if(this.sizeInventoryCheckCard == minecraft.player.inventory.getSizeInventory())
								{
									if(!(tile.getAmount() == 0))
									{
										minecraft.player.sendMessage(new StringTextComponent(I18n.format("title.noCardFoundAndNoLink")));
									}
									else if(tile.getAmount() == 0)
									{
										minecraft.player.sendMessage(new StringTextComponent(I18n.format("title.noMoreQuantity")));
									}
									this.sizeInventoryCheckCard = 0;
								}
								if(i == minecraft.player.inventory.getSizeInventory())
								{
									this.sizeInventoryCheckCard = 0;
								}
							}
						}
					}	
				}
				else if(buttons == takeFunds)
				{
					final int x = tile.getPos().getX(); // GET X COORDINATES
					final int y = tile.getPos().getY(); // GET Y COORDINATES
					final int z = tile.getPos().getZ(); // GET Z COORDINATES
					tile.setFundsTotal(0);
					tile.markDirty();
					PacketsRegistery.CHANNEL.sendToServer(new PacketSellerFundsTotal(fundsTotalRecovery, x,y,z, amount, true)); //SENDING PACKET TO LET SERVER KNOW CHANGES WITH TOTAL FUNDS, COORDINATES AND AMOUNT
				}
						
			}
			
		});
		return null;
		
	}
	 
		@Override
		public void render(int mouseX, int mouseY, float partialTicks)
		{
			this.renderBackground();
			// added
	        this.getMinecraft().getTextureManager().bindTexture(background);
	        int i = this.guiLeft;
	        int j = this.guiTop;
	        this.blit(i, j, 0, 0, this.xSize, this.ySize);
			this.drawString(font, TextFormatting.BOLD + I18n.format("title.seller") + owner, (this.width / 2 - 120), (this.height / 2 - 50), 0x000);
			this.drawString(font, TextFormatting.BOLD + I18n.format("title.item") + itemName, (this.width / 2 - 120), (this.height / 2 - 40), 0x000);
			this.drawString(font, TextFormatting.BOLD + I18n.format("title.cost") + cost, (this.width / 2 - 120), (this.height / 2 - 30), 0x000);
			this.drawString(font, TextFormatting.BOLD + I18n.format("title.amount") + amount, (this.width / 2 - 120), (this.height / 2 - 20), 0x000);
			this.drawString(font, TextFormatting.BOLD + I18n.format("title.fundsToRecover") + fundsTotalRecovery, (this.width / 2 - 120), (this.height / 2 - 10), 0x000);
			super.render(mouseX, mouseY, partialTicks);
	        drawImageInGui();

	    }

		public void drawImageInGui() 
		{
	        int i = this.guiLeft;
	        int j = this.guiTop;
	        GL11.glPushMatrix();
			GlStateManager.enableRescaleNormal();
		    RenderHelper.enableGUIStandardItemLighting();
		    GL11.glScaled(2, 2, 2);
		    ItemStack stack = new ItemStack(Blocks.BARRIER,1);
		    if(!(tile.getAmount() == 0))
		    {
			    stack = new ItemStack(tile.getStackInSlot(0).getItem(), 1);
		    }
		    this.itemRenderer.renderItemIntoGUI(stack, (i / 2) + 105 , (j /2) + 5);
		    RenderHelper.disableStandardItemLighting();
		    GlStateManager.disableRescaleNormal();
		    GL11.glPopMatrix();   
		}

		@Override
		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
			// TODO Auto-generated method stub
			
		}

}