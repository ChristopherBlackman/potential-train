package Assignment3;

import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadBar extends HBox {

	private static Stage downloadWindow = null;
	private static VBox downloadTasks;
	private static TextArea messageArea;
	private static final int BUFFER_SIZE = 4096;
	
	
	/** Calling this function will guarantee that the downloadTasks VBox is created and visible.
	 * @return A Stage that will show each downloadTask's progress
	 */
	public Stage getDownloadWindow()
	{ System.out.print("oh great");
		if(downloadWindow == null)
		{
			//Create a new borderPane for the download window
			BorderPane downloadRoot = new BorderPane();
			downloadTasks = new VBox();
			//downloadTasks will contain rows of DownloadTask objects, which are HBoxes
			downloadRoot.setCenter(		 downloadTasks		);
			
			//The bottom of the window will be the message box for download tasks
			downloadRoot.setBottom(		messageArea = new TextArea() 		);
			downloadWindow = new Stage();
			downloadWindow.setScene( new Scene(downloadRoot, 400, 600)  );
			
			//When closing the window, set the variable downloadWindow to null
			downloadWindow.setOnCloseRequest(		event -> downloadWindow = null		);
			System.out.print("oh great");
		}
		return downloadWindow;
	}
	
	/**The constructor for a DownloadTask
	 * 
	 * @param newLocation  The String URL of a file to download
	 */
	public void downloadFile(String fileURL, String saveDir)
            throws IOException {
        URL url = new URL(fileURL);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        int responseCode = httpConn.getResponseCode();
 
        // always check HTTP response code first
        if (responseCode == HttpURLConnection.HTTP_OK) {
            String fileName = "";
            String disposition = httpConn.getHeaderField("Content-Disposition");
            String contentType = httpConn.getContentType();
            int contentLength = httpConn.getContentLength();
 
            if (disposition != null) {
                // extracts file name from header field
                int index = disposition.indexOf("filename=");
                if (index > 0) {
                    fileName = disposition.substring(index + 10,
                            disposition.length() - 1);
                }
            } else {
                // extracts file name from URL
                fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1,
                        fileURL.length());
            }
 
            System.out.println("Content-Type = " + contentType);
            System.out.println("Content-Disposition = " + disposition);
            System.out.println("Content-Length = " + contentLength);
            System.out.println("fileName = " + fileName);
 
            // opens input stream from the HTTP connection
            InputStream inputStream = httpConn.getInputStream();
            String saveFilePath = saveDir + File.separator + fileName;
             
            // opens an output stream to save into file
            FileOutputStream outputStream = new FileOutputStream(saveFilePath);
 
            int bytesRead = -1;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
 
            outputStream.close();
            inputStream.close();
 
            System.out.println("File downloaded");
        } else {
            System.out.println("No file to download. Server replied HTTP code: " + responseCode);
        }
        httpConn.disconnect();
    }
	public DownloadBar(String newLocation)
	{
		//See if the filename at the end of newLocation exists on your hard drive.
		// If the file already exists, then add (1), (2), ... (n) until you find a new filename that doesn't exist.
		
		
		//Create the window if it doesn't exist. After this call, the VBox and TextArea should exist.
		getDownloadWindow();

		
		
		//Add a Text label for the filename
		
		//Add a ProgressBar to show the progress of the task
		
		//Add a cancel button that asks the user for confirmation, and cancel the task if the user agrees
		
		try {
			downloadFile(newLocation,("downloads/"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		 //Start the download
		DownloadTask aFileDownload = new DownloadTask( ) ;
		  new Thread( aFileDownload ).start();
	}
	
	
	
	
	
	
	/**This class represents a task that will be run in a separate thread. It will run call(), 
	 *  and then call succeeded, cancelled, or failed depending on whether the task was cancelled
	 *  or failed. If it was not, then it will call succeeded() after call() finishes.
	 */
	private class DownloadTask extends Task<String>
	{ 	
		
		
		// This should start the download. Look at the downloadFile() function at:
		//  http://www.codejava.net/java-se/networking/use-httpurlconnection-to-download-file-from-an-http-url
		//Take that function but change it so that it updates the progress bar as it iterates through the while loop.
		//Here is a tutorial on how to upgrade a progress bar:
		//	https://docs.oracle.com/javase/8/javafx/user-interface-tutorial/progress.htm
		@Override
		protected String call() throws Exception {
			
			return "Finished";
		}
		
		
		//Write the code here to handle a successful completion of the call() function.
		@Override
		protected void succeeded() {
			super.succeeded();	
			
		}
		
		//Write the code here to handle the task being cancelled before call() finishes.
		@Override
		protected void cancelled() {
			super.cancelled();	
		}
		
		@Override
		protected void failed() {		
			super.failed();			
		}
	}		
}
