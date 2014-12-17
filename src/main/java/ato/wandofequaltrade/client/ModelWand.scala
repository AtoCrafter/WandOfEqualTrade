package ato.wandofequaltrade.client

import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.client.model.{ModelBase, ModelRenderer}

@SideOnly(Side.CLIENT)
class ModelWand extends ModelBase {

  setTextureOffset("wand.rod", 0, 0)
  setTextureOffset("wand.head", 0, 0)
  setTextureOffset("crystal.cube", 0, 0)

  val wand = new ModelRenderer(this, "wand")
  val crystal = new ModelRenderer(this, "crystal")

  wand.addBox("rod", -1.0F, -6.0F, -1.0F, 2, 18, 2)
  wand.addBox("head", -1.5F, 8.0F, -1.5F, 3, 3, 3)
  crystal.addBox("cube", -1.0F, -1.0F, -1.0F, 2, 2, 2)

  def renderItem(scale: Float, tick: Int) {
    wand.render(scale)
    crystal.offsetY = (15.0F + Math.sin(tick / 60.0F).asInstanceOf[Float]) * scale
    crystal.rotateAngleX = Math.sin(tick / 120.0F).asInstanceOf[Float]
    crystal.rotateAngleY = Math.sin(tick / 130.0F).asInstanceOf[Float]
    crystal.rotateAngleZ = Math.sin(tick / 140.0F).asInstanceOf[Float]
    crystal.render(scale)
  }

}
