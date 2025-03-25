package dev.mydogsed.sollexicalanalyzer.framework;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

/*
Represents a simple call-and-response command, where the same message is used for all responses.
 */
public class SimpleSlashCommand implements SlashCommand {

    private final String response;
    private final String description;
    private final String name;

    public SimpleSlashCommand(String name, String description, String response){
        this.response = response;
        this.description = description;
        this.name = name;
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        event.reply(response).setEphemeral(true).queue();
    }

    @Override
    public SlashCommandData getData() {
        return Commands.slash(name, description);
    }
}
