package com.extendedclip.deluxemenus.utils;

import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Adapts Bukkit item flags to the tooltip display component introduced in 1.21.5.
 *
 * <p>The complete additional-tooltip set is important for protocol translators: ViaBackwards only restores
 * {@code HIDE_ADDITIONAL_TOOLTIP} for 1.21.4 clients when every legacy additional-tooltip component is hidden.
 * This class is only loaded through a version guard in {@link com.extendedclip.deluxemenus.menu.MenuItem}, so
 * older Paper versions supported by DeluxeMenus do not need the data component API at runtime.</p>
 */
public final class TooltipDisplayCompat {

    private static final List<String> ADDITIONAL_TOOLTIP_KEYS = List.of(
            "banner_patterns",
            "bees",
            "block_entity_data",
            "block_state",
            "bundle_contents",
            "charged_projectiles",
            "container",
            "container_loot",
            "firework_explosion",
            "fireworks",
            "instrument",
            "map_id",
            "painting/variant",
            "pot_decorations",
            "potion_contents",
            "tropical_fish/pattern",
            "written_book_content"
    );

    private static final Set<DataComponentType> ADDITIONAL_TOOLTIP_COMPONENTS = resolveAdditionalTooltipComponents();

    private TooltipDisplayCompat() {
    }

    public static void apply(@NotNull final ItemStack itemStack, @NotNull final Set<ItemFlag> itemFlags) {
        final boolean hideAdditional = itemFlags.contains(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        final boolean hideAttributes = itemFlags.contains(ItemFlag.HIDE_ATTRIBUTES);
        final boolean hideEnchants = itemFlags.contains(ItemFlag.HIDE_ENCHANTS);
        if (!hideAdditional && !hideAttributes && !hideEnchants) {
            return;
        }

        final TooltipDisplay current = itemStack.getData(DataComponentTypes.TOOLTIP_DISPLAY);
        final Set<DataComponentType> hiddenComponents = new LinkedHashSet<>();
        if (current != null) {
            hiddenComponents.addAll(current.hiddenComponents());
        }
        if (hideAdditional) {
            hiddenComponents.addAll(ADDITIONAL_TOOLTIP_COMPONENTS);
        }
        if (hideAttributes) {
            hiddenComponents.add(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        }
        if (hideEnchants) {
            hiddenComponents.add(DataComponentTypes.ENCHANTMENTS);
            hiddenComponents.add(DataComponentTypes.STORED_ENCHANTMENTS);
        }

        itemStack.setData(
                DataComponentTypes.TOOLTIP_DISPLAY,
                TooltipDisplay.tooltipDisplay()
                        .hideTooltip(current != null && current.hideTooltip())
                        .hiddenComponents(hiddenComponents)
                        .build()
        );
    }

    private static @NotNull Set<DataComponentType> resolveAdditionalTooltipComponents() {
        final Set<DataComponentType> components = new LinkedHashSet<>();
        for (final String key : ADDITIONAL_TOOLTIP_KEYS) {
            components.add(Registry.DATA_COMPONENT_TYPE.getOrThrow(NamespacedKey.minecraft(key)));
        }
        return Collections.unmodifiableSet(components);
    }
}
