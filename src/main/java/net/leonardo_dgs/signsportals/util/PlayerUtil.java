package net.leonardo_dgs.signsportals.util;

import me.lucko.helper.reflect.ServerReflection;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

public final class PlayerUtil {

    private static final Method ICHATBASECOMPONENT_A_METHOD;

    private static final Object ACTIONBAR_ENUM;
    private static final Constructor<?> ACTIONBAR_CONSTRUCTOR;

    static {
        Method iChatBaseComponent_A_Method = null;
        Object actionbar_Enum = null;
        Constructor<?> actionbar_Constructor = null;
        try {
            iChatBaseComponent_A_Method = ServerReflection.nmsClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class);
            actionbar_Enum = ServerReflection.nmsClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("ACTIONBAR").get(null);
            actionbar_Constructor = ServerReflection.nmsClass("PacketPlayOutTitle").getConstructor(ServerReflection.nmsClass("PacketPlayOutTitle").getDeclaredClasses()[0], ServerReflection.nmsClass("IChatBaseComponent"));
        } catch (IllegalAccessException | NoSuchFieldException | NoSuchMethodException | ClassNotFoundException e) {
            e.printStackTrace();
        }
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
