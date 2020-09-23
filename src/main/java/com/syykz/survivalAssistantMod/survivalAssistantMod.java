package com.syykz.survivalAssistantMod;

import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Mod(survivalAssistantMod.MOD_ID)
public class survivalAssistantMod {
    public static final String MOD_ID = "survivalassistantmod";

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEventSubscriber {

        // Itemsの登録
        @SubscribeEvent
        public static void registerItems(final RegistryEvent.Register<Item> event) {
            event.getRegistry().registerAll(Items.SEARCHER);
        }
    }

    // 追加するアイテムの定義クラス
    public static class Items {
        public static final SearchItem SEARCHER = (SearchItem) new SearchItem(new Item.Properties()
                .group(ItemGroup.MATERIALS))
                .setRegistryName("searcher");
    }

    /**
     * Called to trigger the item's "innate" right click behavior. To handle when this item is used on a Block, see
     * {@link #onItemUse}.
     */
    public static class SearchItem extends Item {

        SearchItem(Item.Properties properties) {
            super(properties);
        }

        // 右クリック時の挙動をオーバーライドする。
        @Override
        public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
            ItemStack itemstack = playerIn.getHeldItem(handIn);

            // クライアント側のみで実行する分岐。(本来、以下の処理はクライアント側とサーバー側で１回ずつ呼ばれる)
            if (!worldIn.isRemote) return new ActionResult<>(ActionResultType.SUCCESS, itemstack);

            playerIn.setActiveHand(handIn);

            // TODO: 二重for文の解消。chunk取得とMonsterMob取得の処理を分ける。
            // TODO: 命名の修正。mobMapなど。
            /**
             * 自身の周囲９ChunkのMonsterEntity取得
             * Chunkは、ワールドを、256ブロックの高さの16 × 16に区切った領域。
             * ただし、world.getChunk[1][1]では (-15 <= x <= 15), (-15 <= y <= 15)の範囲が取得される。(原因不明)
             */
            HashMap<String, Integer> monsterMap = new HashMap<String, Integer>();
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    Chunk chunk = worldIn.getChunk((int) (playerIn.posX / 16) + i - 1, (int) (playerIn.posZ / 16) + j - 1);

                    MonsterEntity[] entities = this.getMonsterEntityInChunk(chunk);
                    for (MonsterEntity entity : entities) {
                        this.countMob(monsterMap, entity);
                    }
                }
            }

            int totalMobs = monsterMap.values().stream().mapToInt(Integer::intValue).sum();
            String statusMessage = "周囲に" + totalMobs + "体います";
            playerIn.sendStatusMessage(new StringTextComponent(statusMessage), true);

            for (HashMap.Entry<String, Integer> entry : monsterMap.entrySet()) {
                playerIn.sendMessage(new StringTextComponent(entry.getKey() + ": " + entry.getValue() + "体"));
            }

            return new ActionResult<>(ActionResultType.SUCCESS, itemstack);
        }

        private MonsterEntity[] getMonsterEntityInChunk(Chunk chunk) {
            ClassInheritanceMultiMap<Entity>[] entityLists = chunk.getEntityLists();

            List<MonsterEntity> monsterEntities = new ArrayList<MonsterEntity>();
            for (int e = 0; e < entityLists.length; e++) {
                for (Entity entity : entityLists[e]) {
                    // TODO: フィルターの方法の再考
                    if (entity instanceof MonsterEntity) {
                        monsterEntities.add((MonsterEntity) entity);
                    }
                }
            }

            return monsterEntities.toArray(new MonsterEntity[monsterEntities.size()]);
        }

        private void countMob(HashMap<String, Integer> mobMap, Entity entity) {
            if (!mobMap.containsKey(entity.getEntityString())) {
                mobMap.put(entity.getEntityString(), 0);
            }
            mobMap.put(entity.getEntityString(), mobMap.get(entity.getEntityString()) + 1);
        }
    }
}

