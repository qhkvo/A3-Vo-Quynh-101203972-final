package game;

import java.io.PrintWriter;
import java.util.*;

public class Player {
    private List<Card> hand;
    private int totalShields;
    private int previousStageValue;
    private List<Card> selectedAttackCards;

    public Player() {
        // Initialize an empty hand
        hand = new ArrayList<>();
        this.totalShields = 0;
        this.previousStageValue = 0;
        this.selectedAttackCards = new ArrayList<>();
    }

    public void addCardToHand(Card card) {
        hand.add(card);
    }

    // Display only the newly drawn cards
    public void displayNewlyDrawnCards(PrintWriter printWriter, int initialHandSize) {
        printWriter.println("Newly drawn cards:");
        for (int i = initialHandSize; i < hand.size(); i++) {
            printWriter.println((i + 1) + ". " + hand.get(i).toString());
        }
    }

    // Display the player's hand
    public void displayHand(PrintWriter printWriter) {
        printWriter.println("Hand:");
        int cardNumber = 1;
        for (Card card : hand) {
            printWriter.println(cardNumber + ". " + card.toString());
            cardNumber++;
        }
    }

    public void displayUpdatedHand(PrintWriter printWriter) {
        List<String> cards = new ArrayList<>();
        for (Card card : hand) {
            cards.add(card.toString());
        }
        printWriter.println("Your current hand: " + cards);
    }

    // Sort cards in the player's hand (F -> W (S before H))
    public void sortHand() {
        hand.sort((c1, c2) -> {
            if (c1.getType().startsWith("F") && !c2.getType().startsWith("F")) return -1;
            if (!c1.getType().startsWith("F") && c2.getType().startsWith("F")) return 1;
            if (c1.getType().startsWith("F") && c2.getType().startsWith("F")) return Integer.compare(c1.getValue(), c2.getValue());
            if (c1.getType().startsWith("S") && c2.getType().startsWith("H")) return -1;
            if (c1.getType().startsWith("H") && c2.getType().startsWith("S")) return 1;
            return Integer.compare(c1.getValue(), c2.getValue());
        });
    }

    public void loseShields(int shieldsLost) {
        // Ensure the player doesn't lose more shields than they have
        totalShields = Math.max(0, totalShields - shieldsLost);
    }

    public void addShields(int shieldsGained) {
        totalShields += shieldsGained;
    }

    public void setUpQuest(PrintWriter printWriter, Scanner input, List<List<Card>> stages, int stageCount) {
        Set<Integer> globalSelectedCardPositions = new HashSet<>();  // Global set to track selected card positions across all stages
        List<Card> discardedCards = new ArrayList<>();

        for (int i = 1; i <= stageCount; i++) {
            List<Card> stageCards = new ArrayList<>();
            Set<Integer> selectedCardPositions = new HashSet<>();  // Set to track selected card positions for the current stage

            printWriter.println("---> SETTING UP STAGE " + i + " OF " + stageCount + " <---");
            printWriter.flush();
            sortHand();
            displayHand(printWriter);

            boolean validStage = false;

            while (!validStage) {
                printWriter.println("Enter the position of a card to include in this stage, or type 'Quit' to finish the stage:");
                printWriter.flush();

                String inputLine = input.nextLine().trim();

                if (inputLine.equalsIgnoreCase("Quit")) {
                    if (stageCards.isEmpty()) {
                        printWriter.println("A stage cannot be empty. Please select at least one card.");
                    } else if (!validateStage(stageCards)) {
                        printWriter.println("Invalid stage: Each stage must contain exactly 1 Foe card and zero or more unique Weapon cards.");
                    } else {
                        int currentStageValue = calculateStageValue(stageCards);
                        printWriter.println("Total stage value: " + currentStageValue);

                        if (currentStageValue <= previousStageValue) {
                            printWriter.println("Insufficient value for this stage. Stage value must be higher than the previous stage.");
                        } else {
                            stages.add(stageCards);
                            previousStageValue = currentStageValue;
                            printWriter.println("Stage " + i + " successfully set with the following cards:");

                            for (Card card : stageCards) {
                                printWriter.println(card.toString());
                            }
                            discardedCards.addAll(stageCards);
                            validStage = true;  // Move to the next stage
                        }
                    }
                } else {
                    try {
                        int cardPosition = Integer.parseInt(inputLine) - 1;

                        if (cardPosition < 0 || cardPosition >= hand.size()) {
                            printWriter.println("Invalid input. Please enter a valid card position.");
                            continue;
                        }

                        // Check if the card has already been selected for any stage
                        if (selectedCardPositions.contains(cardPosition) || globalSelectedCardPositions.contains(cardPosition)) {
                            printWriter.println("This card has already been selected for this stage or another stage. Please choose a different card.");
                            continue;
                        }

                        Card selectedCard = hand.get(cardPosition);

                        if (isInvalidCardForStage(selectedCard, stageCards)) {
                            printWriter.println("Invalid card: You can only have one Foe and no repeated Weapon cards in a stage.");
                        } else {
                            stageCards.add(selectedCard);
                            selectedCardPositions.add(cardPosition);  // Track the selected card position for this stage
                            globalSelectedCardPositions.add(cardPosition);  // Track the selected card position across all stages
                            printWriter.println("Added " + selectedCard + " to stage " + i);
                        }
                    } catch (NumberFormatException e) {
                        printWriter.println("Invalid input. Please enter a valid card position.");
                    }
                }

            }
        }

        hand.removeAll(discardedCards);

        printWriter.println("Quest setup complete.");
        printWriter.println();
        printWriter.flush();
        previousStageValue = 0;
    }



