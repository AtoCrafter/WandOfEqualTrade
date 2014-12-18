package ato.wandofequaltrade.client

import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.client.Minecraft
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.IItemRenderer
import net.minecraftforge.client.IItemRenderer.{ItemRenderType, ItemRendererHelper}
import org.lwjgl.opengl.GL11

@SideOnly(Side.CLIENT)
class RendererWand extends IItemRenderer {

  val model = new ModelWand()
  val mc = Minecraft.getMinecraft()

  var tick = 0

  override def handleRenderType(item: ItemStack, renderType: ItemRenderType): Boolean = true

  override def shouldUseRenderHelper(renderType: ItemRenderType, item: ItemStack, helper: ItemRendererHelper): Boolean = helper match {
    case ItemRendererHelper.ENTITY_BOBBING => true
    case ItemRendererHelper.ENTITY_ROTATION => true
    case ItemRendererHelper.INVENTORY_BLOCK => true
    case _ => false
  }

  override def renderItem(renderType: ItemRenderType, item: ItemStack, data: AnyRef*) {
    GL11.glPushMatrix()
    mc.renderEngine.bindTexture(new ResourceLocation("WandOfEqualTrade:items/WandOfEqualTrade.png"))
    renderType match {
      case ItemRenderType.EQUIPPED_FIRST_PERSON => {
        GL11.glTranslatef(0.8F, 0.9F, 0.1F)
        GL11.glRotatef(-5.0F, 0.0F, 1.0F, 0.0F)
        GL11.glRotatef(30.0F, 0.0F, 0.0F, 1.0F)
      }
      case ItemRenderType.EQUIPPED => {
        GL11.glTranslatef(0.5F, 0.6F, 0.0F)
        GL11.glRotatef(-5.0F, 0.0F, 1.0F, 0.0F)
        GL11.glRotatef(30.0F, 0.0F, 0.0F, 1.0F)
      }
      case ItemRenderType.INVENTORY => {
        GL11.glScalef(1.5f, 1.5f, 1.5f)
        GL11.glTranslatef(-0.5F, -0.5F, 0.0F)     // 枠内に収まるように枠の半分だけずらす
        GL11.glRotatef(-45.0F, 1.0F, 0.0F, 1.0F)  // INVENTORY_BLOCK ヘルパーにより既に傾いているので x,z 斜め 45 度回転
      }
      case _ => {}
    }
    tick += 1
    model.renderItem(1 / 16F, tick)
    GL11.glPopMatrix()
  }
}
