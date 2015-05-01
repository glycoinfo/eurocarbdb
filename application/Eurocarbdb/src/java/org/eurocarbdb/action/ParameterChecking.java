package org.eurocarbdb.action;
import java.lang.annotation.*; 

@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface ParameterChecking {
    String[] whitelist() default {};
    String[] blacklist() default {};
}