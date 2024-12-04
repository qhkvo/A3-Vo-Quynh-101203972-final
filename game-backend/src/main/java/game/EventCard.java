package game;

import java.io.PrintWriter;
import java.util.List;

public class EventCard extends Card {
    public EventCard(String type, int value) {
        super(type, value);
    }

    public boolean isQuest() {
        return getType().matches("Q[2-5]");  // Quest cards follow "Q2" to "Q5"
    }

    // Apply events of the event card
    public void applyEvent(Player currentPlayer, List<Player> players, Deck adventureDeck, PrintWriter printWriter) {
        switch (getType()) {
            case "Plague":
                applyPlagueEvent(currentPlayer, printWriter);
                break;
            case "Queen’s Favor":
                applyQueensFavorEvent(currentPlayer, adventureDeck, printWriter);
                break;
            case "Prosperity":
                applyProsperityEvent(currentPlayer, players, adventureDeck, printWriter);
                break;
        }
    }

    // Plague event
    private void applyPlagueEvent(Player player, PrintWriter printWriter) {
        player.loseShields(2);
        printWriter.println("Player loses 2 shields. Total shields: " + player.getTotalShield());
        printWriter.println();
    }

    // Queen's Favor event
    private void applyQueensFavorEvent(Player player, Deck adventureDeck, PrintWriter printWriter) {
        int initialHandSize = player.getHandSize();

        player.addCardToHand(adventureDeck.drawCard());
        player.addCardToHand(adventureDeck.drawCard());

        printWriter.println("Player draws 2 adventure cards.");
        player.displayNewlyDrawnCards(printWriter, initialHandSize);
        printWriter.println();

        player.sortHand();
        player.displayHand(printWriter);
        printWriter.println();
    }

    // Prosperity event
    private void applyProsperityEvent(Player currentPlayer, List<Player> players, Deck adventureDeck, PrintWriter printWriter) {
        int initialHandSize = currentPlayer.getHandSize();
        int currentPlayerIndex = players.indexOf(currentPlayer) + 1;

        for (Player player : players) {
            player.addCardToHand(adventureDeck.drawCard());
            player.addCardToHand(adventureDeck.drawCard());
        }

        printWriter.println("All players each draws 2 adventure cards.");
        printWriter.println("P" + currentPlayerIndex + "'s hand update:");

        currentPlayer.displayNewlyDrawnCards(printWriter, initialHandSize);
        printWriter.println();

        currentPlayer.sortHand();
        currentPlayer.displayHand(printWriter);
        printWriter.println();
    }

    // Resolve Quest event
    public void resolveQuest(EventCard card) {
        System.out.println("Resolving Quest will be handle in RESP-10: ");
    }
}