    // Calculate the total value of a stage (Foe + Weapon values)
    private int calculateStageValue(List<Card> stageCards) {
        return stageCards.stream().mapToInt(Card::getValue).sum();
    }

    // Validate if a selected card is valid for the current stage
    private boolean isInvalidCardForStage(Card selectedCard, List<Card> stageCards) {
        if (selectedCard.getType().startsWith("F")) {
            // Ensure only one Foe per stage
            for (Card card : stageCards) {
                if (card.getType().startsWith("F")) {
                    return true;  // Invalid, because a Foe card already exists
                }
            }
        } else if (selectedCard.getType().matches("[DSHBEL].*")) {
            // Check if there's a repeated Weapon card in this stage
            for (Card card : stageCards) {
                if (card.getType().equals(selectedCard.getType())) {
                    return true;  // Duplicate Weapon card
                }
            }
        }
        return false;  // Valid card for the stage
    }

    // Validate that the stage contains exactly 1 Foe card and unique Weapon cards
    private boolean validateStage(List<Card> stageCards) {
        int foeCount = 0;
        Set<String> weaponTypes = new HashSet<>();

        for (Card card : stageCards) {
            if (card.getType().startsWith("F")) {
                foeCount++;
            } else if (card.getType().matches("[DSHBEL].*")) {
                if (!weaponTypes.add(card.getType())) {
                    return false;  // Duplicate Weapon card found
                }
            }
        }

        // Ensure there is exactly 1 Foe card and zero or more unique Weapon cards
        return foeCount == 1 && weaponTypes.size() == (stageCards.size() - 1);
    }

    public boolean canSponsorQuest(int stageCount) {
        long foeCardCount = hand.stream()
                .filter(card -> card.getType().startsWith("F"))
                .count();

        // Check if there are enough Foe cards for the number of stages
        return foeCardCount >= stageCount;
    }

    // Add a card to the selected attack cards
    public void addToAttack(Card card) {
        selectedAttackCards.add(card);
    }
    public void removeHand(){
        hand.clear();
    }

    // Clear the selected attack cards after each stage
    public void clearAttackCards() {
        selectedAttackCards.clear();
    }



    // Getters
    public int getHandSize() {
        return hand.size();
    }
    public List<Card> getHand() {
        return hand;
    }
    public int getTotalShield() {
        return totalShields;
    }

    // Calculate the player's attack value based on the selected attack cards
    public int getAttackValue() {
        return selectedAttackCards.stream().mapToInt(Card::getValue).sum();
    }

    public List<Card> getSelectedAttackCards() {
        return selectedAttackCards;
    }

}
