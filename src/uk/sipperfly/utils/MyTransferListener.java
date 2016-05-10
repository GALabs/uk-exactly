/* 
 * Exactly
 * Author: Nouman Tayyab (nouman@avpreserve.com)
 * Author: Rimsha Khalid (rimsha@avpreserve.com)
 * Version: 0.1
 * Requires: JDK 1.7 or higher
 * Description: This tool transfers digital files to the UK Exactly
 * Support: info@avpreserve.com
 * License: Apache 2.0
 * Copyright: University of Kentucky (http://www.uky.edu). All Rights Reserved
 *
 */
package uk.sipperfly.utils;

import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.sipperfly.ui.FTPConnection;

/**
 *
 * @author noumantayyab
 */
public class MyTransferListener implements FTPDataTransferListener {

	private static String GACOM = "com.UKExactly";
	private String filePath;
	private FTPClient client;

	public MyTransferListener(String filePath, FTPClient ftp) {
		this.filePath = filePath;
		this.client = ftp;
	}

	public void started() {
		Logger.getLogger(GACOM).log(Level.INFO, "File transfer started: ".concat(this.filePath));
		// Transfer startedÏ
	}

	public void transferred(int length) {
//		try {
			System.out.println("file transferred: "+ length);
//			this.client.noop();
//		} catch (IllegalStateException ex) {
//			Logger.getLogger(MyTransferListener.class.getName()).log(Level.SEVERE, null, ex);
//		} catch (IOException ex) {
//			Logger.getLogger(MyTransferListener.class.getName()).log(Level.SEVERE, null, ex);
//		} catch (FTPIllegalReplyException ex) {
//			Logger.getLogger(MyTransferListener.class.getName()).log(Level.SEVERE, null, ex);
//		} catch (FTPException ex) {
//			Logger.getLogger(MyTransferListener.class.getName()).log(Level.SEVERE, null, ex);
//		}
	}

	public void completed() {
		Logger.getLogger(GACOM).log(Level.INFO, "File transfer completed: ".concat(this.filePath));
		// Transfer completed
	}

	public void aborted() {
		Logger.getLogger(GACOM).log(Level.INFO, "File transfer aborted: ".concat(this.filePath));
		// Transfer aborted
	}

	public void failed() {
		Logger.getLogger(GACOM).log(Level.INFO, "File transfer failed: ".concat(this.filePath));
		// Transfer failed
	}

}
