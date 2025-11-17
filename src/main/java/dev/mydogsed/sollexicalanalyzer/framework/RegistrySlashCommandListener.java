package dev.mydogsed.sollexicalanalyzer.framework;

import dev.mydogsed.sollexicalanalyzer.Main;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

/*
Class that listens for slash command events and executes the related onCommand method via the registry.
 */
public class RegistrySlashCommandListener extends ListenerAdapter {

    /*
    When a slash command is received, check the registry. If the command is present, execute it.
    If the command is not present, log a warning and tell the user.
    */
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {

        // get the name of the command
        String command = event.getName();
        CommandRegistry registry = CommandRegistry.getInstance();

        // if the executor for the command is not found, log and error to user
        if (!registry.containsExecutor(command)){
            LoggerFactory.getLogger(RegistrySlashCommandListener.class)
                    .warn("Command executor for '{}' is not registered!", command);
            event
                    .reply("An error occurred with that command. Please try again later.")
                    .addContent("(internal error: executor not registered)")
                    .setEphemeral(true)
                    .queue();
        }
        // Execute the command.
        registry.getExecutor(command).onCommand(event);
    }
}
