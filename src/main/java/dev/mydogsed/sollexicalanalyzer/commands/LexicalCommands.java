package dev.mydogsed.sollexicalanalyzer.commands;

import dev.mydogsed.sollexicalanalyzer.DLAUtil;
import dev.mydogsed.sollexicalanalyzer.Main;
import dev.mydogsed.sollexicalanalyzer.commands.framework.SlashCommandDescription;
import dev.mydogsed.sollexicalanalyzer.commands.framework.SlashCommandExecutor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.time.DayOfWeek;
import java.util.*;
import java.util.List;

public class LexicalCommands {

    public static EmbedBuilder basicEmbed(String title) {
        return new EmbedBuilder()
                .setTitle(title)
                .setAuthor("sol-lexical-analyzer", "https://mydogsed.dev", Main.jda.getSelfUser().getAvatarUrl())
                .setColor(new Color(184, 56, 59))
                .setTimestamp(new Date().toInstant());
    }

    @SlashCommandExecutor("randomkeyboardsmash")
    @SlashCommandDescription("Returns a random one of sol's keyboard smashes")
    public static void randomSmashCommand(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        event.deferReply().queue();
        Message random = randomSmash();
        EmbedBuilder eb = basicEmbed("Random")
                .addField(DLAUtil.getMessageContentRaw(random), random.getJumpUrl(), false);
        hook.editOriginalEmbeds(eb.build()).queue();
    }

    @SlashCommandExecutor("lettercount")
    @SlashCommandDescription("Returns the percent of the top 10 letters in all of sol's keyboard smashes")
    public static void letterCountCommand(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        event.deferReply().queue();

        //
        char[] chars = getCharactersInMessages(
                Main.smashesCache.getMessages().stream().filter((Message message) -> !message.getContentRaw().contains("//")).toList()
        );
        Map<Character, Integer> map = getCharacterOccurencesMap(chars);

        // Convert the keys in the hashmap to a list
        List<Character> keys = new ArrayList<>(map.keySet().stream().toList());

        // Sort that list based on the key's value in the map
        keys.sort(Comparator.comparing(map::get));

        // Build an embed with that information
        EmbedBuilder eb = basicEmbed("Letter Percentages");
        for (int k = keys.size() - 1; k > keys.size() - 10; k--) {
            eb.addField(String.valueOf(keys.get(k)), map.get(keys.get(k)) + "%", false);
        }
        hook.editOriginalEmbeds(eb.build()).queue();
    }

    @SlashCommandExecutor("averagelength")
    @SlashCommandDescription("Gets the average length of each keyboard smash")
    public static void averageLengthCommand(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        event.deferReply().queue();

        List<Message> messages = Main.smashesCache.getMessages().stream().filter((Message message) -> !message.getContentRaw().contains("//")).toList();

        int sum = 0;
        for (Message message : messages) {
            sum += DLAUtil.getMessageContentRaw(message).length();
        }
        double avg = (double) sum / (double) messages.size();

        EmbedBuilder eb = basicEmbed("Average Length")
                .setDescription("The average length of each keyboard smash is " + new DecimalFormat("#.#").format(avg) + " characters");

        hook.editOriginalEmbeds(eb.build()).queue();
    }

    @SlashCommandExecutor("longest")
    @SlashCommandDescription("Returns the longest single keyboard smash in the channel")
    public static void longestCommand(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        event.deferReply().queue();
        List<Message> messages = new ArrayList<>(Main.smashesCache.getMessages().stream().filter((Message message) -> !message.getContentRaw().contains("//")).toList());
        messages.sort(Comparator.comparing(message -> message.getContentRaw().length()));
        Message longestMessage = messages.get(messages.size() - 1);
        EmbedBuilder eb = basicEmbed("Longest")
                .setDescription("The longest single keyboard smash is " + DLAUtil.getMessageContentRaw(longestMessage).length() + " characters")
                .addField(DLAUtil.getMessageContentRaw(longestMessage), longestMessage.getJumpUrl(), false);
        hook.editOriginalEmbeds(eb.build()).queue();
    }

