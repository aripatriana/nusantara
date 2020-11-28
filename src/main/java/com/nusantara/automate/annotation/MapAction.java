package com.nusantara.automate.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Deprecated since Nusantara version 0.0.3, instead see MapEntity annotation
 * @author ari.patriana
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@Deprecated
public @interface MapAction {
	
	public Class<?> clazz();
	
	public String name() default "";

}
