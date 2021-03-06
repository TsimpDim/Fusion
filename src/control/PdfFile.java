package control;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class PdfFile extends File{


	private static final long serialVersionUID = -8832254767996433033L;
	
	private String path;
	private ArrayList<Integer> pages = new ArrayList<>();
	private ArrayList<Integer> availablePages = null;
	private Integer numberOfAvailablePages = -1;
	private String pageInput = "All"; // User input in the 'pages' cell
	private Boolean toInclude;
	private int fileId;
	
	/**
	 * Initializes a new {@link PdfFile} with page restrictions
	 * @param path The file path
	 * @param pages An {@link ArrayList} with all the pages to use. Set null for full pages.
	 * @param toInclude Whether or not this file should be merged
	 * @param fileId File specific id
	 */
	public PdfFile(String path, ArrayList<Integer> pages, Boolean toInclude, int fileId) {
		super(path);
		this.path = path;
		this.pages = pages;
		this.toInclude = toInclude;
		this.fileId = fileId;
	}
	
	/**
	 * Initializes a new {@link PdfFile} without page restrictions
	 * @param path The file path
	 * @param toInclude Whether or not this file should be merged
	 * @param fileId File specific id
	 */
	public PdfFile(String path, Boolean toInclude, int fileId) {
		super(path);
		this.path = path;
		this.pages = null;
		this.toInclude = toInclude;
		this.fileId = fileId;
	}
	
	/**
	 * Initializes a new {@link PdfFile} with a string pages parameter
	 * @param path The file path
	 * @param pages A {@link String} containing the pages to include
	 * @param toInclude Whether or not this file should be merged
	 * @param fileId File specific id
	 */
	public PdfFile(String path, String pages, Boolean toInclude, int fileId) {
		super(path);
		this.path = path;
		this.setPages(pages);
		this.toInclude = toInclude;
		this.fileId = fileId;
	}

	/**
	 * Creates a new {@link PdfFile} from an existing one
	 * @param master The prototype for the new object
	 */
	public PdfFile(PdfFile master) {
		super(master.path);
		this.path = master.path;
		this.pages = master.pages;
		this.toInclude = master.toInclude;
		this.fileId = master.fileId;
	}

	/**
	 * Creates a new {@link PdfFile} from a {@link File}
	 * @param file The master file of {@link File} type
	 * @param fileId The id of the new file (workspace.totalFiles)
	 */
	public PdfFile(File file, int fileId){
		super(file.getPath());
		this.path = file.getPath();
		this.pages = null;
		this.toInclude = true;
		this.fileId = fileId;
	}


	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * getPages() will return the pages that the user inputted. If pages==null that means that
	 * we want *every* page. Otherwise we want just the given range.
	 *
	 * The difference with getAvailablePages() is that the latter always returns
	 * the pages that the file contains, which are not always the ones we want.
	 *
	 * @return An {@link ArrayList} with the range of pages.
	 */
	public ArrayList<Integer> getPages() {
		
		if(pages != null)
			return pages;
		else
			// If pages == null then we want *every* page
			return getAvailablePages();
	}

	/**
	 * Opens the current file and reads the amount of pages it has
	 * @return An {@link ArrayList} with the same size as the pages on the current file
	 */
	public ArrayList<Integer> getAvailablePages(){

		// If available pages have never been calculated
		if(this.availablePages == null) {
			PdfDocument sourcePdf = null;
			int numPages = -1;
			ArrayList<Integer> pages = new ArrayList<>();

			try {
				sourcePdf = new PdfDocument(new PdfReader(this.path));
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}

			numPages = sourcePdf.getNumberOfPages();
			sourcePdf.close();

			for (int i = 1; i < numPages + 1; i++)
				pages.add(i);

			this.availablePages = pages;

			this.numberOfAvailablePages = pages.size();
			return pages;
		}else{
			return this.availablePages;
		}
	}
	
	/**
	 * Transforms a range {@link String} into an {@link ArrayList}
	 * e.g "1,3-5" = {1,3,4,5}<br>
	 * e.g "2-" = {2,3,4,5,6....}<br>
	 * e.g "1-3,5,6-8" = {1,2,3,5,6,7,8}<br>
	 * e.g "-5" = {1,2,3,4,5}
	 * e.g "10-15" = <strong>error</strong> <i>if the file does not have a 10th page</i><br>
	 * e.g "12-32" = <strong>error</strong> <i>if the file does have a 12th page but not 32 pages</i><br>
	 * @param pages A {@link String} representation of the selection range
	 */
	public void setPages(String pages) {


		ArrayList<Integer> new_pages = new ArrayList<>();
		String[] splitStr;
		Boolean hasGivenFirstPage = false;
		Boolean hasGivenLastPage = false;

		pages = pages.replaceAll("(-)\\1+", "-"); // Remove multiple consecutive dashes
		splitStr = pages.split(Pattern.quote(","));

		if(pages.toLowerCase().equals("all") || pages.equals("-")) {
			this.pages = null;
			return;
		}

		
		for(String str : splitStr) {


			if (str.contains("-")) {

				// A page range can't have more than one dash
				// e.g "1-2-50"
				int dashAppearances = 0;
				for(char ch : str.toCharArray()){
					if(ch == '-')
						dashAppearances++;
				}

				if(dashAppearances > 1){
					showWrongInputError();
					return;
				}


				// Get all values within given range
				String[] splitRange = str.split(Pattern.quote("-"));

				int end;
				int start;

				// Handle start
				// No start given
				if(str.charAt(0) == '-') {
					if(!hasGivenFirstPage) {
						start = 1;
						hasGivenFirstPage = true;
					}else {
						// e.g "-100,-200" we can't have "1-100,1-200" so we throw an error
						showWrongInputError();
						return;
					}
				// Start given
				}else{
					start = Integer.valueOf(splitRange[0]);
					hasGivenFirstPage = true;
				}

				// Handle end
				// No end given
				if(splitRange.length == 1) { // e.g "1-" -> splitRange = {"1"} so splitRange has length = 1
					if(!hasGivenLastPage){
						end = getNumberOfAvailablePages();
						hasGivenLastPage = true;
					}else{
						// e.g "1,5,100-,200-
						showWrongInputError();
						return;
					}

				// End given
				}else{
					// End is digit
					if(splitRange[1].chars().allMatch(Character :: isDigit)){
						end = Integer.valueOf(splitRange[1]);
						hasGivenLastPage = true;
					}else{
						// e.g "1,7,100-e" or "-200,204,206-"
						showWrongInputError();
						return;
					}
				}

				// Start out of range
				if(start > getNumberOfAvailablePages()) {
					showWrongInputError();
					return;
				}


				// Add all pages within range
				for(int i = start; i < end+1; i++)
					new_pages.add(i);

			}else {
				try {
					new_pages.add(Integer.valueOf(str));
				}catch(NumberFormatException e) {
					showWrongInputError();
					return;
				}
			}
		}

		this.pages = new_pages;
		this.pageInput = pages;
	}

	public void showWrongInputError() {
		JOptionPane.showMessageDialog(null, "Wrong input given. Only numbers, commas and dashes are allowed.\n"
				+ "Make sure input is within available pages.", "Warning", JOptionPane.WARNING_MESSAGE);
		this.pages = null;
		this.pageInput = "All";
	}

	public void setPageInput(String input){
		this.pageInput = input;
	}

	public Integer getNumberOfAvailablePages(){

		if(this.numberOfAvailablePages == -1)
			getAvailablePages();

		return this.numberOfAvailablePages;
	}

	public Boolean getToInclude() {
		return toInclude;
	}

	public void setToInclude(Boolean toInclude) {
		this.toInclude = toInclude;
	}

	public int getFileId() {
		return fileId;
	}

	public void setFileId(int fileId) {
		this.fileId = fileId;
	}

	/**
	 * @return A {@link String} version of the {@link ArrayList} containing the selected pages
	 */
	public String getPagesString() {
		String finalString = "";
		
		if(pages == null)
			finalString =  "All";
		else {
			if(pageInput.charAt(0) == '-')
				finalString += '1';

			finalString += pageInput;

			if(pageInput.charAt(pageInput.length() -1) == '-')
				finalString += String.valueOf(this.getNumberOfAvailablePages());

		}
		
		return finalString;
	}
	
	
	
	
}
