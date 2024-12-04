package game;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class EventCard extends Card {
    private List<String> messages = new ArrayList<>();
    public EventCard(String type, int value) {
        super(type, value);
    }

    public boolean isQuest() {
        return getType().matches("Q[2-5]");  // Quest cards follow "Q2" to "Q5"
    }

    // Apply events of the event card
    public String applyEvent(Player currentPlayer, List<Player> players, Deck adventureDeck) {
       // messages.clear();
        switch (getType()) {
            case "Plague":
                return applyPlagueEvent(currentPlayer);
            case "Queen’s Favor":
                return applyQueensFavorEvent(currentPlayer, adventureDeck);
            case "Prosperity":
                return applyProsperityEvent(currentPlayer, players, adventureDeck);
            default:
                messages.add("Unknown event type: " + getType());
                return String.join("\n", messages);
        }
    }

    // Plague event
    private String applyPlagueEvent(Player player) {
        player.loseShields(2);
        messages.add("Player loses 2 shields. Total shields: " + player.getTotalShield());
        return String.join("\n", messages);
    }

    // Queen's Favor event
    private String applyQueensFavorEvent(Player player, Deck adventureDeck) {
        int initialHandSize = player.getHandSize();

        player.addCardToHand(adventureDeck.drawCard());
        player.addCardToHand(adventureDeck.drawCard());

         messages.add("Player draws 2 adventure cards.");
         player.displayNewlyDrawnCards(initialHandSize);

        player.sortHand();
        messages.addAll(player.displayHand());
        return String.join("\n", messages);
    }

    // Prosperity event
    private String applyProsperityEvent(Player currentPlayer, List<Player> players, Deck adventureDeck) {
        int initialHandSize = currentPlayer.getHandSize();
        int currentPlayerIndex = players.indexOf(currentPlayer) + 1;

        for (Player player : players) {
            player.addCardToHand(adventureDeck.drawCard());
            player.addCardToHand(adventureDeck.drawCard());
        }

         messages.add("All players each draws 2 adventure cards.");
         messages.add("P" + currentPlayerIndex + "'s hand update:");

         currentPlayer.displayNewlyDrawnCards(initialHandSize);

        currentPlayer.sortHand();
        messages.addAll(currentPlayer.displayHand());
        return String.join("\n", messages);
    }
}
