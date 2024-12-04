package game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {
    private String type;
    private List<Card> cards;

    public Deck(String type, int size) {
        this.type = type;
        this.cards = new ArrayList<>();

        // Initialize adventure and event deck with specific cards
        if (type.equals("Adventure")) {
            initializeAdventureDeck();
        } else if (type.equals("Event")) {
            initializeEventDeck();
        }
    }

    public void addOnTop(List<Card> list) {
        cards.addAll(0,list);
    }

    // Initialize Adventure Deck
    private void initializeAdventureDeck() {
        // Add 50 Foe cards
        addCards("F5", 5, 8);  // Add 8 F5 cards
        addCards("F10", 10, 7); // Add 7 F10 cards
        addCards("F15", 15, 8);
        addCards("F20", 20, 7);
        addCards("F25", 25, 7);
        addCards("F30", 30, 4);
        addCards("F35", 35, 4);
        addCards("F40", 40, 2);
        addCards("F50", 50, 2);
        addCards("F70", 70, 1);

        // Add 50 Weapon cards
        addCards("D5", 5, 6);  // Add 6 Dagger (D) cards
        addCards("H10", 10, 12);
        addCards("S10", 10, 16);
        addCards("B15", 15, 8);
        addCards("L20", 20, 6);
        addCards("E30", 30, 2);


    }

    // Initialize Event Deck
    private void initializeEventDeck() {
        // Add 12 quest cards
        addCards("Q2", 2, 3);
        addCards("Q3", 3, 4);
        addCards("Q4", 4, 3);
        addCards("Q5", 5, 2);

        addCards("Plague", 0, 1);
        addCards("Queen’s Favor", 0, 2);
        addCards("Prosperity", 0, 2);
    }

    // Helper method to add specific number of cards to the deck
    private void addCards(String cardType, int value, int count) {
        for (int i = 0; i < count; i++) {
            cards.add(new Card(cardType, value));
        }
    }

    // Shuffle the deck
    public void shuffle() {
        Collections.shuffle(cards);
    }

    // Reshuffle from discard pile back into the deck
    public void reshuffle(List<Card> discardPile) {
        if (!discardPile.isEmpty()) {
            System.out.println(type + " deck is empty. Reshuffling from discard pile.");
            cards.addAll(discardPile);
            discardPile.clear();
            shuffle();
            System.out.println("Deck reshuffled from the discard pile.");
        } else {
            System.out.println("No more cards in the discard pile to reshuffle.");
        }
    }

    // Return the size of the deck
    public int size() {
        return cards.size();
    }

    // Draw a card from the deck
    public Card drawCard() {
        // Draw the top card in cards list
        if (!cards.isEmpty()) {
            return cards.remove(0);
        }
        //If the deck is empty
        return null;
    }

    public List<Card> getCards() {
        return cards;
    }
    @Override
    public String toString() {
        StringBuilder deckString = new StringBuilder();
        for (Card card : cards) {
            deckString.append(card.toString()).append("\n");
        }
        return deckString.toString();
    }

}
