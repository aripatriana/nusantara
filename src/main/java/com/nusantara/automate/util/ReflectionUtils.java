package com.nusantara.automate.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionUtils {

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
				e1.printStackTrace();
			}
		} catch (SecurityException e1) {
			e1.printStackTrace();
		}
		
		if (field != null) {
		    try {
		    	field.setAccessible(true);
				field.set(object, data);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
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
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		} catch (NoSuchMethodException e) {
			Class<?> superClazz = clazz.getSuperclass();
			if (!Object.class.equals(superClazz)) {
				invokeMethod(object, superClazz, methodName, parameterClazz, data);
			} else {
				e.printStackTrace();
				
			}
		} catch (SecurityException e) {
			e.printStackTrace();
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
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		} else {
			Class<?>[] parameterTypes = new Class<?>[args.length];
			for (int i=0; i<args.length; i++) {
				parameterTypes[i] = args[i].getClass();
			}
			try {
				return clazz.getConstructor(parameterTypes).newInstance(args);
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			}
			
		}
		return null;
	}
}
