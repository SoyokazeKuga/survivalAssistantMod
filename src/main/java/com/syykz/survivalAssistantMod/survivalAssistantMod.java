package com.syykz.survivalAssistantMod;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(survivalAssistantMod.MOD_ID)
public class survivalAssistantMod {
    public static final String MOD_ID = "survivalassistantmod";

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEventSubscriber {

        @SubscribeEvent
        public static void registerItems(final RegistryEvent.Register<Item> event) {
            event.getRegistry().registerAll(Items.SEARCHER);
        }
    }

    public static class Items {
        public static final Item SEARCHER = new Item(new Item.Properties()
                .group(ItemGroup.MATERIALS))
                .setRegistryName("searcher");
    }
}
