/*
 * Name
 * Date
 * Class for single card 
 */

import java.awt.*;
import javax.swing.*;

public class Card {
	private int suit;
	private int value;
	private Rectangle cardArea;
	private boolean hasBeenRemoved;
	private boolean isFaceUp;

	public Card(int value, int suit) { // constructor, creates new card with
										// value and suit specified
		hasBeenRemoved = false;
		isFaceUp = false;
		cardArea = new Rectangle(0, 0, 0, 0);
		this.value = value;
		this.suit = suit;
	}

	// -------------------------------------------------------------------
	// -------- Accessor and mutator methods -----------------------------
	// -------------------------------------------------------------------
	public int getSuit() {
		return suit;
	}

	public int getValue() {
		return value;
	}

	public void setSuit(int suit) {
		this.suit = suit;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public boolean getHasBeenRemoved() {
		return hasBeenRemoved;
	}

	public void setHasBeenRemoved(boolean removed) {
		hasBeenRemoved = removed;
	}

	public boolean getIsFaceUp() {
		return isFaceUp;
	}

	public void setIsFaceUp(boolean faceUp) {
		isFaceUp = faceUp;
	}

	public void setCardArea(int x, int y, int w, int h) {
		cardArea = new Rectangle(x, y, w, h);
	}

	public Rectangle getCardArea() {
		return cardArea;
	}

	// -------------------------------------------------------------------
	// -------- Returns true if the parameter Point object ---------------
	// -------- is inside the Card area. --------------------------------
	// -------------------------------------------------------------------
	public boolean pressPointIsInsideCard(Point pressPt) {
		if (hasBeenRemoved)
			return false;
		if (cardArea.contains(pressPt))
			return true;
		else
			return false;
	}

	// -------------------------------------------------------------------
	// -------- Get String describing the card suit and value ------------
	// -------------------------------------------------------------------
	public String getCardStatusInformation() {
		String cardInfo = "" + value + " " + suit + " " + cardArea.x + " "
				+ cardArea.y + " " + hasBeenRemoved + " " + isFaceUp;
		return cardInfo;
	}

	// -------------------------------------------------------------------
	// -------- Draw the Card object. ------------------------------------
	// -------------------------------------------------------------------
	public void drawCard(Graphics g, JComponent theJPanelInstance) {
		Image cardPic;
		int fileNumber;

		if (hasBeenRemoved) {
			return;
		}

		if (isFaceUp) {
			fileNumber = suit * A1JPanel.CARDS_IN_EACH_SUIT + value;
			cardPic = CardImageLoadUp.getSingleCardImage(fileNumber);
		} else {
			cardPic = CardImageLoadUp.getFaceDownCardImage();
		}

		g.drawImage(cardPic, cardArea.x, cardArea.y, theJPanelInstance);
	}

	// -------------------------------------------------------------------
	// -------- Get String describing the card suit and value ------------
	// -------------------------------------------------------------------
	public String toString() {
		final String[] SUITS = { "CLUBS", "DIAMONDS", "HEARTS", "SPADES" };
		if (value == 0) {
			return "A" + " " + SUITS[suit];
		} else if (value == 12) {
			return "K" + " " + SUITS[suit];
		} else if (value == 11) {
			return "Q" + " " + SUITS[suit];
		} else if (value == 10) {
			return "J" + " " + SUITS[suit];
		}

		return (value + 1) + " " + SUITS[suit];
	}
}
