package org.fabryprog.cv.faces.example;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.openimaj.image.ImageUtilities;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;

import boofcv.abst.tracker.TrackerObjectQuad;
import boofcv.factory.tracker.FactoryTrackerObjectQuad;
import boofcv.gui.image.ShowImages;
import boofcv.gui.tracker.TrackerObjectQuadPanel;
import boofcv.io.MediaManager;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.io.image.SimpleImageSequence;
import boofcv.io.wrapper.DefaultMediaManager;
import boofcv.misc.BoofMiscOps;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.ImageBase;
import georegression.struct.shapes.Quadrilateral_F64;

/**
 * Hello world!
 *
 */
public class App {
	private static final HaarCascadeDetector detector = new HaarCascadeDetector(100);
	private List<DetectedFace> faces = null;

	public void execute() throws IOException {
		MediaManager media = DefaultMediaManager.INSTANCE;
		String fileName = App.class.getResource("myface.mjpeg").getFile();

		TrackerObjectQuad tracker = FactoryTrackerObjectQuad.circulant(null, GrayU8.class);
		SimpleImageSequence video = media.openVideo(fileName, tracker.getImageType());

		ImageBase frame = video.next();

		BufferedImage img = ConvertBufferedImage.convertTo(frame, null, true);
		//ImageIO.write(img, "jpg", new File("prova.jpg"));
		faces = detector.detectFaces(ImageUtilities.createFImage(img));
		System.out.println("detect faces size = " + faces.size());
		Quadrilateral_F64 location = new Quadrilateral_F64();
		for(DetectedFace f : faces) {
			System.out.println("bounds: " + f.getBounds());	
			location = new Quadrilateral_F64(
					f.getBounds().getTopLeft().getX(), f.getBounds().getTopLeft().getY(),
					f.getBounds().getTopLeft().getX(), f.getBounds().getTopLeft().getY() + f.getBounds().getHeight(),
					f.getBounds().getTopLeft().getX() + f.getBounds().getWidth(), f.getBounds().getTopLeft().getY(),
					f.getBounds().getTopLeft().getX() + f.getBounds().getWidth(), f.getBounds().getTopLeft().getY() + f.getBounds().getHeight());
			
			tracker.initialize(frame, location);
			// TODO list of faces
			break;
		}
		

		// For displaying the results
		TrackerObjectQuadPanel gui = new TrackerObjectQuadPanel(null);
		gui.setPreferredSize(new Dimension(frame.getWidth(), frame.getHeight()));
		gui.setBackGround((BufferedImage) video.getGuiImage());
		gui.setTarget(location, true);
		ShowImages.showWindow(gui, "Tracking Results", true);

		// Track the object across each video frame and display the results
		long previous = 0;
		while (video.hasNext()) {
			frame = video.next();
			
			boolean visible = tracker.process(frame, location);

			gui.setBackGround((BufferedImage) video.getGuiImage());
			gui.setTarget(location, visible);
			gui.repaint();

			// shoot for a specific frame rate
			long time = System.currentTimeMillis();
			BoofMiscOps.pause(Math.max(0, 80 - (time - previous)));
			previous = time;
		}
	}

	public static void main(String[] args) throws IOException {
		new App().execute();
	}
}