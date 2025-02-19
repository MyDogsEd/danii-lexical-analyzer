package dev.mydogsed.sollexicalanalyzer.commands.framework;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/*
Represents a simple call-and-response command, where the same message is used for all responses.
 */
public class SimpleSlashCommand implements SlashCommand {

    private final String response;
    private final String description;

    public SimpleSlashCommand(String response, String description){
        this.response = response;
        this.description = description;
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        event.reply(response).setEphemeral(true).queue();
    }

    @Override
    public String getDescription() {
        return this.description;
    }
}
