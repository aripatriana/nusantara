package com.nusantara.automate.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Since Nusantara version 0.0.3, this instead used to map object from the session
 * 
 * @author ari.patriana
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface MapSession {
	
	public String name() default "";
}
