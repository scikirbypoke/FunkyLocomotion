package com.rwtema.funkylocomotion.items;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.rwtema.funkylocomotion.FunkyLocomotion;
import com.rwtema.funkylocomotion.blocks.BlockStickyFrame;
import com.rwtema.funkylocomotion.movers.IMover;
import com.rwtema.funkylocomotion.movers.MoverEventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemWrench extends Item {
	public static final int metaWrenchNormal = 0;
	public static final int metaWrenchEye = 1;
	public static final int metaWrenchHammer = 2;
	static EnumFacing[] offsetDir1 = {EnumFacing.NORTH, EnumFacing.NORTH, EnumFacing.WEST, EnumFacing.WEST, EnumFacing.NORTH, EnumFacing.NORTH};
	static EnumFacing[] offsetDir2 = {EnumFacing.WEST, EnumFacing.WEST, EnumFacing.UP, EnumFacing.UP, EnumFacing.UP, EnumFacing.UP};

	public ItemWrench() {
		super();
		this.setMaxStackSize(1);
		this.setRegistryName("funkylocomotion:wrench");
		this.setTranslationKey("funkylocomotion:wrench");
		this.setCreativeTab(FunkyLocomotion.creativeTabFrames);
		this.setHasSubtypes(true);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list) {
		if (this.isInCreativeTab(tab)) {
			list.add(new ItemStack(this, 1, 0));
			list.add(new ItemStack(this, 1, 1));
			list.add(new ItemStack(this, 1, 2));
		}
	}

	@Override
	public boolean doesSneakBypassUse(ItemStack stack, IBlockAccess world, BlockPos pos, EntityPlayer player) {
		return true;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(ItemStack stack, @Nullable World world, @Nonnull List<String> list, ITooltipFlag advanced) {
		if (stack.getItemDamage() == metaWrenchEye)
			list.add(I18n.format("tooltip.funkylocomotion:wrench_eye"));
		else if (stack.getItemDamage() == metaWrenchHammer)
			list.add(I18n.format("tooltip.funkylocomotion:wrench_hammer"));
	}

	@Nonnull
	@Override
	public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);
		if (stack.getItemDamage() != metaWrenchHammer)
			return EnumActionResult.PASS;

		if (world.isRemote) {
			FunkyLocomotion.proxy.sendUsePacket(pos, side, hand, hitX, hitY, hitZ);
			return EnumActionResult.SUCCESS;
		}


		EnumFacing d1 = offsetDir1[side.ordinal()];
		EnumFacing d2 = offsetDir2[side.ordinal()];

		IBlockState state = world.getBlockState(pos);
		Block block = state.getBlock();

		if (block.isAir(state, world, pos))
			return EnumActionResult.PASS;

		if (block instanceof BlockStickyFrame) {
			state = state.getActualState(world, pos);
			boolean isOpen = state.getValue(BlockStickyFrame.DIR_OPEN_MAP.get(side));

			Block otherBlock;

			for (BlockPos.MutableBlockPos otherPos : BlockPos.getAllInBoxMutable(pos.offset(d1, -2).offset(d2, -2), pos.offset(d1, 2).offset(d2, 2))) {
				if (otherPos.equals(pos)) {
					block.onBlockActivated(world, pos, state, player, hand, side, hitX, hitY, hitZ);
				} else {
					IBlockState otherBlockState = world.getBlockState(otherPos);
					otherBlock = otherBlockState.getBlock();
					if (otherBlock instanceof BlockStickyFrame && otherBlockState.getValue(BlockStickyFrame.DIR_OPEN_MAP.get(side)) == isOpen) {
						BlockPos blockingPos = otherPos.offset(side);
						if (isInaccessible(world, side, hitX, hitY, hitZ, blockingPos)) continue;


						otherBlock.onBlockActivated(world, otherPos, otherBlockState, player, hand, side, hitX, hitY, hitZ);
					}
				}
			}
		} else {

			for (BlockPos.MutableBlockPos otherPos : BlockPos.getAllInBoxMutable(pos.offset(d1, -1).offset(d2, -1), pos.offset(d1).offset(d2))) {
				IBlockState otherBlockState = world.getBlockState(otherPos);
				Block otherBlock = otherBlockState.getBlock();
				if (otherBlockState != state) continue;
				if (otherBlock.isAir(otherBlockState, world, otherPos))
					continue;

				BlockPos blockingPos = otherPos.offset(side.getOpposite());
				if (!pos.equals(otherPos) && isInaccessible(world, side, hitX, hitY, hitZ, blockingPos)) continue;

				otherBlock.onBlockActivated(world, otherPos, otherBlockState, player, hand, side, hitX, hitY, hitZ);
			}

		}
		return EnumActionResult.SUCCESS;
	}

	private boolean isInaccessible(World world, EnumFacing side, float hitX, float hitY, float hitZ, BlockPos blockingPos) {
		IBlockState blockingState = world.getBlockState(blockingPos);
		Block blockingBlock = blockingState.getBlock();
		if (!blockingBlock.isAir(blockingState, world, blockingPos)) {
			if (blockingState.getCollisionBoundingBox(world, blockingPos) != null && blockingBlock.canCollideCheck(blockingState, false)) {
				int face = side.ordinal();
				if (blockingState.collisionRayTrace(world, blockingPos,
						new Vec3d(
								blockingPos.getX() + (face == 4 ? -0.1 : face == 5 ? 1.1 : hitX),
								blockingPos.getY() + (face == 0 ? -0.1 : face == 1 ? 1.1 : hitY),
								blockingPos.getZ() + (face == 2 ? -0.1 : face == 3 ? 1.1 : hitZ)
						),
						new Vec3d(
								blockingPos.getX() + (face == 4 ? 1.1 : face == 5 ? -0.1 : hitX),
								blockingPos.getY() + (face == 0 ? 1.1 : face == 1 ? -0.1 : hitY),
								blockingPos.getZ() + (face == 2 ? 1.1 : face == 3 ? -0.1 : hitZ)
						)
				) != null) {
					return true;
				}
			}
		}
		return false;
	}


	@Override
	public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, EntityPlayer player) {
		if (!player.getEntityWorld().isRemote) {
			TileEntity tileEntity = player.getEntityWorld().getTileEntity(pos);
			if (tileEntity instanceof IMover) {
				MoverEventHandler.registerMover((IMover) tileEntity);
			}
		}
		return true;
	}

	@Nonnull
	@Override
	public String getTranslationKey(ItemStack itemstack) {
		if (itemstack.getItemDamage() == metaWrenchEye)
			return "item.funkylocomotion:wrench_eye";
		else if (itemstack.getItemDamage() == metaWrenchHammer)
			return "item.funkylocomotion:wrench_hammer";
		else
			return super.getTranslationKey(itemstack);
	}

	@SubscribeEvent
	public void leftClick(PlayerInteractEvent.LeftClickBlock event) {
		ItemStack heldItem = event.getEntityPlayer().getHeldItem(event.getHand());
		if (heldItem.isEmpty() || heldItem.getItem() != this)
			return;
		if (!event.getWorld().isRemote) {
			TileEntity tileEntity = event.getWorld().getTileEntity(event.getPos());
			if (tileEntity instanceof IMover) {
				MoverEventHandler.registerMover((IMover) tileEntity);
			}
		}
		event.setCanceled(true);
	}
}
