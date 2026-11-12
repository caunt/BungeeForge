package net.minecraftforge.fml.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Compile-only stand-in for Forge's {@code @Mod}. Forge 1.8 - 1.12 reads {@code modid}, Forge 1.13+ reads
 * {@code value}; each ignores the members it does not know, so one annotation serves both.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Mod {
    String value() default "";
    String modid() default "";
    String name() default "";
    boolean useMetadata() default false;
    String acceptableRemoteVersions() default "";
    boolean serverSideOnly() default false;
}
