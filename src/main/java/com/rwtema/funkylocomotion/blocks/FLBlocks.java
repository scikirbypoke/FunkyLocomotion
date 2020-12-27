package com.rwtema.funkylocomotion.blocks;

import net.minecraft.block.Block;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nonnull;

@Mod.EventBusSubscriber
public class FLBlocks
{
	@GameRegistry.ObjectHolder("funkylocomotion:pusher")
	public static final BlockPusher PUSHER = null;

	@GameRegistry.ObjectHolder("funkylocomotion:moving")
	public static final BlockMoving MOVING = null;

	@GameRegistry.ObjectHolder("funkylocomotion:slider")
	public static final BlockSlider SLIDER = null;

	@GameRegistry.ObjectHolder("funkylocomotion:booster")
	public static final BlockBooster BOOSTER = null;

	@GameRegistry.ObjectHolder("funkylocomotion:teleporter")
	public static final BlockTeleport TELEPORTER = null;

	@GameRegistry.ObjectHolder("funkylocomotion:frame_projector")
	public static final BlockFrameProjector FRAME_PROJECTOR = null;

	@GameRegistry.ObjectHolder("funkylocomotion:mass_frame_corner")
	public static final BlockMassFrameCorner MASS_FRAME_CORNER = null;

	@GameRegistry.ObjectHolder("funkylocomotion:mass_frame_edge")
	public static final BlockMassFrameEdge MASS_FRAME_EDGE = null;



	public static final BlockStickyFrame[] FRAMES = new BlockStickyFrame[4];

	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event) {
		IForgeRegistry<Block> registry = event.getRegistry();

		registry.register(new BlockBooster());
		registry.register(new BlockFrameProjector());
		registry.register(new BlockMoving());
		registry.register((new BlockPusher()).setTranslationKey("funkylocomotion:pusher").setRegistryName("funkylocomotion:pusher"));
		registry.register(new BlockSlider());
		registry.register(new BlockTeleport());
		registry.register(new BlockMassFrameEdge());
		registry.register(new BlockMassFrameCorner());

		for (int i = 0; i < 4; i++) {
			BlockStickyFrame.curLoadingIndex = i;
			registry.register(FRAMES[i] = new BlockStickyFrame());
		}

		GameRegistry.registerTileEntity(TileBooster.class,			  "funkylocomotion:tile_booster");
		GameRegistry.registerTileEntity(TileFrameProjector.class,	  "funkylocomotion:tile_frame_projector");
		GameRegistry.registerTileEntity(TileMovingClient.class,		  "funkylocomotion:tile_mover_client");
		GameRegistry.registerTileEntity(TileMovingServer.class,		  "funkylocomotion:tile_mover");
		GameRegistry.registerTileEntity(TilePusher.class,			  "funkylocomotion:tile_pusher");
		GameRegistry.registerTileEntity(TileSlider.class,			  "funkylocomotion:tile_slider");
		GameRegistry.registerTileEntity(TileTeleport.class,			  "funkylocomotion:tile_teleporter");
		GameRegistry.registerTileEntity(TileMassFrame.class,		  "funkylocomotion:tile_mass_frame");
		GameRegistry.registerTileEntity(TileMassFrameController.class,"funkylocomotion:tile_mass_frame_controller");
	}
}
