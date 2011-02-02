/*
 * Copyright (C) 2006-2011 by Benedict Paten (benedictpaten@gmail.com)
 *
 * Released under the MIT license, see LICENSE.txt
 */

package bp.common;

/*
 * Viewer.java
 */

import java.awt.Frame;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Viewer
                   extends Frame {
    /**
	 * 
	 */
	private static final long serialVersionUID = 7528604705605315468L;
	private Image image;

    public Viewer(final Image image) {
        final Toolkit toolkit = Toolkit.getDefaultToolkit();
        this.image = image;
        final MediaTracker mediaTracker = new MediaTracker(this);
        mediaTracker.addImage(image, 0);
        try {
            mediaTracker.waitForID(0);
        } catch (final InterruptedException ie) {
            System.err.println(ie);
            System.exit(1);
        }
        this.addWindowListener(new WindowAdapter() {
            @Override
			public void windowClosing(final WindowEvent e) {
                System.exit(0);
            }
        });
        this.setSize(image.getWidth(null), image.getHeight(null));
        //setTitle(fileName);
        this.show();
    }

    @Override
	public void paint(final java.awt.Graphics graphics) {
        graphics.drawImage(this.image, 0, 0, null);
    }

    public static void main(final String[] args) {
        //new Viewer(args[0]);
    }
}