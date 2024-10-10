package dev.mydogsed.daniilexicalanalyzer.commands.framework;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/*
Represents a simple call-and-response command, where the same message is used for all responses.
 */
public class SimpleSlashCommand implements SlashCommand {

    private final String response;

    public SimpleSlashCommand(String response){
        this.response = response;
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        event.reply(response).setEphemeral(true).queue();
    }
}
