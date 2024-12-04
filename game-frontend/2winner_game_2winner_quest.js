// Scenario 2: 2winner_game_2winner_quest




p1.addCardToHand(
        p1.addCardToHand( new Card("F5", 5));
        p1.addCardToHand( new Card("F5", 5));
        p1.addCardToHand( new Card("F10",10));
        p1.addCardToHand( new Card("F10",10));
        p1.addCardToHand( new Card("F15", 15));
        p1.addCardToHand( new Card("F15", 15));
        p1.addCardToHand( new Card("D5", 5));
        p1.addCardToHand( new Card("H10", 10));
        p1.addCardToHand( new Card("H10", 10));
        p1.addCardToHand( new Card("B15", 15));
        p1.addCardToHand( new Card("B15", 15));
        p1.addCardToHand( new Card("L20", 20));

        p2.addCardToHand(new Card("F40", 40));
        p2.addCardToHand(new Card("F50", 50));
        p2.addCardToHand(new Card("S10", 10));
        p2.addCardToHand(new Card("S10", 10));
        p2.addCardToHand(new Card("S10", 10));
        p2.addCardToHand(new Card("H10", 10));
        p2.addCardToHand(new Card("H10", 10));
        p2.addCardToHand(new Card("B15", 15));
        p2.addCardToHand(new Card("B15", 15));
        p2.addCardToHand(new Card("L20",20));
        p2.addCardToHand(new Card("L20", 20));
        p2.addCardToHand(new Card("E30",30));

        p3.addCardToHand(new Card("F5", 5));
        p3.addCardToHand(new Card("F5", 5));
        p3.addCardToHand(new Card("F5", 5));
        p3.addCardToHand(new Card("F5", 5));
        p3.addCardToHand(new Card("D5",  5));
        p3.addCardToHand(new Card("D5", 5));
        p3.addCardToHand(new Card("D5", 5));
        p3.addCardToHand(new Card("H10", 10));
        p3.addCardToHand(new Card("H10", 10));
        p3.addCardToHand(new Card("H10", 10));
        p3.addCardToHand(new Card("H10", 10));
        p3.addCardToHand(new Card("H10", 10));

        p2.addCardToHand(new Card("F50", 50));
        p2.addCardToHand(new Card("F70", 70));
        p2.addCardToHand(new Card("S10", 10));
        p2.addCardToHand(new Card("S10", 10));
        p2.addCardToHand(new Card("S10", 10));
        p2.addCardToHand(new Card("H10", 10));
        p2.addCardToHand(new Card("H10", 10));
        p2.addCardToHand(new Card("B15", 15));
        p2.addCardToHand(new Card("B15", 15));
        p2.addCardToHand(new Card("L20", 20));
        p2.addCardToHand(new Card("L20",20));
        p2.addCardToHand(new Card("E30",30));


String input = ["yes\n" + // P1 sponsors the quest
                "1\nquit\n" + // Stage 1 (foe only)
                "2\n7\nquit\n" + // Stage 2
                "3\n8\nquit\n" + // Stage 3
                "4\n10\nquit\n" + // Stage 4
                "c\n" + "1\n" + "c\n" + "1\n" + "c\n" + "1\n" + // Players accept quest and trim to 12 cards
                "4\n" + "quit\n" + "quit\n" + "3\n" + "quit\n" + // P2,3,4 set up attack
                "c\n" + "c\n" + // P2,4 continue to stage 2
                "1\n" + "quit\n" + "1\n" + "quit\n" + // P2,4 set up attack stage 2
                "c\n" + "c\n" + // P2,4 continue to stage 3
                "3\n" + "2\n" + "quit\n" + "3\n" + "2\n" + "quit\n" + // P2,4 set up attack stage 3
                "c\n" + "c\n" +  // P2,4 continue to stage 4
                "1\n" + "1\n" + "quit\n" + "1\n" + "1\n" + "quit\n" +  // P2,4 set up attack stage 4
                "1\n" + "1\n" + "1\n" + "1\n" +  // Sponsor trims to 12 cards

                // Next Quest
                "\nno\n" + "yes\n" +    // P2 declines, P3 sponsors
                "1\n" + "quit\n" +              // Stage 1:
                "2\n" + "5\n" + "quit\n" +              // Stage 2:
                "3\n" + "8\n" + "quit\n" +              // Stage 3:
                "w\n" + "c\n" + "c\n" + // P2, 4 participate stage 1
                "1\n" + "quit\n" + "1\n" + "quit\n" + // P2: H10, P4: H10 set up attack Stage 1
                "c\n" + "c\n" + // P2,4 continue to stage 2
                "1\n" + "quit\n" + "1\n" + "quit\n" + // P2: B15,4: B15 set up attack stage 2
                "c\n" + "c\n" + // P2,4 continue to stage 3
                "3\n" + "quit\n" + "3\n" + "quit\n" + // P2: L20,P4: L20 set up attack stage 3
                "1\n" + "2\n" + "2\n" ;  // Sponsor trims to 12 cards;
                ]
