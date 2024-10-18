package dev.mydogsed.daniilexicalanalyzer.commands;

import dev.mydogsed.daniilexicalanalyzer.Main;
import dev.mydogsed.daniilexicalanalyzer.commands.framework.SlashCommandDescription;
import dev.mydogsed.daniilexicalanalyzer.commands.framework.SlashCommandExecutor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;

import java.util.*;

import static dev.mydogsed.daniilexicalanalyzer.commands.MiscCommands.getMessages;

public class LexicalCommands {

    @SlashCommandExecutor("lettercount")
    @SlashCommandDescription("Returns the percent of each letter in all of danii's keyboard smashes")
    public static void letterCountCommand(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        event.deferReply().queue();

        // Get the messages in the channel and write all of them into one string
        List<Message> messages = getMessages(event.getChannel().asTextChannel());
        StringBuilder letters = new StringBuilder();
        for( Message message : messages ) {
            letters.append(message.getContentDisplay().toLowerCase());
        }

        // Get a character array from the string and sort it
        char[] arr = letters.toString().toCharArray();
        Arrays.sort(arr);
        Map<Character, Integer> map = new HashMap<>();

        // Count occurrences of each letter in the sorted array
        int i = 0;
        while (i < arr.length) {
            char letter = arr[i];
            int j = i + 1;
            int letterCount = 1;
            while (j < arr.length && arr[j] == letter) {
                letterCount++;
                j++;
            }
            i = j;
            map.put(letter, (int)((((double)letterCount)/100.0) * 100));
        }
        Character[] keys = map.keySet().toArray(new Character[0]);
        Arrays.sort(keys);


        EmbedBuilder eb = new EmbedBuilder()
                .setAuthor("danii-lexical-analyzer", "https://mydogsed.dev", Main.jda.getSelfUser().getAvatarUrl())
                .setTitle("Letter Percentages");
        for(int k = 0; k < 10 && k < keys.length; k++) {
            eb.addField(String.valueOf(keys[k]), String.valueOf(map.get(keys[k])), false);
        }

        hook.editOriginalEmbeds(eb.build()).queue();
    }
}
