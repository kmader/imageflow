/*
Version 1.0, 30-12-2007, First release
Version 1.1, 03-02-2008, added component <version> handling, prepared for MDI support

IMPORTANT NOTICE, please read:

This software is licensed under the terms of the GNU GENERAL PUBLIC LICENSE,
please read the enclosed file license.txt or http://www.gnu.org/licenses/licenses.html

Note that this software is freeware and it is not designed, licensed or intended
for use in mission critical, life support and military purposes.

The use of this software is at the risk of the user.
 */

/* used by VisualAp.java

javalc6

todo:
- migliorare gestione delle exception interne: ExceptionListener in XMLDecoder
- estendere <selection> ad altri oggetti per esempio: Edge
 */
package visualap;


import static de.danielsenff.imageflow.utils.Reversed.reversed;
import ij.IJ;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.Collection;

import javax.swing.JPanel;

import de.danielsenff.imageflow.models.NodeList;
import de.danielsenff.imageflow.models.connection.Connection;
import de.danielsenff.imageflow.models.connection.ConnectionList;
import de.danielsenff.imageflow.models.connection.Input;
import de.danielsenff.imageflow.models.connection.Output;
import de.danielsenff.imageflow.models.connection.Pin;
import de.danielsenff.imageflow.models.unit.UnitList;

/**
 * Depricated, should be faded out in the future
 * @author dahie
 *
 */
public class GPanel extends JPanel implements Printable, MouseListener, MouseMotionListener  {

	protected GPanelListener parentPanel;

	protected Point pick = null;
	protected Pin drawEdge;
	protected Selection<Node> selection = new Selection<Node>();
	protected NodeList<Node> nodeL = new NodeList<Node>();
	protected Collection<Connection> connectionList = new ConnectionList();
	protected Point mouse;

	protected Rectangle rect;

	// handling of selection rectangle
	protected Rectangle currentRect = null;
	protected Rectangle rectToDraw = null;
	protected Rectangle previousRectDrawn = new Rectangle();
	final static float dash1[] = {5.0f};
	protected final static BasicStroke dashed = new BasicStroke(1.0f, 
			BasicStroke.CAP_BUTT, 
			BasicStroke.JOIN_MITER, 
			10.0f, dash1, 0.0f);

	public GPanel(GPanelListener parent) {
		this.parentPanel = parent;
		addMouseListener(this);
		addMouseMotionListener(this);
		setBackground(Color.white);
	}

	/**
	 * paint things that eventually go on a printer
	 * @param g
	 */
	public void paintPrintable(Graphics g) {
		updatePreferredSize(g);
		for (Connection aEdge : connectionList) {
			paintPrintableConnection(g, aEdge);
		}
	}

	public void updatePreferredSize(Graphics g) {
		rect = new Rectangle();
		for (Node t : getNodeL()) {
			rect = rect.union(t.paint(g, this));
		}
		this.setPreferredSize(rect.getSize());
		this.getParent().setPreferredSize(rect.getSize());
		revalidate();
	}



	protected void paintPrintableConnection(Graphics g, Connection aEdge) {
		Point from = aEdge.getInput().getOrigin();
		Point to = aEdge.getOutput().getOrigin();
		g.drawLine(from.x, from.y, to.x, to.y);
	}


