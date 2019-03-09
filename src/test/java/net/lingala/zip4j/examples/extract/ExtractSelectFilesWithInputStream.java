package net.lingala.zip4j.examples.extract;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.io.ZipInputStream;
import net.lingala.zip4j.model.CentralDirectory;
import net.lingala.zip4j.unzip.UnzipUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Paths;

public class ExtractSelectFilesWithInputStream {

	private final int BUFF_SIZE = 4096;

	public ExtractSelectFilesWithInputStream() {

		ZipInputStream is = null;
		OutputStream os = null;

		try {
			// Initiate the ZipFile
			ZipFile zipFile = new ZipFile(Paths.get("C:\\ZipTest\\ExtractAllFilesWithInputStreams.zip"));
			String destinationPath = "c:\\ZipTest";

			// If zip file is password protected then set the password
			if (zipFile.isEncrypted()) {
				zipFile.setPassword("password");
			}

			//Get the FileHeader of the File you want to extract from the
			//zip file. Input for the below method is the name of the file
			//For example: 123.txt or abc/123.txt if the file 123.txt
			//is inside the directory abc
			CentralDirectory.FileHeader fileHeader = zipFile.getFileHeader("yourfilename");

			if (fileHeader != null) {

				//Build the output file
				String outFilePath = destinationPath + System.getProperty("file.separator") + fileHeader.getFileName();
				File outFile = new File(outFilePath);

				//Checks if the file is a directory
				if (fileHeader.isDirectory()) {
					//This functionality is up to your requirements
					//For now I create the directory
					outFile.mkdirs();
					return;
				}

				//Check if the directories(including parent directories)
				//in the output file path exists
				File parentDir = outFile.getParentFile();
				if (!parentDir.exists()) {
					parentDir.mkdirs(); //If not create those directories
				}

				//Get the InputStream from the ZipFile
				is = zipFile.getInputStream(fileHeader);
				//Initialize the output stream
				os = new FileOutputStream(outFile);

				int readLen = -1;
				byte[] buff = new byte[BUFF_SIZE];

				//Loop until End of File and write the contents to the output stream
				while ((readLen = is.read(buff)) != -1) {
					os.write(buff, 0, readLen);
				}

				//Closing inputstream also checks for CRC of the the just extracted file.
				//If CRC check has to be skipped (for ex: to cancel the unzip operation, etc)
				//use method is.close(boolean skipCRCCheck) and set the flag,
				//skipCRCCheck to false
				//NOTE: It is recommended to close out first because Zip4j throws
				//an exception if CRC check fails
				is.close();

				//Close output stream
				os.close();

				//To restore File attributes (ex: last modified file time,
				//read only flag, etc) of the extracted file, a utility class
				//can be used as shown below
				UnzipUtil.applyFileAttributes(fileHeader, outFile);

				System.out.println("Done extracting: " + fileHeader.getFileName());
			} else {
				System.err.println("FileHeader does not exist");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new ExtractSelectFilesWithInputStream();
	}

}
