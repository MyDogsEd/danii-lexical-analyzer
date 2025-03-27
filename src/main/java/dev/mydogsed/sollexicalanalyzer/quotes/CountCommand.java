package dev.mydogsed.sollexicalanalyzer.quotes;

import dev.mydogsed.sollexicalanalyzer.framework.SlashCommand;
import dev.mydogsed.sollexicalanalyzer.quotes.persist.QuotesDB;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import static dev.mydogsed.sollexicalanalyzer.quotes.QuotesUtil.quotesEmbed;

public class CountCommand implements SlashCommand {

    @Override
    public SlashCommandData getData() {
        return Commands.slash("count", "Counts the number of quotes");
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        event.deferReply().queue();

        int size = QuotesDB.getQuotes().size();

        hook.editOriginalEmbeds(
                quotesEmbed("Number of Quotes")
                        .setDescription("#quotes-without-context has " + size + " quotes archived." )
                        .build()
        ).queue();
    }
}
