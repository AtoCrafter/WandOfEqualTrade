package ato.wandofequaltrade.item

import java.util.HashSet

import net.minecraft.block.Block
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.enchantment.{Enchantment, EnchantmentHelper}
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.item.{Item, ItemStack, ItemTool}
import net.minecraft.nbt.{NBTTagCompound, NBTTagList}
import net.minecraft.world.World
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent

import scala.collection.JavaConverters._

class ItemWandOfEqualTrade extends ItemTool(0, Item.ToolMaterial.EMERALD, new HashSet[Nothing]()) {
  setUnlocalizedName("WandOfEqualTrade")
  setCreativeTab(CreativeTabs.tabTools)
  setTextureName("WandOfEqualTrade:WandOfEqualTrade")


  override def onItemRightClick(is: ItemStack, world: World, player: EntityPlayer): ItemStack = {
    stopTrade(is)
    is
  }

  override def onItemUse(is: ItemStack, player: EntityPlayer, world: World,
                         x: Int, y: Int, z: Int, side: Int, fx: Float, fy: Float, fz: Float): Boolean = {
    stopTrade(is)
    if (player.isSneaking()) {
      startTrade(is, player, world, x, y, z, side)
    }
    true
  }

  override def onDroppedByPlayer(item: ItemStack, player: EntityPlayer): Boolean = {
    stopTrade(item)
    return true
  }

  override def onUpdate(itemstack: ItemStack, world: World, entity: Entity, p_77663_4_ : Int, p_77663_5_ : Boolean) {
    if (hasWandNBT(itemstack)) {
      val wandtc = getWandNBT(itemstack)
      if (world.provider.dimensionId == wandtc.getInteger("Dim") &&
        entity.isInstanceOf[EntityPlayer] &&
        entity.asInstanceOf[EntityPlayer].getCurrentEquippedItem() == itemstack &&
        getSrcItem(entity.asInstanceOf[EntityPlayer]) != null) {
        val side = wandtc.getInteger("Side")
        val candidates = wandtc.getTagList("Candidate", 10)
        val done = wandtc.getTagList("Done", 10)
        val id = wandtc.getInteger("Block")
        val meta = wandtc.getInteger("Meta")

        if (candidates.tagCount() > 0) {
          val ctc = candidates.getCompoundTagAt(0)
          val x = ctc.getInteger("X")
          val y = ctc.getInteger("Y")
          val z = ctc.getInteger("Z")
          candidates.removeTag(0)
          for (dir <- ForgeDirection.VALID_DIRECTIONS) {
            val nx = x + dir.offsetX
            val ny = y + dir.offsetY
            val nz = z + dir.offsetZ
            var reputation = false
            for (i <- List.range(0, done.tagCount())) {
              val other = done.getCompoundTagAt(i)
              if (nx == other.getInteger("X") &&
                ny == other.getInteger("Y") &&
                nz == other.getInteger("Z")) {
                reputation = true
              }
            }
            if (id == Block.getIdFromBlock(world.getBlock(nx, ny, nz)) &&
              meta == world.getBlockMetadata(nx, ny, nz) &&
              world.getTileEntity(nx, ny, nz) == null &&
              !reputation)
              addCandidate(itemstack, nx, ny, nz)
          }
          if (!trade(itemstack, entity.asInstanceOf[EntityPlayer], world, x, y, z, side)) {
            stopTrade(itemstack)
          }
        } else {
          stopTrade(itemstack)
        }
      } else {
        stopTrade(itemstack)
      }
    }
  }

  override def getItemEnchantability(): Int = 10

  private def startTrade(itemstack: ItemStack, player: EntityPlayer, world: World, x: Int, y: Int, z: Int, side: Int) {
    stopTrade(itemstack)

    val wandtc = getWandNBT(itemstack)
    wandtc.setInteger("Dim", world.provider.dimensionId)
    wandtc.setInteger("Side", side)
    wandtc.setInteger("Block", Block.getIdFromBlock(world.getBlock(x, y, z)))
    wandtc.setInteger("Meta", world.getBlockMetadata(x, y, z))

    addCandidate(itemstack, x, y, z)
  }

