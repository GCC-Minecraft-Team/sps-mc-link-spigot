package com.github.gcc_minecraft_team.sps_mc_link_spigot;

import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CMD {
    /**
     * Builds the text for a list to be sent in chat.
     * @param title The title of the list. May safely include {@link ChatColor}s.
     * @param items A {@link List} of the items to include.
     * @return A multi-line text representation of the list.
     */
    @NotNull
    public static String buildListText(@NotNull String title, @NotNull List<String> items) {
        StringBuilder str = new StringBuilder(ChatColor.BOLD + "====[" + title.strip() + ChatColor.RESET + ChatColor.BOLD + "]====\n");
        for (String item : items) {
            str.append(ChatColor.RESET).append(item.strip()).append("\n");
        }
        return str.toString();
    }

    /**
     * Builds the text for a list to be sent in chat, with boolean values for each item.
     * @param title The title of the list. May safely include {@link ChatColor}s.
     * @param items A {@link Map} of the items to include and their boolean values.
     * @return A multi-line text representation of the list.
     */
    @NotNull
    public static String buildListBooleanText(@NotNull String title, @NotNull Map<String, Boolean> items) {
        List<String> strItems = new ArrayList<>();
        for (Map.Entry<String, Boolean> item : items.entrySet()) {
            if (item.getValue())
                strItems.add(item.getKey().strip() + ChatColor.RESET + " - " + ChatColor.GREEN + "true");
            else
                strItems.add(item.getKey().strip() + ChatColor.RESET + " - " + ChatColor.RED + "false");
        }
        return buildListText(title, strItems);
    }

    @NotNull
    public static List<String> keepStarts(@NotNull List<String> list, @NotNull String prefix) {
        List<String> newList = new ArrayList<>();
        for (String str : list) {
            if (str.toLowerCase().startsWith(prefix.toLowerCase()))
                newList.add(str);
        }
        newList.sort(String.CASE_INSENSITIVE_ORDER);
        return newList;
    }
}
