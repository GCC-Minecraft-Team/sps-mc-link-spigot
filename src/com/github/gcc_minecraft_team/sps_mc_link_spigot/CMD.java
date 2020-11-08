package com.github.gcc_minecraft_team.sps_mc_link_spigot;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
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
        return str.toString() + "\n";
    }

    /**
     * Gives an array of {@link TextComponent}s similar to {@link #buildListText(String, List)} except that each entry is a tellraw.
     * The array can be passed directly to {@link org.bukkit.command.CommandSender.Spigot#sendMessage(BaseComponent...)}.
     * @param title The title of the list.
     * @param items A {@link List} of items to include. Should be able to be put into the command.
     * @param command The command to run, with {@code %s} as the spot where the item should be substituted.
     * @return An array of {@link TextComponent}s ready to be sent.
     */
    @NotNull
    public static TextComponent[] buildListTellraw(@NotNull String title, @NotNull List<String> items, @NotNull String command) {
        TextComponent[] out = new TextComponent[items.size() + 1];
        out[0] = new TextComponent("====[" + title + "]====\n");
        out[0].setBold(true);
        for (int i = 0; i < items.size(); i++) {
            out[i + 1] = new TextComponent(items.get(i) + "\n");
            out[i + 1].setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format(command, items.get(i))));
        }
        return out;
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

    /**
     * Gives an array of {@link TextComponent}s similar to {@link #buildListText(String, List)} except that each entry has boolean tellraw.
     * The array can be passed directly to {@link org.bukkit.command.CommandSender.Spigot#sendMessage(BaseComponent...)}.
     * @param title The title of the list.
     * @param items A {@link List} of items to include. Should be able to be put into the command.
     * @param command The command to run, with {@code %s} as the spot where the item should be substituted, and {@code %b} as the spot for setting another boolean value.
     * @return An array of {@link TextComponent}s ready to be sent.
     */
    @NotNull
    public static TextComponent[] buildListBooleanTellraw(@NotNull String title, @NotNull Map<String, Boolean> items, @NotNull String command) {
        TextComponent[] out = new TextComponent[items.size() + 1];
        out[0] = new TextComponent("====[" + title + "]====\n");
        out[0].setBold(true);
        List<String> keys = new ArrayList<>(items.keySet());
        for (int i = 0; i < items.size(); i++) {
            String str = keys.get(i);
            out[i + 1] = new TextComponent(str + " - ");

            TextComponent textTrue = new TextComponent("[TRUE]");
            textTrue.setColor(net.md_5.bungee.api.ChatColor.GREEN);
            textTrue.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format(command, str, true)));

            TextComponent textFalse = new TextComponent("[FALSE]");
            textFalse.setColor(net.md_5.bungee.api.ChatColor.RED);
            textFalse.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format(command, str, false)));

            if (items.get(str))
                textTrue.setBold(true);
            else
                textFalse.setBold(true);

            out[i + 1].addExtra(textTrue);
            out[i + 1].addExtra(textFalse);
        }
        return out;
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
