package dev.mydogsed.daniilexicalanalyzer.commands.framework;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SlashCommandDescription {
    String value() default "Error: No description provided";
}
