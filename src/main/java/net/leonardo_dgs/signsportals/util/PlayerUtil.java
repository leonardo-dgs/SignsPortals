package net.leonardo_dgs.signsportals.util;

import me.lucko.helper.reflect.ServerReflection;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class PlayerUtil {

    private static final boolean ACTIONBAR_NATIVE_SUPPORT = MinecraftVersion.getRunningVersion().isAfterOrEqual(MinecraftVersion.parse("1.9"));

    private static final Method ICHATBASECOMPONENT_A_METHOD;
    private static final Constructor<?> ACTIONBAR_CONSTRUCTOR;

    static {
        Method iChatBaseComponentAMethod = null;
        Constructor<?> actionbarConstructor = null;
        if(!ACTIONBAR_NATIVE_SUPPORT) {
            try {
                Class<?> iChatBaseComponentClass = ServerReflection.nmsClass("IChatBaseComponent");
                iChatBaseComponentAMethod = iChatBaseComponentClass.getDeclaredClasses()[0].getMethod("a", String.class);
                actionbarConstructor = ServerReflection.nmsClass("PacketPlayOutChat").getConstructor(iChatBaseComponentClass, byte.class);
            } catch (NoSuchMethodException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        ICHATBASECOMPONENT_A_METHOD = iChatBaseComponentAMethod;
        ACTIONBAR_CONSTRUCTOR = actionbarConstructor;
    }

    /**
     * Sends an action bar to a set of players.
     *
     * @param text the action bar text
     * @param players the players to whom send the action bar
     */
    public static void sendActionBar(String text, Player... players) {
        if(ACTIONBAR_NATIVE_SUPPORT) {
            for (Player player : players)
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(text));
        }
        else {
            text = text.replace("\\", "\\\\").replace("\"", "\\\"");
            try {
                for (Player player : players) {
                    Object chatText = ICHATBASECOMPONENT_A_METHOD.invoke(null, "{\"text\":\"" + text + "\"}");
                    Object titlePacket = ACTIONBAR_CONSTRUCTOR.newInstance(chatText, (byte) 2);
                    ReflectionUtil.sendPacket(titlePacket, player);
                }
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Clears the action bar to a set of players.
     *
     * @param players the players to whom clear the action bar
     */
    public static void clearActionBar(Player... players) {
        sendActionBar("", players);
    }

}
