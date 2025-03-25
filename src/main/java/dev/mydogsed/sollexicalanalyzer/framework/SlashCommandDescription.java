package dev.mydogsed.sollexicalanalyzer.framework;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SlashCommandDescription {
    String value() default "A sol-lexical-analyzer command";
}
