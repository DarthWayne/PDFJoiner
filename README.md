# PDFJoiner
This is a small Java command line tool to merge different pdfs and images into one single pdf. <br>
<br>
java -jar PDFJoiner.jar "flags" "outfile" "infile_1" "infile_2" ... "infile_n"

## Flags
-h /t show help page <br>
-? /t show help page <br>
-o /t open created pdf <br>
-p /t add page numbers <br>

## Usage Example
java -jar PDFJoiner.jar -op out.pdf in01.pdf in02.jpg <br>
java -jar PDFJoiner.jar out.pdf in01.pdf in02.pdf
