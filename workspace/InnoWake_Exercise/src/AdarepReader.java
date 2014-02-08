import java.io.*;

import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.cos.COSDocument;

public class AdarepReader {
	
	public static void main(String[] argv) {

		// Check command line arguments
		if (argv.length != 1) {
			System.out.println("Please put filename as an argument.");
			System.exit(-1);
		}

		// Open file given from command line.
		File f = new File(argv[0]);
		if (!f.isFile()) {
			System.out.println("File not found!");
			System.exit(-1);
		}

		// Check if file is of .txt or .pdf type
		String filename = f.getName();
		String extension = filename.substring(filename.lastIndexOf(".") + 1,
				filename.length());

		// File is of type .txt
		if (extension.equals("txt")) {
			parseTextFile(f);
		}

		// file is of type .pdf
		else if (extension.equals("pdf")) {
			parsePdfFile(f);
		}
		
		//File type is not supported or file does not end .txt or .pdf
		else {
			System.out.println("File type of "
					+ extension
					+ " is not supported. Please use a txt or pdf file."
					+ " That ends with file extension .txt or .pdf");
			System.exit(-1);
		}
	}
	
	/*
	 * parsePdfFile - Parses a pdf file and prints to stdout the names of the
	 * 	files contained in an adarep report. 
	 * Input:  A File object containing an adarep report. 
	 * Output: File names printed to stdout.
	 */
	public static void parsePdfFile(File f) {
		try {
			
			// Invoke pdfbox PDF parser, to transform pdf to string.
			PDFParser parser = new PDFParser(new FileInputStream(f));
			parser.parse();
			COSDocument cosDoc = parser.getDocument();
			PDFTextStripper pdfStripper = new PDFTextStripper();
			PDDocument pdDoc = new PDDocument(cosDoc);
			String parsedText = pdfStripper.getText(pdDoc);
		    
			// Create temp file.
		    File temp = File.createTempFile("pattern", ".suffix");
		    temp.deleteOnExit();

		    // Write to temp file
		    BufferedWriter out = new BufferedWriter(new FileWriter(temp));
		    out.write(parsedText);
		    out.close();
			 
		    //Now that we have a textfile re-use our parseTextFile function.
		    parseTextFile(temp);
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/*
	 * parseTextFile - Parses a text file and prints to stdout the names of the
	 * 	files contained in an adarep report. 
	 * Input:  A File object containing an adarep report. 
	 * Output: File names printed to stdout.
	 */
	public static void parseTextFile(File f) {
		String sCurrentLine;
		try (BufferedReader br = new BufferedReader(new FileReader(f))) {

			// Start reading through document line by line.
			while ((sCurrentLine = br.readLine()) != null) {

				// Find 'database contents' section
				if (sCurrentLine.toLowerCase().contains("contents of database")) {

					// Print names until 'file options' section
					while (!(sCurrentLine = br.readLine()).toLowerCase()
							.contains("file options")) {
						printFileName(sCurrentLine);
					}
				}
			}
		} catch (IOException e) {
			System.out.println("IOException!");
			e.printStackTrace();
		}
	}
	
	/*
	 * printFileName - A helper function for parseTextFile() and parsePDFFile()
	 * 	that parses the current line and prints the filename.
	 * Input: The current line of text being read.
	 * Output: Filename printed to stdout.
	 */
	private static void printFileName(String sCurrentLine) {
		// Time the line of extra spaces, and split on spaces.
		String[] elements = sCurrentLine.trim().split(" ");

		// If the line contains more than 2 elements, the first element is a
		// number, and the second element is a word then print the word
		if (elements.length > 2 && elements[0].matches("-?\\d+(\\.\\d+)?")
				&& !elements[1].matches("-?\\d+(\\.\\d+)?")) {
			System.out.println(elements[1]);
		}
	}
}