    @SlashCommandExecutor("days")
    @SlashCommandDescription("What day do most keyboard smashes take place on?")
    public static void daysCommand(SlashCommandInteractionEvent event) {
        // Command Boilerplate
        InteractionHook hook = event.getHook();
        event.deferReply().queue();

        // get the keyboard smashes
        List<Message> smashes = Main.smashesCache.getMessages().stream().filter((Message message) -> !message.getContentRaw().contains("//")).toList();

        // Sort all the keyboard smashes by day
        Map<DayOfWeek, List<Message>> days = new HashMap<>();
        for (DayOfWeek day : DayOfWeek.values()) {
            days.put(day, new LinkedList<>());
        }

        for (Message message : smashes) {
            List<Message> list = days.get(message.getTimeCreated().getDayOfWeek());
            list.add(message);
        }

        List<DayOfWeek> keys = new ArrayList<>(days.keySet().stream().toList());
        keys.sort(
                Comparator.comparing(
                                key -> days.get((DayOfWeek) key).size()
                        )
                        .reversed()
        );

        EmbedBuilder eb = basicEmbed("Days");
        for (DayOfWeek day : keys) {
            eb.addField(String.valueOf(day), days.get(day).size() + " keyboard smashes", false);
        }

        hook.editOriginalEmbeds(eb.build()).queue();
    }

    @SlashCommandExecutor("csv")
    @SlashCommandDescription("Upload a CSV file containing the keyboard smashes in the channel")
    public static void csvCommand(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        event.deferReply().setEphemeral(false).queue();
        List<Message> smashes = Main.smashesCache.getMessages().stream().filter((Message message) -> !message.getContentRaw().contains("//")).toList();

        StringBuilder csv = new StringBuilder();
        for (Message message : smashes) {
            csv.append(String.format("%s, %tc%n", DLAUtil.getMessageContentRaw(message), message.getTimeCreated()));
        }

        InputStream stream = new ByteArrayInputStream(csv.toString().getBytes());
        FileUpload upload = FileUpload.fromData(stream, "keyboard_smashes.csv");
        hook.editOriginalAttachments(upload).queue();

    }

    // Counts the number of sol's keyboard smashes
    @SlashCommandExecutor("number")
    @SlashCommandDescription("Count the number of sol's keyboard shmashes")
    public static void numberCommand(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        event.deferReply().queue();
        int number = Main.smashesCache.getMessages()
                .stream().filter((Message message) -> !message.getContentRaw().contains("//")).toList().size();

        EmbedBuilder eb = basicEmbed("Number of Keyboard Smashes")
                .setDescription("sol has archived " + number +  " keyboard smashes.");

        hook.editOriginalEmbeds(eb.build()).queue();
    }

    // Private utility methods for the commands in this class

    // Get all the characters in the list of messages
    private static char[] getCharactersInMessages(List<Message> messages) {
        // Get the messages in the channel and write all of them into one string
        StringBuilder letters = new StringBuilder();
        for (Message message : messages) {
            letters.append(DLAUtil.getMessageContentSanitized(message).toLowerCase());
        }

        // Get a character array from the string and sort it
        char[] arr = letters.toString().replaceAll("\\s", "").toCharArray();
        Arrays.sort(arr);
        return arr;
    }

    /*
    Returns a Map<Character, Integer> where the integer is the number of occurrences of that character
    in the char[] parameter.
     */
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

            if (!(letter == ' ')) {
                map.put(letter, (int) ((((double) letterCount) / arr.length) * 100));
            }
        }
        return map;
    }

    // Get a random keyboard smash
    public static Message randomSmash() {
        List<Message> filtered = Main.smashesCache.getMessages()
                .stream().filter((Message message) -> !message.getContentRaw().contains("//")).toList();
        return filtered.get(new Random().nextInt(filtered.size()));
    }
}
