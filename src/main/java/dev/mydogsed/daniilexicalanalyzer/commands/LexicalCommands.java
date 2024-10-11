package dev.mydogsed.daniilexicalanalyzer.commands;

import dev.mydogsed.daniilexicalanalyzer.commands.framework.SlashCommandMethod;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;

public class LexicalCommands {

    @SlashCommandMethod("number")
    public static void numberCommand(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        event.deferReply().queue();
        TextChannel channel = event.getChannel().asTextChannel();
        channel.getHistoryFromBeginning(100).queue(messageHistory
                -> hook.editOriginal(String.valueOf(messageHistory.size())));
    }

}
