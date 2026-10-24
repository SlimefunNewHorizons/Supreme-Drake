package com.github.relativobr.supreme.machine.tech;

import com.github.relativobr.supreme.generic.machine.SimpleItemContainerMachine;
import com.github.relativobr.supreme.generic.recipe.AbstractItemRecipe;
import com.github.relativobr.supreme.generic.recipe.InventoryRecipe;
import com.github.relativobr.supreme.resource.SupremeComponents;
import com.github.relativobr.supreme.resource.magical.SupremeAttribute;
import com.github.relativobr.supreme.resource.magical.SupremeCetrus;
import com.github.relativobr.supreme.resource.magical.SupremeCore;
import com.github.relativobr.supreme.util.ItemGroups;
import com.github.relativobr.supreme.util.SupremeItemStack;
import com.github.relativobr.supreme.util.UtilEnergy;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.MachineTier;
import io.github.thebusybiscuit.slimefun4.core.attributes.MachineType;
import io.github.thebusybiscuit.slimefun4.core.attributes.Radioactive;
import io.github.thebusybiscuit.slimefun4.core.attributes.Radioactivity;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import io.github.thebusybiscuit.slimefun4.utils.LoreBuilder;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.springframework.scheduling.annotation.Async;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Async
public class TechRobotic extends SimpleItemContainerMachine implements Radioactive {

  private static final String PROCESSING_ITEM_KEY = "supreme-tech-robotic-processing-item";
  private static final String PROCESSING_TIME_KEY = "supreme-tech-robotic-processing-time";

  public static final SlimefunItemStack TECH_ROBOTIC = new SupremeItemStack("SUPREME_TECH_ROBOTIC",
      Material.POLISHED_BLACKSTONE, "&bTech Robotic", "", "&fUse beginner level robots ",
      "&fto progress to higher levels", "&fneed 64x to upgrade", "", LoreBuilder.radioactive(Radioactivity.VERY_HIGH), "",
      LoreBuilder.machine(MachineTier.END_GAME, MachineType.MACHINE), UtilEnergy.energyPowerPerSecond(500), "",
      "&3Supreme Machine");
  public static final ItemStack[] RECIPE_TECH_ROBOTIC = {SupremeComponents.INDUCTIVE_MACHINE,
      SupremeComponents.SYNTHETIC_RUBY, SupremeComponents.INDUCTIVE_MACHINE, SlimefunItems.REINFORCED_PLATE,
      SlimefunItems.ELECTRIC_MOTOR, SlimefunItems.REINFORCED_PLATE, SupremeComponents.RUSTLESS_MACHINE,
      SupremeCore.CORE_OF_BLOCK, SupremeComponents.RUSTLESS_MACHINE};

  public static final SlimefunItemStack TECH_ROBOTIC_II = new SupremeItemStack("SUPREME_TECH_ROBOTIC_II",
      Material.POLISHED_BLACKSTONE, "&bTech Robotic II", "", "&fUse beginner level robots ",
      "&fto progress to higher levels", "&fneed 32x to upgrade", "", LoreBuilder.radioactive(Radioactivity.VERY_HIGH), "",
      LoreBuilder.machine(MachineTier.END_GAME, MachineType.MACHINE), UtilEnergy.energyPowerPerSecond(500), "",
      "&3Supreme Machine");
  public static final ItemStack[] RECIPE_TECH_ROBOTIC_II = new ItemStack[]{SupremeComponents.CONVEYANCE_MACHINE,
      SupremeCetrus.CETRUS_LUMIUM, SupremeComponents.CONVEYANCE_MACHINE, SupremeComponents.INDUCTOR_MACHINE,
      TechRobotic.TECH_ROBOTIC, SupremeComponents.INDUCTOR_MACHINE, SupremeComponents.THORNERITE,
      SupremeCetrus.CETRUS_IGNIS, SupremeComponents.THORNERITE};

  public static final SlimefunItemStack TECH_ROBOTIC_III = new SupremeItemStack("SUPREME_TECH_ROBOTIC_III",
      Material.POLISHED_BLACKSTONE, "&bTech Robotic III", "", "&fUse beginner level robots ",
      "&fto progress to higher levels", "&fneed 16x to upgrade", "", LoreBuilder.radioactive(Radioactivity.VERY_HIGH), "",
      LoreBuilder.machine(MachineTier.END_GAME, MachineType.MACHINE), UtilEnergy.energyPowerPerSecond(500), "",
      "&3Supreme Machine");
  public static final ItemStack[] RECIPE_TECH_ROBOTIC_III = new ItemStack[]{SupremeComponents.THORNERITE,
      SupremeAttribute.getImpetus(), SupremeComponents.THORNERITE, SupremeComponents.SUPREME,
      TechRobotic.TECH_ROBOTIC_II, SupremeComponents.SUPREME, SupremeComponents.CRYSTALLIZER_MACHINE,
      SupremeCetrus.CETRUS_LUMIUM, SupremeComponents.CRYSTALLIZER_MACHINE};

