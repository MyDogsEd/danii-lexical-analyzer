package dev.mydogsed.daniilexicalanalyzer.commands;

import dev.mydogsed.daniilexicalanalyzer.Main;
import dev.mydogsed.daniilexicalanalyzer.commands.framework.SlashCommandDescription;
import dev.mydogsed.daniilexicalanalyzer.commands.framework.SlashCommandExecutor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static dev.mydogsed.daniilexicalanalyzer.commands.MiscCommands.getMessages;

public class LexicalCommands {

    @SlashCommandExecutor("lettercount")
    @SlashCommandDescription("Returns the percent of each letter in all of danii's keyboard smashes")
    public static void letterCountCommand(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        event.deferReply().queue();

        char[] chars = getCharactersInMessages(event.getChannel().asTextChannel());
        Map<Character, Integer> map = getCharacterOccurencesMap(chars);

        // Convert the keys in the hashmap to a list
        List<Character> keys = new ArrayList<>(map.keySet().stream().toList());

        // Sort that list based on the key's value in the map
        keys.sort(Comparator.comparing(map::get));

        // Build an embed with that information
        EmbedBuilder eb = new EmbedBuilder()
                .setAuthor("danii-lexical-analyzer", "https://mydogsed.dev", Main.jda.getSelfUser().getAvatarUrl())
                .setTitle("Letter Percentages");
        for(int k = keys.size() - 1; k > keys.size() - 10; k--) {
            eb.addField(String.valueOf(keys.get(k)), String.valueOf(map.get(keys.get(k))), false);
        }
        hook.editOriginalEmbeds(eb.build()).queue();
    }

    private static char[] getCharactersInMessages(TextChannel channel) {
        // Get the messages in the channel and write all of them into one string
        List<Message> messages = getMessages(channel);
        StringBuilder letters = new StringBuilder();
        for( Message message : messages ) {
            letters.append(message.getContentDisplay().toLowerCase());
        }

        // Get a character array from the string and sort it
        char[] arr = letters.toString().replaceAll("\\s","").toCharArray();
        Arrays.sort(arr);
        return arr;
    }

    @NotNull
    private static Map<Character, Integer> getCharacterOccurencesMap(char[] arr) {
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

            if (!(letter == ' ')){
                map.put(letter, (int)((((double)letterCount) / arr.length) * 100));
            }
        }
        return map;
    }


}
