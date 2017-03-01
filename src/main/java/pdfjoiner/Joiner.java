package pdfjoiner;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.ExceptionConverter;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;

public class Joiner {

	private boolean addPages;
	private boolean openAfterCreate;

	public static void main(String[] args) {

		// show help
		if (args.length == 0 || args[0].contains("-h") || args[0].contains("-help") || args[0].equals("-?")) {
			StringBuilder stbHelp = new StringBuilder();
			stbHelp.append("Usage: java -jar PDFJoiner.jar ");
			stbHelp.append("<flags> <outfile> <infile 1> <infile 2> ... <infile n>\n");
			stbHelp.append("Flags: -op \n");
			stbHelp.append("-h /t show this page \n");
			stbHelp.append("-? /t show this page \n");
			stbHelp.append("-o /t open created pdf \n");
			stbHelp.append("-p /t add page numbers \n");

			System.out.println(stbHelp.toString());
			return;
		}

		Joiner joiner = new Joiner();
		int argsoffset = 0;

		// set flags
		if (args[0].startsWith("-")) {
			argsoffset = 1;
			joiner.addPages = args[0].contains("p");
			joiner.openAfterCreate = args[0].contains("o");
		}

		File outfile = new File(args[argsoffset]);
		List<File> infiles = new ArrayList<File>();

		for (int i = argsoffset + 1; i < args.length; i++) {
			File in = new File(args[i]);
			if (in.exists() && !in.isDirectory()) {
				infiles.add(in);
			} else {
				System.out.println("File not found: " + in.getAbsolutePath());
			}
		}

		joiner.createPDF(outfile, infiles);
	}

	private void createPDF(File outfile, List<File> infiles) {
		try {
			Document document = new Document();
			PdfCopy copy = new PdfCopy(document, new FileOutputStream(outfile));
			document.open();

			for (File file : infiles) {
				if (file.getName().endsWith(".pdf")) {
					appendPDF(copy, file);
				} else {
					appendImage(copy, file);
				}
				document.newPage();
			}
			document.close();

			if (addPages) {
				addPageNumbers(outfile);
			}
			if (openAfterCreate) {
				Desktop.getDesktop().browse(outfile.toURI());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void appendPDF(PdfCopy copy, File fileToAppend) {
		try {
			PdfReader reader = new PdfReader(fileToAppend.toURI().toURL());
			for (int i = 1; i <= reader.getNumberOfPages(); i++) {
				copy.addPage(copy.getImportedPage(reader, i));
			}
		} catch (Exception e) {
			System.out.println("Appending PDF " + fileToAppend.getName() + " failed.");
			e.printStackTrace();
		}
	}

	private void appendImage(PdfCopy copy, File imageFile) {
		try {
			// create temp image pdf
			String tempFile = imageFile.getAbsolutePath();
			if (tempFile.contains("\\")) {
				tempFile = tempFile.substring(0, tempFile.lastIndexOf("\\")) + "\\temp.pdf";
			} else {
				tempFile = tempFile.substring(0, tempFile.lastIndexOf("/")) + "/temp.pdf";
			}

			Document tempDoc = new Document();
			PdfWriter.getInstance(tempDoc, new FileOutputStream(tempFile));

			tempDoc.open();
			// tempDoc.setMargins(0, 0, 0, 0);
			tempDoc.setPageSize(PageSize.A4);
			tempDoc.newPage();

			Image img = Image.getInstance(imageFile.toURI().toURL());
			img.scaleToFit(PageSize.A4.getWidth(), PageSize.A4.getHeight());
			tempDoc.add(img);

			tempDoc.close();

			// append the temp pdf
			appendPDF(copy, new File(tempFile));

			// delete the temp pdf
			new File(tempFile).delete();
		} catch (Exception e) {
			System.out.println("Appending Image " + imageFile.getName() + " failed.");
			e.printStackTrace();
		}
	}

	public void addPageNumbers(File outfile) {
		try {
			String tempFilePath = outfile.getAbsolutePath();
			if (tempFilePath.contains("\\")) {
				tempFilePath = tempFilePath.substring(0, tempFilePath.lastIndexOf("\\")) + "\\temp.pdf";
			} else {
				tempFilePath = tempFilePath.substring(0, tempFilePath.lastIndexOf("/")) + "/temp.pdf";
			}
			File tempFile = new File(tempFilePath);
			Files.copy(outfile.toPath(), tempFile.toPath());

			PdfReader reader = new PdfReader(tempFile.getAbsolutePath());
			int n = reader.getNumberOfPages();
			PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(outfile.getAbsolutePath()));
			PdfContentByte pagecontent;
			for (int i = 0; i < n;) {
				pagecontent = stamper.getOverContent(++i);
				pagecontent.setFontAndSize(BaseFont.createFont(BaseFont.HELVETICA, "UTF-8", false), 10);
				ColumnText.showTextAligned(pagecontent, Element.ALIGN_RIGHT,
						new Phrase(String.format("Seite %s von %s", i, n)), 525, 50, 0);
			}
			stamper.close();
			reader.close();
		} catch (Exception e) {
			System.out.println("Failed to add page numbers");
			e.printStackTrace();
		}
	}
}