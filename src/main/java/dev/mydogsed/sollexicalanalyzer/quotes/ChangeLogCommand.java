package dev.mydogsed.sollexicalanalyzer.quotes;

import dev.mydogsed.sollexicalanalyzer.framework.SlashCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class ChangeLogCommand implements SlashCommand {
    @Override
    public SlashCommandData getData() {
        return Commands.slash("changelog", "Find out what's new!");
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {

    }
}
