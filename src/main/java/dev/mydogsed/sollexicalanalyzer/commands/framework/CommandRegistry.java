package dev.mydogsed.sollexicalanalyzer.commands.framework;

import dev.mydogsed.sollexicalanalyzer.commands.QuotesCommands;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

import java.lang.reflect.Method;
import java.util.*;

public class CommandRegistry {

    // Static field to hold the (only) instance of the registry
    private static CommandRegistry instance;

    // The Map actually used to register PlayerProfile instances
    private final Map<String, SlashCommand> map;

    // Getter to return a reference to the instance
    public static CommandRegistry getInstance() {
        if (instance == null) {
            instance = new CommandRegistry();
        }
        return instance;
    }

    // Private Constructor
    private CommandRegistry() {
        map = new HashMap<>();
    }

    // Public method to register commands in the registry
    public void register(SlashCommand executor) {
        map.put(executor.data().getName(), executor);
    }

    /*
    Register multiple commands from a single class as command executors using an annotation shorthand
    This is so that less complex commands can be registered with just a method and not need an entire dedicated class
     */
    public void registerMethods(Class<?> clazz) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (!method.isAnnotationPresent(SlashCommandName.class)) {
                continue;
            }
            SlashCommandName methodAnnotation = method.getAnnotation(SlashCommandName.class);
            String name = methodAnnotation.value();

            String description = "A sol-lexical-analyzer command";
            if (method.isAnnotationPresent(SlashCommandDescription.class)) {
                SlashCommandDescription descriptionAnnotation = method.getAnnotation(SlashCommandDescription.class);
                description = descriptionAnnotation.value();
            }
            String finalDescription = description; // Effectively final variable
            register(new SlashCommand() {
                @Override
                public void onCommand(SlashCommandInteractionEvent event) {
                    try {
                        method.invoke(null, event);
                    } catch (Exception e) {
                        e.printStackTrace();
                        sendErrorMessage(event.getChannel().asTextChannel(), e.toString());
                    }
                }

                @Override
                public CommandData data() {
                    return Commands.slash(name, finalDescription);
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

    public Set<String> getCommandNames() {
        return map.keySet();
    }

    private void sendErrorMessage(TextChannel channel, String errorMessage) {
        MessageCreateBuilder messageBuilder = new MessageCreateBuilder()
                .setContent("Looks like something broke <@335802802335121408> \n `" + errorMessage + "`")
                .addFiles(
                        FileUpload.fromData(
                                Objects.requireNonNull(QuotesCommands.class.getResourceAsStream("/broken.png")),
                                "broken.png"
                        )
                );
        channel.sendMessage(messageBuilder.build()).queue();
    }
}

