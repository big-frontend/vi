package com.jamesfchen.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Copyright ® $ 2017
 * All right reserved.
 *
 * author jamesfchen
 * since Aug/17/2019  Sat
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD})
public @interface TraceLog {
}
