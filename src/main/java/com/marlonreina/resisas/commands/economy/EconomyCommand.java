package com.marlonreina.resisas.commands.economy;

import com.marlonreina.resisas.commands.Command;
import com.marlonreina.resisas.commands.CommandContext;
import com.marlonreina.resisas.dto.DailyRewardResult;
import com.marlonreina.resisas.model.EconomyAccount;
import com.marlonreina.resisas.service.EconomyService;
import com.marlonreina.resisas.utils.EmbedUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;

import java.awt.Color;
import java.time.Duration;
import java.util.List;
import java.util.Locale;

public class EconomyCommand implements Command {

    private static final String CURRENCY = "hexacoins";

    private final EconomyService economyService;
    private final String defaultAction;

    public EconomyCommand(EconomyService economyService, String defaultAction) {
        this.economyService = economyService;
        this.defaultAction = defaultAction;
    }

    @Override
    public void execute(CommandContext context) {
        String action = resolveAction(context);

        switch (action) {
            case "balance" -> showBalance(context);
            case "daily" -> claimDaily(context);
            case "pay" -> pay(context);
            case "leaderboard" -> showLeaderboard(context);
            default -> showMenu(context);
        }
    }

    private String resolveAction(CommandContext context) {
        String[] args = context.getArgs();
        if (!"menu".equals(defaultAction)) {
            return defaultAction;
        }
        if (args.length == 0) {
            return "menu";
        }
        return args[0].toLowerCase(Locale.ROOT);
    }

    private void showMenu(CommandContext context) {
        EmbedBuilder embed = EmbedUtil.info("Economia");
        embed.setDescription("Administra tus monedas dentro del servidor.");
        embed.addField("Balance", "`" + context.usage("economy balance [@usuario]") + "`", false);
        embed.addField("Daily", "`" + context.usage("economy daily") + "`", false);
        embed.addField("Pagar", "`" + context.usage("economy pay @usuario <cantidad>") + "`", false);
        embed.addField("Ranking", "`" + context.usage("economy leaderboard") + "`", false);
        context.getEvent().getChannel().sendMessageEmbeds(embed.build()).queue();
    }

    private void showBalance(CommandContext context) {
        Member target = context.getEvent().getMessage().getMentions().getMembers().stream()
                .findFirst()
                .orElse(context.getEvent().getMember());

        if (target == null) {
            context.getEvent().getChannel().sendMessageEmbeds(
                    EmbedUtil.error("No pude identificar el usuario.").build()
            ).queue();
            return;
        }

        EconomyAccount account = economyService.getOrCreate(context.getEvent().getGuild().getId(), target.getId());
        context.getEvent().getChannel().sendMessageEmbeds(
                EmbedUtil.info("Balance")
                        .setDescription(target.getAsMention() + " tiene **"
                                + formatAmount(account.getBalance()) + "** " + CURRENCY + ".")
                        .build()
        ).queue();
    }

    private void claimDaily(CommandContext context) {
        DailyRewardResult result = economyService.claimDaily(context.getEvent().getGuild().getId(),
                context.getEvent().getAuthor().getId());

        if (!result.claimed()) {
            context.getEvent().getChannel().sendMessageEmbeds(
                    EmbedUtil.error("Ya reclamaste tu recompensa diaria.")
                            .addField("Disponible en", formatDuration(result.remaining()), false)
                            .addField("Balance", formatAmount(result.balance()) + " " + CURRENCY, false)
                            .build()
            ).queue();
            return;
        }

        context.getEvent().getChannel().sendMessageEmbeds(
                EmbedUtil.success("Daily reclamado")
                        .setDescription("Ganaste **" + formatAmount(result.reward()) + "** " + CURRENCY + ".")
                        .addField("Nuevo balance", formatAmount(result.balance()) + " " + CURRENCY, false)
                        .build()
        ).queue();
    }

    private void pay(CommandContext context) {
        String[] args = context.getArgs();
        int amountIndex = "menu".equals(defaultAction) ? 2 : 1;

        if (args.length <= amountIndex || context.getEvent().getMessage().getMentions().getMembers().isEmpty()) {
            context.getEvent().getChannel().sendMessageEmbeds(
                    EmbedUtil.usage(context.usage("economy pay @usuario <cantidad>")).build()
            ).queue();
            return;
        }

        Member receiver = context.getEvent().getMessage().getMentions().getMembers().get(0);
        if (receiver.getUser().isBot() || receiver.getId().equals(context.getEvent().getAuthor().getId())) {
            context.getEvent().getChannel().sendMessageEmbeds(
                    EmbedUtil.error("Elige otro miembro del servidor para pagarle.").build()
            ).queue();
            return;
        }

        long amount = parseAmount(args[amountIndex]);
        if (amount <= 0) {
            context.getEvent().getChannel().sendMessageEmbeds(
                    EmbedUtil.error("La cantidad debe ser mayor a 0.").build()
            ).queue();
            return;
        }

        try {
            EconomyAccount sender = economyService.transfer(context.getEvent().getGuild().getId(),
                    context.getEvent().getAuthor().getId(), receiver.getId(), amount);
            context.getEvent().getChannel().sendMessageEmbeds(
                    EmbedUtil.success("Pago enviado")
                            .setDescription("Enviaste **" + formatAmount(amount) + "** "
                                    + CURRENCY + " a " + receiver.getAsMention() + ".")
                            .addField("Tu balance", formatAmount(sender.getBalance()) + " " + CURRENCY, false)
                            .build()
            ).queue();
        } catch (IllegalArgumentException e) {
            context.getEvent().getChannel().sendMessageEmbeds(
                    EmbedUtil.error(e.getMessage()).build()
            ).queue();
        }
    }

    private void showLeaderboard(CommandContext context) {
        List<EconomyAccount> accounts = economyService.leaderboard(context.getEvent().getGuild().getId());
        if (accounts.isEmpty()) {
            context.getEvent().getChannel().sendMessageEmbeds(
                    EmbedUtil.error("Todavia no hay balances en este servidor.").build()
            ).queue();
            return;
        }

        StringBuilder board = new StringBuilder();
        for (int i = 0; i < accounts.size(); i++) {
            EconomyAccount account = accounts.get(i);
            board.append("`#")
                    .append(i + 1)
                    .append("` <@")
                    .append(account.getUserId())
                    .append("> - **")
                    .append(formatAmount(account.getBalance()))
                    .append("** ")
                    .append(CURRENCY)
                    .append(System.lineSeparator());
        }

        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(new Color(EmbedUtil.HEXA_COLOR));
        embed.setTitle("Ranking de economia");
        embed.setDescription(board.toString());
        embed.setFooter(EmbedUtil.FOOTER);
        context.getEvent().getChannel().sendMessageEmbeds(embed.build()).queue();
    }

    private long parseAmount(String rawAmount) {
        try {
            return Long.parseLong(rawAmount);
        } catch (NumberFormatException e) {
            return -1L;
        }
    }

    private String formatAmount(long amount) {
        return String.format(Locale.US, "%,d", amount);
    }

    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.minusHours(hours).toMinutes();
        return hours + "h " + minutes + "m";
    }
}
