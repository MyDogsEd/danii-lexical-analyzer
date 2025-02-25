package dev.mydogsed.sollexicalanalyzer.commands.framework;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public interface SlashCommand {

    /*
    Called when the command is to be run.
     */
    void onCommand(SlashCommandInteractionEvent event);

    SlashCommandData getData();

}
