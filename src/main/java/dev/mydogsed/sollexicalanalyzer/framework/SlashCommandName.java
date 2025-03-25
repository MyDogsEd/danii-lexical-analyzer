package dev.mydogsed.sollexicalanalyzer.framework;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*
Represents a slash command contained in a single method.
How to define a method slash command:
    - Define a public static method that has as a parameter a `SlashCommandInteractionEvent`

 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SlashCommandName {
    String value();
}
