package dev.mydogsed.sollexicalanalyzer.commands.framework;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public interface SlashCommand {

    /**
     * This method is called when the bot starts up,
     * and should contain all slash command data for the commands in this class.
     * @return The SlashCommandData for commands in this class
     */
    SlashCommandData getData();

    /**
     * This method is called when the command associated with the implementing class is called.
     * @param event The event object supplied by JDA
     */
    void onCommand(SlashCommandInteractionEvent event);

}
