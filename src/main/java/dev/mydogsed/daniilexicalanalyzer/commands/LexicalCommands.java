package dev.mydogsed.daniilexicalanalyzer.commands;

import dev.mydogsed.daniilexicalanalyzer.Main;
import dev.mydogsed.daniilexicalanalyzer.commands.framework.SlashCommandDescription;
import dev.mydogsed.daniilexicalanalyzer.commands.framework.SlashCommandExecutor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.DayOfWeek;
import java.util.*;
import java.util.List;

import static dev.mydogsed.daniilexicalanalyzer.commands.MiscCommands.getMessages;

public class LexicalCommands {

    public static EmbedBuilder basicEmbed(String title){
        return new EmbedBuilder()
                .setTitle(title)
                .setAuthor("danii-lexical-analyzer", "https://mydogsed.dev", Main.jda.getSelfUser().getAvatarUrl())
                .setColor(new Color(184, 56, 59))
                .setTimestamp(new Date().toInstant());
    }

    @SlashCommandExecutor("lettercount")
    @SlashCommandDescription("Returns the percent of the top 10 letters in all of danii's keyboard smashes")
    public static void letterCountCommand(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        event.deferReply().queue();

        char[] chars = getCharactersInMessages(getSmashes(event.getChannel().asTextChannel()));
        Map<Character, Integer> map = getCharacterOccurencesMap(chars);

        // Convert the keys in the hashmap to a list
        List<Character> keys = new ArrayList<>(map.keySet().stream().toList());

        // Sort that list based on the key's value in the map
        keys.sort(Comparator.comparing(map::get));

        // Build an embed with that information
        EmbedBuilder eb = basicEmbed("Letter Percentages");
        for(int k = keys.size() - 1; k > keys.size() - 10; k--) {
            eb.addField(String.valueOf(keys.get(k)), String.valueOf(map.get(keys.get(k))) + "%", false);
        }
        hook.editOriginalEmbeds(eb.build()).queue();
    }

    @SlashCommandExecutor("averagelength")
    @SlashCommandDescription("Gets the average length of each keyboard smash")
    public static void averageLengthCommand(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        event.deferReply().queue();

        List<Message> messages = getSmashes(event.getChannel().asTextChannel());

        int sum = 0;
        for(Message message : messages) {
            sum += message.getContentRaw().length();
        }
        double avg = (double)sum / (double)messages.size();

        EmbedBuilder eb = basicEmbed("Average Length")
                .setDescription("The average length of each keyboard smash is " + avg + " characters");
        hook.editOriginalEmbeds(eb.build()).queue();
    }

    @SlashCommandExecutor("longest")
    @SlashCommandDescription("Returns the longest single keyboard smash in the channel")
    public static void longestCommand(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        event.deferReply().queue();
        List<Message> messages = getSmashes(event.getChannel().asTextChannel());
        messages.sort(Comparator.comparing(message -> message.getContentRaw().length()));
        Message longestMessage = messages.get(messages.size() - 1);
        EmbedBuilder eb = basicEmbed("Longest")
                .setDescription("The longest single keyboard smash is " + longestMessage.getContentRaw().length() + " characters")
                .addField(longestMessage.getContentRaw(), longestMessage.getJumpUrl(), false);
        hook.editOriginalEmbeds(eb.build()).queue();
    }

    @SlashCommandExecutor("days")
    @SlashCommandDescription("What day do most keyboard smashes take place on?")
    public static void daysCommand(SlashCommandInteractionEvent event) {
        // Command Boilerplate
        InteractionHook hook = event.getHook();
        event.deferReply().queue();

        // get the keyboard smashes
        List<Message> smashes = getSmashes(event.getChannel().asTextChannel());

        // Sort all of the keyboard smashes by day
        Map<DayOfWeek, List<Message>> days = new HashMap<>();
        for (DayOfWeek day : DayOfWeek.values()) {
            days.put(day, new LinkedList<>());
        }

        for(Message message : smashes) {
            List<Message> list = days.get(message.getTimeCreated().getDayOfWeek());
            list.add(message);
        }

        List<DayOfWeek> keys = new ArrayList<>(days.keySet().stream().toList());
        keys.sort(Comparator.comparing(key -> days.get(key).size()).reversed());

        EmbedBuilder eb = basicEmbed("Days");
        for(DayOfWeek day : keys) {
            eb.addField(String.valueOf(day), days.get(day).size() + " keyboard smashes", false);
        }

        hook.editOriginalEmbeds(eb.build()).queue();
    }

    // Private utility methods for the commands in this class

    // Get all the characters in the list of messages
    private static char[] getCharactersInMessages(List<Message> messages) {
        // Get the messages in the channel and write all of them into one string
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

    // Out of all the messages in a channel, return any that don't contain spaces
    private static List<Message> getSmashes(TextChannel channel) {
        List<Message> messages = getMessages(channel);
        List<Message> smashes = new LinkedList<>();
        for(Message message : messages) {
            if(message.getContentRaw().startsWith("//")) {
                continue;
            }
            if (message.getAuthor().isBot()){
                continue;
            }
            smashes.add(message);
        }
        return smashes;
    }
}
