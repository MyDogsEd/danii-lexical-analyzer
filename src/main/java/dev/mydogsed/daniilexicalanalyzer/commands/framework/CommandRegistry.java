package dev.mydogsed.daniilexicalanalyzer.commands.framework;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CommandRegistry {

    // Static field to hold the (only) instance of the registry
    private static CommandRegistry instance;

    // The Map actually used to register PlayerProfile instances
    private final Map<String, SlashCommand> map;

    // Getter to return a reference to the instance
    public static CommandRegistry getInstance() {
        if (instance == null){
            instance = new CommandRegistry();
        }
        return instance;
    }

    // Private Constructor
    private CommandRegistry() {
        map = new HashMap<>();
    }

    // Public method to register commands in the registry
    public void register (String name, SlashCommand executor) {
        map.put(name, executor);
    }

    /*
    Register multiple commands from a single class as command executors using an annotation shorthand
    This is so that less complex commands can be registered with just a method and not need an entire dedicated class
     */
    public void registerMethods(Class<?> clazz){
        for (Method method : clazz.getDeclaredMethods()) {
            if (!method.isAnnotationPresent(SlashCommandExecutor.class)) {
                continue;
            }
            SlashCommandExecutor methodAnnotation = method.getAnnotation(SlashCommandExecutor.class);
            String name = methodAnnotation.value();

            String description = "A danii-lexical-analyzer command";
            if (method.isAnnotationPresent(SlashCommandDescription.class)) {
                SlashCommandDescription descriptionAnnotation = method.getAnnotation(SlashCommandDescription.class);
                description = descriptionAnnotation.value();
            }
            String finalDescription = description; // Effectivly final variable
            register(name, new SlashCommand() {
                @Override
                public void onCommand(SlashCommandInteractionEvent event) {
                    try {
                        method.invoke(null, event);
                    }
                    catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public String getDescription() {
                    return finalDescription;
                }
            });
        }
    }

    // Return the Executor method for the searched command
    public SlashCommand getExecutor(String name) {
        return map.get(name);
    }

    // See whether an executor is stored for a given command or not
    public boolean containsExecutor(String name) {
        return map.containsKey(name);
    }

    public Set<String> getCommandNames(){
        return map.keySet();
    }

}

