package cpw.mods.fml.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Compile-only stand-in for Forge 1.7's {@code @Mod}. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Mod {
    String modid();
    String name() default "";
    boolean useMetadata() default false;
    String acceptableRemoteVersions() default "";
}
