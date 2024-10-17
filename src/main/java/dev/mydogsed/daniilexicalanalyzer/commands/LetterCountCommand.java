package dev.mydogsed.daniilexicalanalyzer.commands;


import dev.mydogsed.daniilexicalanalyzer.Main;
import dev.mydogsed.daniilexicalanalyzer.commands.framework.SlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.mydogsed.daniilexicalanalyzer.commands.MiscCommands.getMessages;

public class LetterCountCommand implements SlashCommand {

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        event.deferReply().queue();

        // Get the messages in the channel and write all of them into one string
        List<Message> messages = getMessages(event.getChannel().asTextChannel());
        String letters = "";
        for( Message message : messages ) {
            letters += message.getContentDisplay();
        }

        // Get a character array from the string and sort it
        char[] arr = letters.toCharArray();
        Arrays.sort(arr);
        Map<Character, Double> map = new HashMap<>();

        // Count occurrences of each letter in the sorted array
        for(int i = 0; i < arr.length; i++) {
            char letter = arr[i];
            int j = i + 1;
            int letterCount = 0;
            while (j < arr.length && arr[j] == letter) {
                letterCount++;
                j++;
            }
            map.put(letter, ((double)letterCount));
        }
        Character[] keys = map.keySet().toArray(new Character[0]);
        Arrays.sort(keys);


        EmbedBuilder eb = new EmbedBuilder()
                .setAuthor("danii-lexical-analyzer", "https://mydogsed.dev", Main.jda.getSelfUser().getAvatarUrl())
                .setTitle("Letter Percentages");
        for(int i = 0; i < 10 && i < keys.length; i++) {
            eb.addField(String.valueOf(keys[i]), String.valueOf(map.get(keys[i])), false);
        }

        hook.editOriginalEmbeds(eb.build()).queue();
    }
}
