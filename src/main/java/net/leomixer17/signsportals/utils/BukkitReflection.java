package net.leomixer17.signsportals.utils;

import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class BukkitReflection {
	
	public static Object newFromNMS(final String nms)
	{
		try
		{
			return nmsClass(nms).newInstance();
		}
		catch (InstantiationException | IllegalAccessException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public static Object newFromOBC(final String obc)
	{
		try
		{
			return obcClass(obc).newInstance();
		}
		catch (InstantiationException | IllegalAccessException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public static Class<?> nmsClass(final String nms)
	{
		try
		{
			return Class.forName("net.minecraft.server." + MinecraftVersion.getVersionString() + "." + nms);
		}
		catch (ClassNotFoundException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public static Class<?> obcClass(final String obc)
	{
		try
		{
			return Class.forName("org.bukkit.craftbukkit." + MinecraftVersion.getVersionString() + "." + obc);
		}
		catch (ClassNotFoundException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public static Object getHandle(final Object obj)
	{
		try
		{
			return getMethod(obj.getClass(), "getHandle", new Class[0]).invoke(obj, new Object[0]);
		}
		catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public static Field getField(final Class<?> clazz, final String name)
	{
		try
		{
			Field field = clazz.getDeclaredField(name);
			field.setAccessible(true);
			return field;
		}
		catch (NoSuchFieldException | SecurityException e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public static Method getMethod(final Class<?> clazz, final String name, final Class<?>[] args)
	{
		for (Method m : clazz.getMethods())
		{
			if ((m.getName().equals(name)) && ((args.length == 0) || (classesEqual(args, m.getParameterTypes()))))
			{
				m.setAccessible(true);
				return m;
			}
		}
		return null;
	}
	
	public static boolean classesEqual(final Class<?>[] l1, final Class<?>[] l2)
	{
		boolean equal = true;
		if (l1.length != l2.length)
			return false;
		for (int i = 0; i < l1.length; i++)
		{
			if (l1[i] != l2[i])
			{
				equal = false;
				break;
			}
		}
		return equal;
	}
	
	public static void sendPacket(final Object packet, final Player... players)
	{
		try
		{
			for (final Player player : players)
			{
				final Object craftPlayer = getHandle(player);
				final Object connection = getField(craftPlayer.getClass(), "playerConnection").get(craftPlayer);
				getMethod(connection.getClass(), "sendPacket", new Class[0]).invoke(connection, new Object[]{
						packet
				});
			}
		}
		catch (IllegalAccessException | InvocationTargetException e)
		{
			e.printStackTrace();
		}
	}
	
	public static Class<?> getCaller()
	{
		try
		{
			return Class.forName(Thread.currentThread().getStackTrace()[3].getClassName(), false, BukkitReflection.class.getClassLoader());
		}
		catch (ClassNotFoundException e)
		{
		}
		return null;
	}
	
}