# PDFJoiner
This is a small Java command line tool to merge different pdfs and images into one single pdf. 

java -jar PDFJoiner.jar <flags> <outfile> <infile 1> <infile 2> ... <infile n>

# Flags
-h /t show this page
-? /t show this page
-o /t open created pdf
-p /t add page numbers

# Usage Example
java -jar PDFJoiner.jar -op out.pdf in01.pdf in02.jpg
java -jar PDFJoiner.jar out.pdf in01.pdf in02.pdf