  private def stopTrade(itemstack: ItemStack) {
    removeWandNBT(itemstack)
  }

  private def addCandidate(itemstack: ItemStack, x: Int, y: Int, z: Int) {
    val wandtc = getWandNBT(itemstack)
    if (!wandtc.hasKey("Candidate")) wandtc.setTag("Candidate", new NBTTagList())
    val list = wandtc.getTagList("Candidate", 10)
    if (!wandtc.hasKey("Done")) wandtc.setTag("Done", new NBTTagList())
    val done = wandtc.getTagList("Done", 10)
    val cand = new NBTTagCompound()
    cand.setInteger("X", x)
    cand.setInteger("Y", y)
    cand.setInteger("Z", z)
    list.appendTag(cand)
    done.appendTag(cand)
  }

  private def trade(wand: ItemStack, player: EntityPlayer, world: World, x: Int, y: Int, z: Int, side: Int): Boolean = {
    if (!world.isRemote) {
      // CHECK
      if (!canTrade(world, x, y, z)) {
        return false
      }
      // GET
      for (is <- getDropItems(wand, player, world, x, y, z)) {
        if (!player.inventory.addItemStackToInventory(is.copy)) {
          player.entityDropItem(is.copy, 1)
        }
      }
      // CLEAR
      world.setBlock(x, y, z, Blocks.air, 0, 2)
      // SET
      val src = getSrcItem(player)
      if (src != null && src.tryPlaceItemIntoWorld(player, world, x, y, z, side, 0, 0, 0) &&
        world.getBlock(x, y, z) != Blocks.air) {
        if (src.stackSize <= 0) {
          MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(player, src))
          player.inventory.mainInventory((player.inventory.currentItem + 1) % 9) = null
        }
        return true
      }
    }
    false
  }

  private def canTrade(world: World, x: Int, y: Int, z: Int): Boolean = {
    return world.getBlock(x, y, z).getBlockHardness(world, x, y, z) >= 0
  }

  private def getDropItems(wand: ItemStack, player: EntityPlayer, world: World, x: Int, y: Int, z: Int): List[ItemStack] = {
    val block = world.getBlock(x, y, z)
    val meta = world.getBlockMetadata(x, y, z)
    if (block.canSilkHarvest(world, player, x, y, z, meta) &&
      EnchantmentHelper.getEnchantmentLevel(Enchantment.silkTouch.effectId, wand) > 0) {
      val item = Item.getItemFromBlock(block)
      val damage = if (item != null && item.getHasSubtypes) meta else 0
      val is = new ItemStack(item, 1, damage)
      return List(is)
    } else {
      val fortune = EnchantmentHelper.getEnchantmentLevel(Enchantment.fortune.effectId, wand)
      val list = block.getDrops(world, x, y, z, meta, fortune)
      return list.asScala.toList
    }
  }

  private def getSrcItem(player: EntityPlayer): ItemStack = {
    val i = (player.inventory.currentItem + 1) % 9
    return player.inventory.mainInventory(i)
  }

  private def hasWandNBT(is: ItemStack): Boolean = {
    val tc = is.getTagCompound()
    return tc != null && tc.hasKey("WandOfEqualTrade")
  }

  private def getWandNBT(is: ItemStack): NBTTagCompound = {
    if (!is.hasTagCompound()) is.setTagCompound(new NBTTagCompound())

    val tc = is.getTagCompound()
    if (!tc.hasKey("WandOfEqualTrade")) {
      val wandtc = new NBTTagCompound()
      tc.setTag("WandOfEqualTrade", wandtc)
    }
    return tc.getCompoundTag("WandOfEqualTrade")
  }

  private def removeWandNBT(is: ItemStack) {
    if (is.hasTagCompound()) {
      is.getTagCompound.removeTag("WandOfEqualTrade")
    }
  }
}
