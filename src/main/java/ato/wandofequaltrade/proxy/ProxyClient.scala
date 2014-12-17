package ato.wandofequaltrade.proxy

import net.minecraft.item.Item
import net.minecraftforge.client.{IItemRenderer, MinecraftForgeClient}

class ProxyClient extends ProxyCommon {
  override def registerItemRenderer(item: Item, renderer: IItemRenderer) {
    MinecraftForgeClient.registerItemRenderer(item, renderer)
  }
}
