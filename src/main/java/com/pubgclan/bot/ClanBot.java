package com.pubgclan.bot;

import com.pubgclan.service.ParticipationService;
import com.pubgclan.service.UserService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.time.LocalDate;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class ClanBot extends ListenerAdapter {

    private final ParticipationService participationService;
    private final UserService userService;
    private JDA jda;
    private volatile boolean isInitialized = false;

    @Value("${discord.bot.token:}")
    private String botToken;

    @Value("${discord.channel.id:}")
    private String channelId;

    @Value("${discord.guild.id:}")
    private String guildId;

    public ClanBot(ParticipationService participationService, UserService userService) {
        this.participationService = participationService;
        this.userService = userService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startBot() {
        // Prevent multiple initializations
        if (isInitialized) {
            log.warn("Discord bot already initialized. Skipping startup.");
            return;
        }

        if (botToken == null || botToken.isBlank()) {
            log.warn("Discord bot token not configured. Bot will not start.");
            return;
        }

        isInitialized = true;

        Thread botThread = new Thread(() -> {
            try {
                jda = JDABuilder.createDefault(botToken)
                        .enableIntents(
                                GatewayIntent.GUILD_MESSAGES,
                                GatewayIntent.MESSAGE_CONTENT,
                                GatewayIntent.GUILD_MEMBERS
                        )
                        .addEventListeners(this)
                        .build();
                jda.awaitReady();
                log.info("Discord bot started successfully. Bot tag: {}", jda.getSelfUser().getAsTag());
                syncGuildMembers();
                syncHistoricalMessages();
            } catch (Exception e) {
                log.error("Failed to start Discord bot: {}", e.getMessage());
                isInitialized = false;
            }
        }, "discord-bot-thread");
        botThread.setDaemon(true);
        botThread.start();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        // Skip processing if bot is not fully initialized
        if (!isInitialized || jda == null) {
            return;
        }

        if (event.getAuthor().isBot()) {
            return;
        }

        // Only process messages from the configured channel
        if (channelId != null && !channelId.isBlank() &&
                !event.getChannel().getId().equals(channelId)) {
            return;
        }

        // Only process messages from the configured guild
        if (!event.isFromGuild()) {
            return;
        }

        if (guildId != null && !guildId.isBlank() &&
                !event.getGuild().getId().equals(guildId)) {
            return;
        }

        Message message = event.getMessage();
        List<User> mentionedUsers = message.getMentions().getUsers();

        if (mentionedUsers.isEmpty()) {
            return;
        }

        log.info("Processing participation message from {} with {} mentions",
                event.getAuthor().getAsTag(), mentionedUsers.size());

        for (User mentionedUser : mentionedUsers) {
            if (mentionedUser.isBot()) {
                continue;
            }

            String userId = mentionedUser.getId();
            String username = mentionedUser.getName();

            // Try to get display name from guild member
            try {
                Member member = event.getGuild().getMember(mentionedUser);
                if (member != null && member.getNickname() != null) {
                    username = member.getNickname();
                }
            } catch (Exception e) {
                log.debug("Could not get member nickname for user {}: {}", userId, e.getMessage());
            }

            try {
                participationService.recordParticipationOnDate(userId, username, LocalDate.now(ZoneId.of("Asia/Seoul")), message.getId());
                log.info("Recorded participation for user: {} ({})", username, userId);
            } catch (Exception e) {
                log.error("Failed to record participation for user {}: {}", userId, e.getMessage());
            }
        }
    }

    private void syncGuildMembers() {
        if (guildId == null || guildId.isBlank()) return;

        net.dv8tion.jda.api.entities.Guild guild = jda.getGuildById(guildId);
        if (guild == null) {
            log.warn("Guild {} not found for member sync", guildId);
            return;
        }

        log.info("Starting guild member sync for: {}", guild.getName());

        guild.loadMembers().onSuccess(members -> {
            int count = 0;
            for (Member member : members) {
                if (member.getUser().isBot()) continue;

                String discordId = member.getId();
                String username = member.getNickname() != null
                        ? member.getNickname()
                        : member.getUser().getName();
                String avatar = member.getUser().getAvatarId(); // hash only

                try {
                    userService.createOrUpdateUser(discordId, username, avatar);
                    count++;
                } catch (Exception e) {
                    log.error("Failed to register member {}: {}", username, e.getMessage());
                }
            }
            log.info("Guild member sync complete. {} members registered.", count);
        });
    }

    private void syncHistoricalMessages() {
        if (channelId == null || channelId.isBlank()) return;

        TextChannel channel = jda.getTextChannelById(channelId);
        if (channel == null) {
            log.warn("Channel {} not found for history sync", channelId);
            return;
        }

        log.info("Starting historical message sync for channel: {}", channel.getName());

        channel.getIterableHistory().takeAsync(2000).thenAccept(messages -> {
            int recorded = 0;
            int totalMessages = messages.size();
            int withMentions = 0;
            
            log.info("==== SYNC START ====");
            log.info("Total messages fetched: {}", totalMessages);
            
            for (Message message : messages) {
                if (message.getAuthor().isBot()) continue;
                List<User> mentions = message.getMentions().getUsers();
                if (mentions.isEmpty()) continue;
                
                withMentions++;

                LocalDate messageDate = message.getTimeCreated()
                        .atZoneSameInstant(ZoneId.of("Asia/Seoul"))
                        .toLocalDate();

                log.info("Message from {}: {} mentions on {} (UTC: {})", 
                    message.getAuthor().getName(), 
                    mentions.size(), 
                    messageDate,
                    message.getTimeCreated());

                for (User mentioned : mentions) {
                    if (mentioned.isBot()) continue;
                    String userId = mentioned.getId();
                    String username = mentioned.getName();

                    try {
                        Member member = message.getGuild().getMember(mentioned);
                        if (member != null && member.getNickname() != null) {
                            username = member.getNickname();
                        }
                    } catch (Exception ignored) {}

                    try {
                        participationService.recordParticipationOnDate(userId, username, messageDate, message.getId());
                        recorded++;
                        log.info("  -> Recorded: {} on {}", username, messageDate);
                    } catch (Exception e) {
                        log.error("Failed to sync participation for {}: {}", username, e.getMessage());
                    }
                }
            }
            log.info("==== SYNC END ====");
            log.info("Messages with @mentions: {}", withMentions);
            log.info("Historical sync complete. {} participation records processed.", recorded);
        }).exceptionally(e -> {
            log.error("Failed to fetch message history: {}", e.getMessage());
            e.printStackTrace();
            return null;
        });
    }

    public void shutdown() {
        if (jda != null) {
            jda.shutdown();
            log.info("Discord bot shut down.");
        }
    }
}
