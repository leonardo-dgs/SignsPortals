package net.leonardo_dgs.signsportals.util;

import me.lucko.helper.reflect.ServerReflection;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

public final class PlayerUtil {

    private static final Object TITLE_ENUM;
    private static final Object SUBTITLE_ENUM;
    private static final Constructor<?> TITLE_CONSTRUCTOR;

    private static final Method ICHATBASECOMPONENT_A_METHOD;

    private static final Object ACTIONBAR_ENUM;
     private static final Constructor<?> ACTIONBAR_CONSTRUCTOR;

    static {
        Object title_Enum = null;
        Object subtitle_Enum = null;
        Constructor<?> title_Constructor = null;
        Method iChatBaseComponent_A_Method = null;
        Object actionbar_Enum = null;
        Constructor<?> actionbar_Constructor = null;
        try {
            title_Enum = ServerReflection.nmsClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("TITLE").get(null);
            subtitle_Enum = ServerReflection.nmsClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("SUBTITLE").get(null);
            title_Constructor = ServerReflection.nmsClass("PacketPlayOutTitle").getConstructor(ServerReflection.nmsClass("PacketPlayOutTitle").getDeclaredClasses()[0], ServerReflection.nmsClass("IChatBaseComponent"), int.class, int.class, int.class);
            iChatBaseComponent_A_Method = ServerReflection.nmsClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class);
            actionbar_Enum = ServerReflection.nmsClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("ACTIONBAR").get(null);
            actionbar_Constructor = ServerReflection.nmsClass("PacketPlayOutTitle").getConstructor(ServerReflection.nmsClass("PacketPlayOutTitle").getDeclaredClasses()[0], ServerReflection.nmsClass("IChatBaseComponent"));
        } catch (IllegalAccessException | NoSuchFieldException | NoSuchMethodException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        TITLE_ENUM = title_Enum;
        SUBTITLE_ENUM = subtitle_Enum;
        TITLE_CONSTRUCTOR = title_Constructor;
        ICHATBASECOMPONENT_A_METHOD = iChatBaseComponent_A_Method;
        ACTIONBAR_ENUM = actionbar_Enum;
        ACTIONBAR_CONSTRUCTOR = actionbar_Constructor;
    }

    /**
     * Sends an action bar to a set of players.
     *
     * @param text the action bar text
     * @param players the players to whom send the action bar
     */
    public static void sendActionBar(String text, Player... players) {
        Objects.requireNonNull(ICHATBASECOMPONENT_A_METHOD);
        Objects.requireNonNull(ACTIONBAR_CONSTRUCTOR);
        text = text.replace("\\", "\\\\").replace("\"", "\\\"");
        try {
            for (Player player : players) {
                Object chatText = ICHATBASECOMPONENT_A_METHOD.invoke(null, "{\"text\":\"" + text + "\"}");
                Object titlePacket = ACTIONBAR_CONSTRUCTOR.newInstance(ACTIONBAR_ENUM, chatText);
                ReflectionUtil.sendPacket(titlePacket, player);
            }
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
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