  public static final List<AbstractItemRecipe> recipes = new ArrayList<>();
  private Map<Block, ItemStack> processing = new HashMap<>();
  private Map<Block, Integer> progressTime = new HashMap<>();
  private int speed = 1;
  private int amountUpgrade = 64;

  public TechRobotic(SlimefunItemStack item, ItemStack[] recipe) {
    super(ItemGroups.MACHINES_CATEGORY, item, RecipeType.ENHANCED_CRAFTING_TABLE, recipe);
  }

  public static void addRecipe(ItemStack input, ItemStack output) {
    recipes.add(new AbstractItemRecipe(input, output));
  }

  private static void invalidProgressBar(BlockMenu menu, String txt) {
    for (int i : InventoryRecipe.TECH_ROBOTIC_PROGRESS_BAR_SLOT) {
      menu.replaceExistingItem(i, new CustomItemStack(Material.RED_STAINED_GLASS_PANE, txt));
    }
  }

  private static void invalidProgressBar(BlockMenu menu, Material material, String txt) {
    for (int i : InventoryRecipe.TECH_ROBOTIC_PROGRESS_BAR_SLOT) {
      menu.replaceExistingItem(i, new CustomItemStack(material, txt));
    }
  }

  @Override
  public int[] getInputSlots() {
    return InventoryRecipe.TECH_ROBOTIC_INPUT_SLOTS;
  }

  @Override
  public int[] getOutputSlots() {
    return InventoryRecipe.TECH_ROBOTIC_OUTPUT_SLOTS;
  }

  @Override
  protected void constructMenu(BlockMenuPreset preset) {

    for (int i : InventoryRecipe.TECH_ROBOTIC_BORDER) {
      preset.addItem(i, new CustomItemStack(Material.GRAY_STAINED_GLASS_PANE, " ", new String[0]),
          ChestMenuUtils.getEmptyClickHandler());
    }

    for (int i : InventoryRecipe.TECH_ROBOTIC_BORDER_IN) {
      preset.addItem(i, new CustomItemStack(Material.BLUE_STAINED_GLASS_PANE, " ", new String[0]),
          ChestMenuUtils.getEmptyClickHandler());
    }

    for (int i : InventoryRecipe.TECH_ROBOTIC_BORDER_OUT) {
      preset.addItem(i, new CustomItemStack(Material.ORANGE_STAINED_GLASS_PANE, " ", new String[0]),
          ChestMenuUtils.getEmptyClickHandler());
    }

    for (int i : InventoryRecipe.TECH_ROBOTIC_PROGRESS_BAR_SLOT) {
      preset.addItem(i, new CustomItemStack(Material.BLACK_STAINED_GLASS_PANE, " ", new String[0]),
          ChestMenuUtils.getEmptyClickHandler());
    }

    for (int i : InventoryRecipe.TECH_ROBOTIC_OUTPUT_SLOTS) {
      preset.addMenuClickHandler(i, new ChestMenu.AdvancedMenuClickHandler() {
        @Override
        public boolean onClick(Player p, int slot, ItemStack cursor, ClickAction action) {
          return false;
        }

        @Override
        public boolean onClick(InventoryClickEvent e, Player p, int slot, ItemStack cursor, ClickAction action) {
          if (cursor == null) {
            return true;
          }
          return cursor.getType() == Material.AIR;
        }
      });
    }

  }

  @Override
  public void preRegister() {
    this.addItemHandler(new BlockTicker() {
      @Override
      public void tick(Block b, SlimefunItem sf, Config data) {
        TechRobotic.this.tick(b);
      }

      public boolean isSynchronized() {
        return true;
      }
    });
  }

  public void tick(Block b) {

    BlockMenu inv = BlockStorage.getInventory(b);

    final ItemStack itemProcess = getProcessingItem(b);
    if (itemProcess == null) {

      final ItemStack itemOutput = validRecipeItem(inv);
      if (itemOutput != null) {

        invalidProgressBar(inv, itemOutput.getType(), " ");

        setProcessingItem(b, itemOutput);
        setProgressTime(b, getTimeProcess() * 2);

      } else {

        invalidProgressBar(inv, "&cTechRobotic unidentified recipe");

      }

    } else {

      if (this.getProgressTime(b) <= 0) {
        if (notHasSpaceOutput(inv, new ItemStack[]{itemProcess})) {
          invalidProgressBar(inv, "&cOutput full");
          return;
        }

        inv.pushItem(itemProcess.clone(), this.getOutputSlots());

        clearProcessing(b);
        invalidProgressBar(inv, Material.BLACK_STAINED_GLASS_PANE, " ");

      } else {

        this.processTicks(b, inv, itemProcess);

      }

    }

  }

