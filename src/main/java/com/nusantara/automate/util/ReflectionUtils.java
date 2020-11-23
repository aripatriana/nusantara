package com.nusantara.automate.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReflectionUtils {
	
	private static Logger log = LoggerFactory.getLogger(ReflectionUtils.class);

	public static boolean checkAssignableFrom(Class<?> sourceClass, Class<?> targetClass) {
		if (sourceClass.isAssignableFrom(targetClass))
			return true;
		
		boolean result = false;
		for (Class<?> c : sourceClass.getInterfaces()) {
			if (c.isAssignableFrom(Class.class))
				return false;
			result = checkAssignableFrom(c, targetClass);
			if (result) break;
		}
		return result;
	}
	
	public static void setProperty(Object object, String fieldName, Object data) {
		setProperty(object, object.getClass(), fieldName, data);;
	}
	
	public static void setProperty(Object object, Class<?> clazz, String fieldName, Object data) {
		Field field = null;
		try {
			field = clazz
			    .getDeclaredField(fieldName);
		} catch (NoSuchFieldException e1) {
			Class<?> superClazz = clazz.getSuperclass();
			if (!Object.class.equals(superClazz)) {
				setProperty(object, superClazz, fieldName,  data);
			} else {
				log.error("ERROR ", e1);
			}
		} catch (SecurityException e1) {
			log.error("ERROR ", e1);
		}
		
		if (field != null) {
		    try {
		    	field.setAccessible(true);
				field.set(object, data);
			} catch (IllegalArgumentException e) {
				log.error("ERROR ", e);
			} catch (IllegalAccessException e) {
				log.error("ERROR ", e);
			}
		}	
	}

	public static Object invokeMethod(Object object, String methodName, Class<?>[] clazz, Object[] data) {
		return invokeMethod(object, object.getClass(), methodName, clazz, data);
		
	}

	
	public static Object invokeMethod(Object object, String methodName, Class<?> clazz, Object data) {
		return invokeMethod(object, object.getClass(), methodName, new Class<?>[] {clazz}, new Object[] {data});
		
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T invokeMethod(Object object, Class<T> clazz, String methodName, Class<?>[] parameterClazz, Object[] data) {
		try {
			Method method = clazz.getDeclaredMethod(methodName, parameterClazz);
			try {
				return (T) method.invoke(object, data);
			} catch (IllegalAccessException e) {
				log.error("ERROR ", e);
			} catch (IllegalArgumentException e) {
				log.error("ERROR ", e);
			} catch (InvocationTargetException e) {
				log.error("ERROR ", e);
			}
		} catch (NoSuchMethodException e) {
			Class<?> superClazz = clazz.getSuperclass();
			if (!Object.class.equals(superClazz)) {
				invokeMethod(object, superClazz, methodName, parameterClazz, data);
			} else {
				log.error("ERROR ", e);
			}
		} catch (SecurityException e) {
			log.error("ERROR ", e);
		}
		return null;
	}
	
	
	
	public static Object instanceObject(Class<?> clazz) {
		return instanceObject(clazz, null);
	}
	
	public static Object instanceObject(Class<?> clazz, Object[] args) {
		if (args == null) {
			try {
				return clazz.newInstance();
			} catch (InstantiationException e) {
				log.error("ERROR ", e);
			} catch (IllegalAccessException e) {
				log.error("ERROR ", e);
			}
		} else {
			Class<?>[] parameterTypes = new Class<?>[args.length];
			for (int i=0; i<args.length; i++) {
				parameterTypes[i] = args[i].getClass();
			}
			try {
				return clazz.getConstructor(parameterTypes).newInstance(args);
			} catch (InstantiationException e) {
				log.error("ERROR ", e);
			} catch (IllegalAccessException e) {
				log.error("ERROR ", e);
			} catch (IllegalArgumentException e) {
				log.error("ERROR ", e);
			} catch (InvocationTargetException e) {
				log.error("ERROR ", e);
			} catch (NoSuchMethodException e) {
				log.error("ERROR ", e);
			} catch (SecurityException e) {
				log.error("ERROR ", e);
			}
			
		}
		return null;
	}
}
