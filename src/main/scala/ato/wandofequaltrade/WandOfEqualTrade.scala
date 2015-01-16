package ato.wandofequaltrade

import ato.wandofequaltrade.item.ItemWandOfEqualTrade
import ato.wandofequaltrade.proxy.ProxyCommon
import cpw.mods.fml.common.Mod.EventHandler
import cpw.mods.fml.common.event.FMLPreInitializationEvent
import cpw.mods.fml.common.registry.GameRegistry
import cpw.mods.fml.common.{Mod, SidedProxy}
import net.minecraft.init.Items
import net.minecraft.item.ItemStack

@Mod(modid = "WandOfEqualTrade", modLanguage = "scala")
object WandOfEqualTrade {

  @SidedProxy(
    clientSide = "ato.wandofequaltrade.proxy.ProxyClient",
    serverSide = "ato.wandofequaltrade.proxy.ProxyCommon"
  )
  var proxy: ProxyCommon = _

  val itemWandOfEqualTrade = new ItemWandOfEqualTrade()

  @EventHandler
  def preInit(event: FMLPreInitializationEvent) {

    // register items
    GameRegistry.registerItem(itemWandOfEqualTrade, "WandOfEqualTrade")

    // register recipes
    GameRegistry.addRecipe(new ItemStack(itemWandOfEqualTrade),
      "  N",
      " S ",
      "S  ",
      new Character('N'), Items.nether_star,
      new Character('S'), Items.stick)

    // register other
    proxy.registerItemRenderer
  }
}
