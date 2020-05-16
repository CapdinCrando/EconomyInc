/*******************************************************************************
 *******************************************************************************/
package fr.fifoube.main.events.server;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import fr.fifoube.items.ItemsRegistery;
import fr.fifoube.main.ModEconomyInc;
import fr.fifoube.main.config.ConfigFile;
import fr.fifoube.world.saveddata.ChunksWorldSavedData;
import fr.fifoube.world.saveddata.PlotsChunkData;
import fr.fifoube.world.saveddata.PlotsData;
import fr.fifoube.world.saveddata.PlotsWorldSavedData;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber(modid = ModEconomyInc.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ServerEvents {

	private static final Random rand = new Random();
	
	@SubscribeEvent
	public void onSmelting(PlayerEvent.ItemSmeltedEvent event)
	{
		if(ConfigFile.goldNuggetRecipe)
		{
			if(!event.getSmelting().isEmpty() && event.getSmelting() != null)
			{
				ItemStack stack = event.getSmelting();
				if(stack.getItem().equals(ItemsRegistery.ITEM_GOLDNUGGET))
				{
					String valeur = Double.toString(rand.nextDouble()).substring(0, 4);
					stack.setTagInfo("weight", StringNBT.valueOf(valeur));
				}
			}
		}
	}
	
	@SubscribeEvent
    public void onPlacedBlock(EntityPlaceEvent event)
    {
		ServerPlayerEntity player = null;
		if(event.getEntity() instanceof ServerPlayerEntity)
		{
			player = (ServerPlayerEntity) event.getEntity();
		}
		if(player != null)
		{
	    	ServerWorld worldIn = player.getServerWorld();
	    	List<ChunkPos> listPos = new ArrayList<ChunkPos>();
	    	DimensionSavedDataManager storage = worldIn.getSavedData();
	    	ChunksWorldSavedData data = (ChunksWorldSavedData)storage.get(ChunksWorldSavedData::new, ChunksWorldSavedData.DATA_NAME);
	    	if(data != null)
	    	{
	    		List<PlotsChunkData> listC = data.getListContainer();
	    		for (int i = 0; i < listC.size(); i++) 
	    		{
	    			 PlotsChunkData plotsChunkData = listC.get(i);
					 for (int j = 0; j < plotsChunkData.getList().size(); j++) 
					 {			
							 String s = plotsChunkData.getList().get(j);
							 int xPos = Integer.valueOf(s.substring(s.indexOf("[") + 1, s.indexOf(",")));
							 int zPos = Integer.valueOf(s.substring(s.indexOf(",") + 2, s.indexOf("]")));
							 listPos.add(new ChunkPos(xPos, zPos));
					 }
				}
	    	}
	    	for(ChunkPos pos : listPos)
	    	{
	    		if(new ChunkPos(event.getPos()).equals(pos))
	    		{
	    			Vec3d vec = new Vec3d(event.getPos().getX(), event.getPos().getY(), event.getPos().getZ());
	    	    	List<AxisAlignedBB> listAABB = new ArrayList<AxisAlignedBB>();
	    	    	PlotsWorldSavedData plotsDataWSD = (PlotsWorldSavedData)storage.get(PlotsWorldSavedData::new, PlotsWorldSavedData.DATA_NAME);
	    	    	UUID uuidOwner = null;
	    	    	if(plotsDataWSD != null)
	    	    	{
	    	    		List<PlotsData> listC = plotsDataWSD.getListContainer();
	    	    		for (int i = 0; i < listC.size(); i++) 
	    	    		{
							PlotsData plotsData = plotsDataWSD.getListContainer().get(i);
							for (int j = 0; j < plotsData.getList().size(); j++) 
							{
								uuidOwner = UUID.fromString(plotsData.getList().get(1));
								int xPosFirst = Integer.valueOf(plotsData.getList().get(2));
								int zPosFirst = Integer.valueOf(plotsData.getList().get(3));
								int xPosSecond = Integer.valueOf(plotsData.getList().get(4));
								int zPosSecond = Integer.valueOf(plotsData.getList().get(5));
								listAABB.add(new AxisAlignedBB(xPosFirst, 0, zPosFirst, xPosSecond, 255, zPosSecond).grow(2, 1, 2));
							}
						}
	    	    	}
	    	    	for(AxisAlignedBB checker : listAABB)
	    	    	{
	    	    		if(checker.contains(vec))
	    	    		{
	    	    			if(uuidOwner != null)
	    	    			if(!player.getUniqueID().equals(uuidOwner))
	    	    			{
	    	    				if(player.hasPermissionLevel(4))
	    	    				{
	    	    					event.setCanceled(false);
	    	    				}
	    	    				else
	    	    				{
	    	    					event.setCanceled(true);
	    	    				}
	    	    			}
	    	    		}
	    	    	}
	    		}
	    	}
		}
    }
    
    @SubscribeEvent
    public void onBreakBlock(BreakEvent event)
    { 	
    	ServerPlayerEntity player = null;
		if(event.getPlayer() instanceof ServerPlayerEntity)
		{
			player = (ServerPlayerEntity) event.getPlayer();
		}
		if(player != null)
		{
	    	ServerWorld worldIn = player.getServerWorld();
	    	List<ChunkPos> listPos = new ArrayList<ChunkPos>();
	    	DimensionSavedDataManager storage = worldIn.getSavedData();
	    	ChunksWorldSavedData data = (ChunksWorldSavedData)storage.get(ChunksWorldSavedData::new, ChunksWorldSavedData.DATA_NAME);
	    	if(data != null)
	    	{
	    		List<PlotsChunkData> listC = data.getListContainer();
	    		for (int i = 0; i < listC.size(); i++) 
	    		{
	    			 PlotsChunkData plotsChunkData = listC.get(i);
					 for (int j = 0; j < plotsChunkData.getList().size(); j++) 
					 {			
							 String s = plotsChunkData.getList().get(j);
							 int xPos = Integer.valueOf(s.substring(s.indexOf("[") + 1, s.indexOf(",")));
							 int zPos = Integer.valueOf(s.substring(s.indexOf(",") + 2, s.indexOf("]")));
							 listPos.add(new ChunkPos(xPos, zPos));
					 }
				}
	    	}
	    	for(ChunkPos pos : listPos)
	    	{
	    		if(new ChunkPos(event.getPos()).equals(pos))
	    		{
	    			Vec3d vec = new Vec3d(event.getPos().getX(), event.getPos().getY(), event.getPos().getZ());
	    	    	List<AxisAlignedBB> listAABB = new ArrayList<AxisAlignedBB>();
	    	    	PlotsWorldSavedData plotsDataWSD = (PlotsWorldSavedData)storage.get(PlotsWorldSavedData::new, PlotsWorldSavedData.DATA_NAME);
	    	    	UUID uuidOwner = null;
	    	    	if(plotsDataWSD != null)
	    	    	{
	    	    		List<PlotsData> listC = plotsDataWSD.getListContainer();
	    	    		for (int i = 0; i < listC.size(); i++) 
	    	    		{
							PlotsData plotsData = plotsDataWSD.getListContainer().get(i);
							for (int j = 0; j < plotsData.getList().size(); j++) 
							{
								uuidOwner = UUID.fromString(plotsData.getList().get(1));
								int xPosFirst = Integer.valueOf(plotsData.getList().get(2));
								int zPosFirst = Integer.valueOf(plotsData.getList().get(3));
								int xPosSecond = Integer.valueOf(plotsData.getList().get(4));
								int zPosSecond = Integer.valueOf(plotsData.getList().get(5));
								listAABB.add(new AxisAlignedBB(xPosFirst, 0, zPosFirst, xPosSecond, 255, zPosSecond).grow(2, 1, 2));
							}
						}
	    	    	}
	    	    	for(AxisAlignedBB checker : listAABB)
	    	    	{
	    	    		if(checker.contains(vec))
	    	    		{
	    	    			if(uuidOwner != null)
	    	    			if(!event.getPlayer().getUniqueID().equals(uuidOwner))
	    	    			{
	    	    				if(player.hasPermissionLevel(4))
	    	    				{
	    	    					event.setCanceled(false);
	    	    				}
	    	    				else
	    	    				{
	    	    					event.setCanceled(true);
	    	    				}
	    	    			}

	    	    		}
	    	    	}
	    		}
	    	}
	    }
    }
    

}
