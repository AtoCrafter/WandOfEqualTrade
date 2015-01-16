package ato.wandofequaltrade.proxy

import ato.wandofequaltrade.WandOfEqualTrade
import ato.wandofequaltrade.client.RendererWand
import net.minecraftforge.client.MinecraftForgeClient

class ProxyClient extends ProxyCommon {
  override def registerItemRenderer {
    MinecraftForgeClient.registerItemRenderer(WandOfEqualTrade.itemWandOfEqualTrade, new RendererWand())
  }
}