  public int getProgressTime(Block b) {
    Integer stored = progressTime.get(b);
    if (stored != null) {
      return stored;
    }

    String persisted = BlockStorage.getLocationInfo(b.getLocation(), PROCESSING_TIME_KEY);
    if (persisted != null) {
      try {
        stored = Math.max(0, Integer.parseInt(persisted));
        progressTime.put(b, stored);
        return stored;
      } catch (NumberFormatException ignored) {
        // A corrupt progress value only restarts this operation's timer; the output remains stored.
      }
    }

    return getTimeProcess() * 2;
  }

  @Override
  protected void onMachineRemoved(Block b) {
    processing.remove(b);
    progressTime.remove(b);
  }

  @Override
  protected void recoverLegacyProcessingOutput(Block b, BlockMenu inv) {
    ItemStack pendingOutput = getProcessingItem(b);
    if (pendingOutput != null) {
      b.getWorld().dropItemNaturally(b.getLocation(), pendingOutput.clone());
    }
  }

  private void processTicks(Block b, BlockMenu inv, ItemStack result) {
    int ticksTotal = getTimeProcess() * 2;
    int ticksLeft = this.getProgressTime(b);
    if (ticksLeft > 0) {

      if (this.takeCharge(b.getLocation())) {

        int time = ticksLeft - this.getSpeed();
        if (time < 0) {
          time = 0;
        }
        progressTime.put(b, time);
        if (time == 0 || time % 20 == 0) {
          BlockStorage.addBlockInfo(b, PROCESSING_TIME_KEY, Integer.toString(time));
        }

        //todo adjust for partial progression in the 3 slots dynamically
        for (int i : InventoryRecipe.TECH_ROBOTIC_PROGRESS_BAR_SLOT) {
          ChestMenuUtils.updateProgressbar(inv, i, Math.round(ticksLeft / this.getSpeed()),
              Math.round(ticksTotal / this.getSpeed()), result);
        }
      } else {
        invalidProgressBar(inv, "&cNo power to machine");
      }
    } else {
      invalidProgressBar(inv, "&cMachine time failure");
    }
  }

  private ItemStack getProcessingItem(Block block) {
    ItemStack cached = processing.get(block);
    if (cached != null) {
      return cached;
    }

    String serialized = BlockStorage.getLocationInfo(block.getLocation(), PROCESSING_ITEM_KEY);
    if (serialized == null) {
      return null;
    }

    try {
      YamlConfiguration config = new YamlConfiguration();
      config.loadFromString(serialized);
      ItemStack item = config.getItemStack("item");
      if (item != null && !item.getType().isAir() && item.getAmount() > 0) {
        processing.put(block, item);
        return item;
      }
    } catch (InvalidConfigurationException ignored) {
      // Keep the serialized value untouched: an administrator can recover it from BlockStorage.
    }

    return null;
  }

  private void setProcessingItem(Block block, ItemStack item) {
    processing.put(block, item);
    YamlConfiguration config = new YamlConfiguration();
    config.set("item", item);
    BlockStorage.addBlockInfo(block, PROCESSING_ITEM_KEY, config.saveToString());
  }

  private void setProgressTime(Block block, int time) {
    progressTime.put(block, time);
    BlockStorage.addBlockInfo(block, PROCESSING_TIME_KEY, Integer.toString(time));
  }

  private void clearProcessing(Block block) {
    processing.remove(block);
    progressTime.remove(block);
    BlockStorage.addBlockInfo(block, PROCESSING_ITEM_KEY, null);
    BlockStorage.addBlockInfo(block, PROCESSING_TIME_KEY, null);
  }

  private ItemStack validRecipeItem(BlockMenu inv) {

    for (AbstractItemRecipe produce : this.recipes) {
      ItemStack itemStack = produce.getFirstItemInput();
      itemStack.setAmount(getAmountUpgrade());
      if (SlimefunUtils.isItemSimilar(inv.getItemInSlot(getInputSlots()[0]), itemStack, false, true)) {
        inv.consumeItem(getInputSlots()[0], getAmountUpgrade());
        return produce.getFirstItemOutput();
      }

    }
    return null;
  }

  @Nonnull
  @Override
  public List<ItemStack> getDisplayRecipes() {
    List<ItemStack> displayRecipes = new ArrayList();
    this.recipes
        .stream().filter(Objects::nonNull)
        .forEach(recipe -> {
      ItemStack itemStack = recipe.getFirstItemOutput().clone();
      itemStack.setAmount(getAmountUpgrade());
      displayRecipes.add(itemStack);
      displayRecipes.add(recipe.getFirstItemOutput());
    });
    return displayRecipes;
  }

  public int getSpeed() {
    return speed;
  }

  public TechRobotic setSpeed(int speed) {
    this.speed = speed;
    return this;
  }

  public int getAmountUpgrade() {
    return amountUpgrade;
  }

  public TechRobotic setAmountUpgrade(int amountUpgrade) {
    this.amountUpgrade = amountUpgrade;
    return this;
  }

  @Nonnull
  @Override
  public Radioactivity getRadioactivity() {
    return Radioactivity.VERY_HIGH;
  }

}
