// Code in this program were referenced from GraphStream examples
/**
 * This program used the dgs file generated from the "FromJSONtoDGS.java"
 * to generate a set of images that can be transformed into a movie using 
 * mencoder.
 */
import org.graphstream.graph.Graph; 
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.file.FileSinkImages;
import org.graphstream.stream.file.FileSinkImages.LayoutPolicy;
import org.graphstream.stream.file.FileSinkImages.OutputPolicy;
import org.graphstream.stream.file.FileSinkImages.OutputType;
import org.graphstream.stream.file.FileSinkImages.Quality;
import org.graphstream.stream.file.FileSinkImages.Resolution;
import org.graphstream.stream.file.FileSinkImages.Resolutions;
import org.graphstream.stream.file.FileSource;
import org.graphstream.stream.file.FileSourceDGS;
import org.graphstream.stream.file.FileSourceFactory;

public class FromDGStoMovie {
	public static void main(String[] args) throws Exception{
		// FileSinkImages arguments
		OutputPolicy outputPolicy = OutputPolicy.BY_EVENT;
		String prefix = "../movie/imagesFromDGS/image";
		OutputType type = OutputType.PNG;
		Resolution resolution = Resolutions.HD720;
		 
		FileSinkImages fsi = new FileSinkImages( type, resolution );
		 
		// Create the source
		FileSourceDGS dgs = new FileSourceDGS();
		 
		// Optional configuration
		fsi.setStyleSheet(
		       "graph { padding: 50px; fill-color: white; }" +
		       "node { fill-color: black; }" +
		       "edge { fill-color: black; }");
		 
		fsi.setOutputPolicy( outputPolicy );
		fsi.setLayoutPolicy( LayoutPolicy.COMPUTED_IN_LAYOUT_RUNNER);
		fsi.setQuality(Quality.HIGH);
		
		// Adding the NUS logo
		fsi.addLogo( "logo/soclogo.jpg", 10, 10 );
		 
		// Images production
		dgs.addSink( fsi );
		 
		fsi.begin(prefix);
		dgs.begin( "DGS/Examples/network.dgs" );
		while( dgs.nextStep() );
		dgs.end();
		fsi.end();
	
		// View with graph display
		Graph graph = new SingleGraph("NetworkEvol");
		graph.display();
		graph.addAttribute("ui.antialias");
		try {
			FileSource source = FileSourceFactory.sourceFor("DGS/Examples/network.dgs" );
			source.addSink(graph);
			source.begin("DGS/Examples/network.dgs"  );
			while(source.nextEvents()){ Thread.sleep(100); }
			source.end();
		} catch(Exception e) { e.printStackTrace(); }	
	}
}