	public void paintComponent(Graphics g) {
		super.paintComponent(g);
/*
		// paint printable items
		paintPrintable(g);
		// paint non printable items
		if (drawEdge!= null)	{
			Point origin = drawEdge.getLocation();

			Graphics2D g2 = (Graphics2D) g;
			float lineWidth = 1.0f;
			g2.setStroke(new BasicStroke(lineWidth));
			g2.drawLine(origin.x, origin.y, mouse.x, mouse.y);
			g2.draw(new Line2D.Double(origin.x, origin.y, mouse.x, mouse.y));
		}
		//If currentRect exists, paint a box on top.
		if (currentRect != null) {
			Graphics2D g2 = (Graphics2D) g;
			//Draw a rectangle on top of the image.
			g2.setXORMode(Color.white); //Color of Edge varies
			//depending on image colors
			g2.setStroke(dashed);
			g2.drawRect(rectToDraw.x, rectToDraw.y, 
					rectToDraw.width - 1, rectToDraw.height - 1);
		}*/
	}



	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() > 1) {
			//if (selection.size() == 1) properties(selection.get(0));
			//else selection.clear(); //zz to be handled in more completed way
		}
	}


	public void mousePressed(MouseEvent e) {
		// generato nell'istante in cui il mouse viene premuto
		int x = e.getX();
		int y = e.getY();
		// qui � obbligatorio un iteratore che scandisce la lista al contrario!
		for (Node aNode : reversed(nodeL)) {
			
			Object sel = aNode.contains(x,y);
			// 	check selected element, is it a Node?
			if (sel instanceof Node) {
				pick = new Point(x,y);
				
				if(!selection.contains(sel)) {
					// if there are selections
					// and the new selection should not be added (Meta or CTRL down)
					// or 
					if(!selection.isEmpty() && !isMetaOrCTRLDown(e) || e.isPopupTrigger() ) {
						selection.clear();
					}
					
					// add node
					selection.add(aNode);
				} else {
					// node is selected
					
					// if no popup triggered
					// and new selection should not be added
					if(!e.isPopupTrigger() && isMetaOrCTRLDown(e))
						selection.remove(aNode);
				}
				
				if(!e.isPopupTrigger()) {
					for (Node iNode : selection)
						iNode.drag(true);

					e.consume();
					changeCursor(Cursor.MOVE_CURSOR);
				} else {
					parentPanel.showFloatingMenu(e);
				}
				repaint();
				return;
			}
			// check selected element, is it a Pin?
			else if (sel instanceof Pin && !((Pin)sel).isLocked()) {
				drawEdge = (Pin) sel;
				//	System.out.println(drawEdge);
				mouse = new Point (x,y);
				changeCursor(Cursor.CROSSHAIR_CURSOR);
				return;
			}
		}
		selection.clear();
		if (e.isPopupTrigger())
			parentPanel.showFloatingMenu(e);

		//	e.consume();

		// handling of selection rectangle
		currentRect = new Rectangle(x, y, 0, 0);
		// update for smoother repaint
		updateDrawableRect(getWidth(), getHeight());
		repaint();
	}


	private boolean isMetaOrCTRLDown(MouseEvent e) {
		return e.isControlDown() || (e.isMetaDown() && IJ.isMacintosh() );
	}

	/**
	 * Sees, if a connection between the given Pins exists.
	 * @param from
	 * @param to
	 * @return
	 */
	public boolean containsConnection(final Pin from, final Pin to) {
		for (final Connection connection : connectionList) {
			if ( (connection.getInput().equals(from) 
						&& connection.getOutput().equals(to) )
					|| ( (connection.getInput().equals(to) 
							&& connection.getOutput().equals(from)) ) )
				return true;
		}
		return false;
	}

	public void mouseReleased(MouseEvent e) {
		// generato quando il mouse viene rilasciato, anche a seguito di click
		int x = e.getX();
		int y = e.getY();
		if (pick != null) {
			if(!e.isPopupTrigger()) {
				for (Node iNode : selection) {
					iNode.translate(x-pick.x, y-pick.y);
					iNode.drag(false);
				}
			}
			pick = null;
			repaint();
			e.consume();
			changeCursor(Cursor.DEFAULT_CURSOR);
		}
		else if (drawEdge != null)	{
			// insert new Edge if not already present in EdgeL
			for (Node aNode : nodeL) {
				Object sel = aNode.contains(x,y);
				if ((sel instanceof Pin) 
						&& (!drawEdge.equals(sel))
						&& (!containsConnection(drawEdge, (Pin) sel)) ) 
				{
					if( ( (sel instanceof Input && drawEdge instanceof Output)
						|| (sel instanceof Output && drawEdge instanceof Input) )
						&& !((Pin)sel).getParent().equals(drawEdge.getParent())
						&& !((Pin)sel).isLocked()) 
					{
						Connection newConn = new Connection(drawEdge, (Pin) sel);
						connectionList.add(newConn);
					}
						
				}
			}
			drawEdge = null;
			changeCursor(Cursor.DEFAULT_CURSOR);
			repaint();
		}
		// handling of selection rectangle
		else if (currentRect != null) {
			normalizeRect();
			for (Node aNode : nodeL)
				if (aNode.contained(currentRect)) {
					selection.add(aNode);
				}
			currentRect = null;
			repaint();
		}

		parentPanel.showFloatingMenu(e);
		//	e.consume();
	}

	public void mouseDragged(MouseEvent e) {
		// generato quando il mouse premuto viene spostato, vari eventi sono generati durante il trascinamento
		if(!e.isPopupTrigger()) {
			if (pick!= null) {
				for (Node iNode : selection)
					iNode.drag(e.getX()-pick.x, e.getY()-pick.y);
				repaint();
				e.consume();
			}
			else if (drawEdge != null)	{
				mouse.x = e.getX(); mouse.y = e.getY();
				repaint();
				e.consume();
			}
			// handling of selection rectangle
			else if (currentRect != null) updateSize(e);
		}
	}

	public void mouseMoved(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}

	protected void changeCursor(int cursor) {
		setCursor(Cursor.getPredefinedCursor(cursor));
	}

	void updateSize(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		currentRect.setSize(x - currentRect.x, y - currentRect.y);
		updateDrawableRect(getWidth(), getHeight());
		Rectangle totalRepaint = rectToDraw.union(previousRectDrawn);
		// FIXME here is a transparency bug, when called the repaint method for an area, 
		// units outside the area are redrawn aswell
		//repaint(totalRepaint.x, totalRepaint.y, totalRepaint.width, totalRepaint.height);
		repaint();
	}

	protected void updateDrawableRect(int compWidth, int compHeight) {
		int x = currentRect.x;
		int y = currentRect.y;
		int width = currentRect.width;
		int height = currentRect.height;

		//Make the width and height positive, if necessary.
		if (width < 0) {
			width = 0 - width;
			x = x - width + 1; 
			if (x < 0) {
				width += x; 
				x = 0;
			}
		}
		if (height < 0) {
			height = 0 - height;
			y = y - height + 1; 
			if (y < 0) {
				height += y; 
				y = 0;
			}
		}
		//The rectangle shouldn't extend past the drawing area.
		if ((x + width) > compWidth) {
			width = compWidth - x;
		}
		if ((y + height) > compHeight) {
			height = compHeight - y;
		}

		//Update rectToDraw after saving old value.
		if (rectToDraw != null) {
			previousRectDrawn.setBounds(
					rectToDraw.x, rectToDraw.y, 
					rectToDraw.width, rectToDraw.height);
			rectToDraw.setBounds(x, y, width, height);
		} else {
			rectToDraw = new Rectangle(x, y, width, height);
		}
	}

	private void normalizeRect() {
		int x = currentRect.x;
		int y = currentRect.y;
		int width = currentRect.width;
		int height = currentRect.height;

		//Make the width and height positive, if necessary.
		if (width < 0) {
			width = - width;
			x = x - width + 1; 
			if (x < 0) {
				width += x; 
				x = 0;
			}
		}
		if (height < 0) {
			height = - height;
			y = y - height + 1; 
			if (y < 0) {
				height += y; 
				y = 0;
			}
		}
		currentRect.setBounds(x, y, width, height);
	}

	public int print(Graphics g, PageFormat pf, int pi) throws PrinterException {
		if (pi >= 1) {
			return Printable.NO_SUCH_PAGE;
		}
		g.translate((int)pf.getImageableX(),(int)pf.getImageableY());
		paintPrintable(g);
		return Printable.PAGE_EXISTS;
	}

	public String shortName(String fullName) {
		int ix = fullName.lastIndexOf('.');
		if (ix >= 0) {
			return fullName.substring(ix+1);
		} else	return fullName;
	}

	/**
	 * @return the nodeL
	 */
	public UnitList getNodeL() {
		return (UnitList) this.nodeL;
	}

	/**
	 * @param nodeL the nodeL to set
	 */
	public void setNodeL(NodeList<Node> nodeL) {
		this.nodeL = nodeL;
	}

	/**
	 * @return the selection
	 */
	public Selection<Node> getSelection() {
		return this.selection;
	}

	/**
	 * @return the edgeL
	 */
	public Collection<Connection> getEdgeL() {
		return this.connectionList;
	}

	/**
	 * @param edgeL the edgeL to set
	 */
	public void setEdgeL(Collection<Connection> edgeL) {
		this.connectionList = edgeL;
	}

};