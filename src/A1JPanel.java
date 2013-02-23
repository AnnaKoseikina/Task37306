/*
 * Name
 * Date
 * Class for game panel
 */

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class A1JPanel extends JPanel implements MouseListener, KeyListener {
	// constants
	public static final int CARDS_IN_EACH_SUIT = A1Constants.CARDS_IN_EACH_SUIT;
	private static final int TOTAL_NUMBER_OF_CARDS = A1Constants.TOTAL_NUMBER_OF_CARDS;

	private static final int NUMBER_OF_ROWS = A1Constants.NUMBER_OF_ROWS;
	private static final int NUMBER_OF_COLS = A1Constants.NUMBER_OF_COLS;

	private static final Color BACKGROUND_COLOUR = new Color(233, 0, 211);
	private static final int CARD_DISPLAY_LEFT = A1Constants.CARD_DISPLAY_LEFT;
	// arraylist of card stack and 2D array for table cards
	private ArrayList<Card> cardStack;
	private Card[][] cards;
	// user's card
	private Card userCard;
	// card which is face down
	// and covers the card stack
	private Card aFaceDownCard;
	// size of card
	private int cardWidth;
	private int cardHeight;
	// number of removed cards
	private int numberRemoved;
	// variables for available moves and remaining table cards
	private boolean noMoreTableCards;
	private boolean noMoreAvailableMoves;

	// variables used for scoring the game
	private int userScore = 0;
	private int pointsToAdd = 1;

	public A1JPanel() { // constructor of panel
		setBackground(BACKGROUND_COLOUR);
		loadAllCardImagesAndSetUpCardDimensions();

		addKeyListener(this);
		addMouseListener(this);

		reset();
	}

	private void reset() { // create a new game
		int randomPosition;
		cardStack = createTheFullPack();
		cards = getRandomTableCards(cardStack);

		setUpVisibleRowOfCards(cards);

		randomPosition = (int) (Math.random() * cardStack.size());
		userCard = cardStack.remove(randomPosition);
		setUpCardPosition(userCard, cardWidth * 8, cardHeight * 5, true);

		aFaceDownCard = new Card(0, 0);
		setUpCardPosition(aFaceDownCard, cardWidth * 6, cardHeight * 5, false);

		numberRemoved = 0;

		noMoreTableCards = false;
		noMoreAvailableMoves = false;
		userScore = 0;
		pointsToAdd = 1;

	}

	private void setUpCardPosition(Card card, int x, int y, boolean isFaceUp) { // set
																				// up
																				// card
																				// position
		card.setCardArea(x, y, cardWidth, cardHeight);
		card.setIsFaceUp(isFaceUp);
	}

	// Handle KeyEvents

	public void keyPressed(KeyEvent e) {
		if (e.getKeyChar() == 'n' || e.getKeyChar() == 'N') // key for new game
			reset();

		repaint();

		if (e.getKeyChar() == 's' || e.getKeyChar() == 'S') { // key for save a
																// game
			writeToFile();
		}
		if (e.getKeyChar() == 'l' || e.getKeyChar() == 'L') // key for load a
															// game
			loadFromFile();
	}

	public void keyReleased(KeyEvent e) {
	}

	public void keyTyped(KeyEvent e) {
	}

	// ---------------------------------------------------------------------
	// Handle MouseEvents
	// ---------------------------------------------------------------------
	public void mousePressed(MouseEvent e) { // removing of faced up table card
												// or facing up card from stack
		Card selectedCard;
		int selectedCardRow;
		int selectedCardCol;
		int randomPosition;

		if (noMoreTableCards || noMoreAvailableMoves) { // if the game ends
			return;
		}

		Point pressPt = e.getPoint();
		Point rowColOfSelectedCard = getRowColOfSelectedCard(pressPt);

		if (rowColOfSelectedCard != null) { // click on table card
			selectedCardRow = rowColOfSelectedCard.x;
			selectedCardCol = rowColOfSelectedCard.y;
			selectedCard = cards[selectedCardRow][selectedCardCol];

			if (selectedCard.getIsFaceUp()
					&& haveValueDifferenceOfOne(userCard, selectedCard)) {
				userCard.setValue(selectedCard.getValue());
				userCard.setSuit(selectedCard.getSuit());

				selectedCard.setHasBeenRemoved(true);
				numberRemoved++;
				userScore = userScore + pointsToAdd;
				pointsToAdd = pointsToAdd + 1;
				cards[selectedCardRow][selectedCardCol] = null;
				revealNeighbouringCards(selectedCard.getCardArea(),
						selectedCardRow);
			}
		}

		if (cardStack.size() > 0 && packCardHasBeenPressed(pressPt)) { // click
																		// on
																		// card
																		// stack
			randomPosition = (int) (Math.random() * cardStack.size());
			userCard = cardStack.remove(randomPosition);
			userScore = userScore - 5;
			pointsToAdd = 1;
			setUpCardPosition(userCard, cardWidth * 8, cardHeight * 5, true);
		}

		if (noMoreTableCards()) {
			noMoreTableCards = true;
		} else if (userIsStillAbleToWin(userCard) == false) {
			noMoreAvailableMoves = true;
		}

		repaint();
	}

	private boolean haveValueDifferenceOfOne(Card userCard, Card selectedCard) { // compare
																					// values
																					// of
																					// cards
		int userValue = userCard.getValue();
		int selectedValue = selectedCard.getValue();
		int diff = Math.abs(userValue - selectedValue);

		return diff == 1 || diff == 12;
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	private Point getRowColOfSelectedCard(Point pressPt) { // return point for a
															// click,
															// represening the
															// card
		boolean cont = false;
		for (int i = 0; i < cards.length; i++) {
			for (int j = 0; j < cards[i].length; j++) {
				if (cards[i][j] != null) {
					cont = cards[i][j].pressPointIsInsideCard(pressPt);
					if (cont) {
						return new Point(i, j);
					}
				}
			}
		}
		return null;

	}

	private boolean packCardHasBeenPressed(Point pressPt) { // checks if a card
															// stack has been
															// pressed
		if (aFaceDownCard.pressPointIsInsideCard(pressPt)) {
			return true;
		}

		return false;
	}

	private void revealNeighbouringCards(Rectangle removedCardArea,
			int rowOfRemovedCard) {// faces up neighbors if they are free
		int previousRow = rowOfRemovedCard - 1;
		int nextRow = rowOfRemovedCard + 1;
		if (rowOfRemovedCard == 0 || rowOfRemovedCard == 4)
			return;
		if (rowOfRemovedCard == 2) {
			checkRowOfNeighbours(removedCardArea, previousRow, rowOfRemovedCard);
			checkRowOfNeighbours(removedCardArea, nextRow, rowOfRemovedCard);
		}
		if (rowOfRemovedCard == 1)
			checkRowOfNeighbours(removedCardArea, previousRow, rowOfRemovedCard);
		if (rowOfRemovedCard == 3)
			checkRowOfNeighbours(removedCardArea, nextRow, rowOfRemovedCard);

	}

	private void checkRowOfNeighbours(Rectangle removedCardArea,
			int rowCurrentlyChecking, int rowNumberOfRemovedCard) {// checks row
																	// of
																	// neighbors
		for (int i = 0; i < cards[rowCurrentlyChecking].length; i++)
			if (cards[rowCurrentlyChecking][i] != null)
				if (hasNoIntersectingNeighbourInRow(
						cards[rowCurrentlyChecking][i].getCardArea(),
						rowNumberOfRemovedCard))
					cards[rowCurrentlyChecking][i].setIsFaceUp(true);

	}

	private boolean hasNoIntersectingNeighbourInRow(
			Rectangle areaOfCardToCheck, int rowNumberOfRemovedCard) { // checks
																		// if
																		// areas
																		// are
																		// intersecting
		Rectangle areaToCheck = areaOfCardToCheck;
		Card cardToCheck;
		for (int i = 0; i < 9; i++) {
			if (cards[rowNumberOfRemovedCard][i] != null) {
				cardToCheck = cards[rowNumberOfRemovedCard][i];
				if (areaToCheck.intersects(cardToCheck.getCardArea()))
					return false;
			}

		}

		return true;
	}

	//checks if there are table cards, return false if are, true if no more
	private boolean noMoreTableCards() {
		for (int i = 0; i < cards.length; i++) {
			for (int j = 0; j < cards[i].length; j++) {
				if (cards[i][j] != null)
					return false;
			}
		}

		return true;
	}

	
	public void paintComponent(Graphics g) { //draw all not-null cards
		super.paintComponent(g);
		setUpVisibleRowOfCards(cards);
		drawTableCards(g);
		drawRestOfJPanelDisplay(g);
	}

	private void drawTableCards(Graphics g) {
		// the order in which rows are drawn
		// i.e., row 0, then row 4, then row 1,
		// etc.,
		int[] orderToBeDrawn = { 0, 4, 1, 3, 2 };
		for (int i = 0; i < orderToBeDrawn.length; i++) {
			drawRowOfCards(g, orderToBeDrawn[i]);
		}

	}

	private void drawRowOfCards(Graphics g, int whichRow) {
		/*
		 * helper method for drawing of all cards
		 */
		if (whichRow == 0 || whichRow == 4) {
			for (int i = 0; i < 4; i++) {
				if (cards[whichRow][i] != null)
					cards[whichRow][i].drawCard(g, this);
			}
		}
		if (whichRow == 1 || whichRow == 3) {
			for (int i = 0; i < 8; i++) {
				if (cards[whichRow][i] != null)
					cards[whichRow][i].drawCard(g, this);
			}
		}
		if (whichRow == 2) {
			for (int i = 0; i < 9; i++) {
				if (cards[whichRow][i] != null)
					cards[whichRow][i].drawCard(g, this);
			}
		}

	}

	private void drawRestOfJPanelDisplay(Graphics g) { //draw rest of panel
		userCard.drawCard(g, this);
		int numberLeftInPack = cardStack.size();
		if (numberLeftInPack > 0) {
			aFaceDownCard.drawCard(g, this);
			drawNumberInsideCardArea(g, aFaceDownCard);
		}

		drawGameInformation(g);
	}

	private void drawNumberInsideCardArea(Graphics g, Card aFaceDownCard) { //draw numbers
		Rectangle cardArea = aFaceDownCard.getCardArea();
		int numberLeftInPack = cardStack.size();
		g.setFont(new Font("Times", Font.BOLD, 48));
		if (numberLeftInPack < 10) {
			g.drawString("" + numberLeftInPack,
					cardArea.x + cardArea.width / 3, cardArea.y
							+ cardArea.height * 2 / 3);
		} else {
			g.drawString("" + numberLeftInPack,
					cardArea.x + cardArea.width / 6, cardArea.y
							+ cardArea.height * 2 / 3);
		}
	}

	private void drawGameInformation(Graphics g) { //draw game information
		g.setFont(new Font("Times", Font.BOLD, 48));
		g.setColor(Color.LIGHT_GRAY);
		String scoreMessage = " Score: " + userScore;

		if (noMoreTableCards) {
			scoreMessage = "No more table cards! " + scoreMessage;
		} else if (noMoreAvailableMoves) {
			scoreMessage = "No more available moves! " + scoreMessage;
		} else {
			scoreMessage = "Cards removed: " + numberRemoved + scoreMessage;
		}
		g.drawString(scoreMessage, A1Constants.SCORE_POSITION.x,
				A1Constants.SCORE_POSITION.y);
	}

	//sets up visible middle row of cards
	private void setUpVisibleRowOfCards(Card[][] cards) {
		for (int i = 0; i < 9; i++) {
			if (cards[2][i] != null)
				cards[2][i].setIsFaceUp(true);
		}
	}

	// -------------------------------------------------------------------
	// -------- Create a 2D array of CARD objects and --------------------
	// --- the parameter ArrayList will contain the cards which remain ---
	// --- in the pack after the table cards are randomly selected ------
	
	private Card[][] getRandomTableCards(ArrayList<Card> cardStack) {
		final int[] NON_NULL_CARDS_IN_EACH_ROW = { 4, 8, 9, 8, 4 };
		Card card;
		int randomArraylistPosition;
		Card[] first = new Card[9];
		for (int i = 0; i < NON_NULL_CARDS_IN_EACH_ROW[0]; i++) {
			Random r = new Random();
			randomArraylistPosition = r.nextInt(cardStack.size());
			card = cardStack.get(randomArraylistPosition);
			setupIndividualCardPosition(card, 0, i);
			first[i] = card;
			cardStack.remove(randomArraylistPosition);
		}
		Card[] second = new Card[9];
		for (int i = 0; i < NON_NULL_CARDS_IN_EACH_ROW[1]; i++) {
			Random r = new Random();
			randomArraylistPosition = r.nextInt(cardStack.size());
			card = cardStack.get(randomArraylistPosition);
			setupIndividualCardPosition(card, 1, i);
			second[i] = card;
			cardStack.remove(randomArraylistPosition);
		}
		Card[] third = new Card[9];
		for (int i = 0; i < NON_NULL_CARDS_IN_EACH_ROW[2]; i++) {
			Random r = new Random();
			randomArraylistPosition = r.nextInt(cardStack.size());
			card = cardStack.get(randomArraylistPosition);
			setupIndividualCardPosition(card, 2, i);
			third[i] = card;
			cardStack.remove(randomArraylistPosition);
		}
		Card[] fourth = new Card[9];
		for (int i = 0; i < NON_NULL_CARDS_IN_EACH_ROW[3]; i++) {
			Random r = new Random();
			randomArraylistPosition = r.nextInt(cardStack.size());
			card = cardStack.get(randomArraylistPosition);
			setupIndividualCardPosition(card, 3, i);
			fourth[i] = card;
			cardStack.remove(randomArraylistPosition);
		}
		Card[] fifth = new Card[9];
		for (int i = 0; i <NON_NULL_CARDS_IN_EACH_ROW[4]; i++) {
			Random r = new Random();
			randomArraylistPosition = r.nextInt(cardStack.size());
			card = cardStack.get(randomArraylistPosition);
			setupIndividualCardPosition(card, 4, i);
			fifth[i] = card;
			cardStack.remove(randomArraylistPosition);
		}
		Card[][] displayCards = { first, second, third, fourth, fifth };
		return displayCards;
	}

	private void setupIndividualCardPosition(Card card, int rowNumber,
			int colNumber) { //set up one card to its position
		final int CARD_DISPLAY_TOP = 60;
		final int DISPLAY_GAP = 6;

		int leftPositionForRow = getLeftPositionForRow(rowNumber);

		int y = CARD_DISPLAY_TOP + (cardHeight * 3 / 4) * rowNumber;
		int x = CARD_DISPLAY_LEFT + leftPositionForRow
				+ (cardWidth + DISPLAY_GAP - 1) * colNumber;

		if (rowNumber == 0 || rowNumber == NUMBER_OF_ROWS - 1) {
			x = CARD_DISPLAY_LEFT + leftPositionForRow
					+ (cardWidth + DISPLAY_GAP - 1) * 2 * colNumber;
		}

		card.setCardArea(x, y, cardWidth, cardHeight);
	}

	private int getLeftPositionForRow(int rowNumber) { //get left position for row
		if (rowNumber == 0 || rowNumber == NUMBER_OF_ROWS - 1) {
			return CARD_DISPLAY_LEFT + cardWidth;
		}
		if (rowNumber == 1 || rowNumber == NUMBER_OF_ROWS - 2) {
			return CARD_DISPLAY_LEFT + cardWidth / 2;
		}

		return CARD_DISPLAY_LEFT;
	}

	// ---------------------------------------------------------------------
	// -------- Write To File ----------------------------------------------
	// ---------------------------------------------------------------------
	// Write the current game information to the SavedGame.txt file
	// private Card aFaceDownCard; doesn't need to be stored
	// ---------------------------------------------------------------------
	// ---------------------------------------------------------------------
	private void writeToFile() {
		String fileName = "SavedGame.txt";
		PrintWriter pW = null;
		Card card;
		try {
			pW = new PrintWriter(fileName);
			pW.println(cardWidth);
			pW.println(cardHeight);
			pW.println(userCard.getCardStatusInformation());

			for (int i = 0; i < NUMBER_OF_ROWS; i++) {
				for (int j = 0; j < NUMBER_OF_COLS; j++) {
					if (cards[i][j] == null) {
						pW.println("null");
					} else {
						pW.println(cards[i][j].getCardStatusInformation());
					}
				}
			}

			pW.println(cardStack.size());

			for (int i = 0; i < cardStack.size(); i++) {
				card = cardStack.get(i);
				pW.println(card.getCardStatusInformation());
			}

			pW.println(userScore);
			pW.println(pointsToAdd);
			pW.println(noMoreTableCards);
			pW.println(noMoreAvailableMoves);
			pW.println(numberRemoved);

			pW.close();
		} catch (IOException e) {
			System.out.println("Error saving game to " + fileName);
		}
	}

	// ---------------------------------------------------------------------
	// -------- Load From File ---------------------------------------------
	// ---------------------------------------------------------------------
	// The createACard helper method has been defined and
	// is useful when creating a Card object from the
	// information read from the file.
	// ---------------------------------------------------------------------
	// ---------------------------------------------------------------------
	public void loadFromFile() {
		String fileName = "SavedGame.txt";
		Scanner scan;
		try {
			scan = new Scanner(new File(fileName));
			String cardInfo;
			int cardStackSize;
			cardWidth = Integer.parseInt(scan.nextLine());
			cardHeight = Integer.parseInt(scan.nextLine());
			cardInfo = scan.nextLine();
			userCard = createACard(cardInfo, cardWidth, cardHeight);
			for (int i = 0; i < cards.length; i++) {
				for (int j = 0; j < cards[i].length; j++) {
					cardInfo = scan.nextLine();
					if (cardInfo.equals("null"))
						cards[i][j] = null;
					else
						cards[i][j] = createACard(cardInfo, cardWidth,
								cardHeight);

				}
			}
			cardStackSize = Integer.parseInt(scan.nextLine());
			cardStack = new ArrayList<Card>();
			for (int i = 0; i < cardStackSize; i++) {
				cardInfo = scan.nextLine();
				cardStack.add(createACard(cardInfo, cardWidth, cardHeight));
			}
			userScore = Integer.parseInt(scan.nextLine());
			pointsToAdd = Integer.parseInt(scan.nextLine());
			noMoreTableCards = scan.nextBoolean();
			noMoreAvailableMoves = scan.nextBoolean();
			numberRemoved = scan.nextInt();
		} catch (FileNotFoundException e) {
			System.out.println("Error loading game from SavedGame.txt");
		}

	}

	private Card createACard(String info, int width, int height) { //creates a card from string
		Card card;
		int suit, value, x, y;
		boolean removed, faceUp;

		Scanner scanInfo = new Scanner(info);
		value = scanInfo.nextInt();
		suit = scanInfo.nextInt();
		x = scanInfo.nextInt();
		y = scanInfo.nextInt();
		removed = scanInfo.nextBoolean();
		faceUp = scanInfo.nextBoolean();

		card = new Card(value, suit);

		card.setCardArea(x, y, width, height);
		card.setIsFaceUp(faceUp);
		card.setHasBeenRemoved(removed);

		return card;
	}

	// -------------------------------------------------------------------
	// ------ Create an ArrayList of a full pack of CARD objects ---------
	// -------------------------------------------------------------------
	private ArrayList<Card> createTheFullPack() {
		ArrayList<Card> theCards = new ArrayList<Card>(TOTAL_NUMBER_OF_CARDS);
		int suitNum = A1Constants.CLUBS;
		int cardValue = 0;

		for (int i = 0; i < TOTAL_NUMBER_OF_CARDS; i++) {
			theCards.add(new Card(cardValue, suitNum));

			if (cardValue >= CARDS_IN_EACH_SUIT - 1) {
				suitNum++;
			}

			cardValue = (cardValue + 1) % (CARDS_IN_EACH_SUIT);
		}

		return theCards;
	}

	// -------------------------------------------------------------------
	// -------- Load all the CARD images ---------------------------------
	// -- Set up the width and height instance variables ----------------
	// -------------------------------------------------------------------
	private void loadAllCardImagesAndSetUpCardDimensions() {
		CardImageLoadUp.loadAndSetUpAllCardImages(this);

		Dimension d = CardImageLoadUp.getDimensionOfSingleCard();
		cardWidth = d.width;
		cardHeight = d.height;
	}

	// ---------------------------------------------------------------------
	// Checks if there are any more moves which can still be made
	// --------------------------------------------------------------------- -
	private boolean userIsStillAbleToWin(Card userCard) {
		if (cardStack.size() > 0) {
			return true;
		}

		int userCardValue = userCard.getValue();
		int cardValue;
		int diff;

		for (int i = 0; i < NUMBER_OF_ROWS; i++) {
			for (int j = 0; j < NUMBER_OF_COLS; j++) {
				if (cards[i][j] != null) {
					cardValue = cards[i][j].getValue();
					diff = Math.abs(cardValue - userCardValue);
					if (diff == 1 || diff == 12) {
						return true;
					}
				}
			}
		}

		return false;
	}
}
