package control;
import java.io.File;
import java.util.ArrayList;

public class PdfFile extends File{


	private static final long serialVersionUID = -8832254767996433033L;
	
	private String path;
	private ArrayList<Integer> pages = new ArrayList<Integer>();
	private Boolean toMerge;
	private int fileId;
	
	/***
	 * Initalize a new PdfFile with page restrictions
	 * @param path The file path
	 * @param pages An ArrayList with all the pages to use. Set null for full pages.
	 * @param toMerge 
	 * @param fileId
	 */
	public PdfFile(String path, ArrayList<Integer> pages, Boolean toMerge, int fileId) {
		super(path);
		this.path = path;
		this.pages = pages;
		this.toMerge = toMerge;
		this.fileId = fileId;
	}
	
	/***
	 * Initalize a new PdfFile without page restrictions
	 * @param path The file path
	 * @param toMerge 
	 * @param fileId
	 */
	public PdfFile(String path, Boolean toMerge, int fileId) {
		super(path);
		this.path = path;
		this.pages = null;
		this.toMerge = toMerge;
		this.fileId = fileId;
	}
	

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public ArrayList<Integer> getPages() {
		return pages;
	}

	public void setPages(ArrayList<Integer> pages) {
		this.pages = pages;
	}

	public Boolean getToMerge() {
		return toMerge;
	}

	public void setToMerge(Boolean toMerge) {
		this.toMerge = toMerge;
	}

	public int getFileId() {
		return fileId;
	}

	public void setFileId(int fileId) {
		this.fileId = fileId;
	}

	public String getPagesString() {
		String finalString = "";
		
		if(pages == null)
			finalString =  "All";
		else {
			for(Integer pg : pages) 
				finalString += String.valueOf(pg) + ',';
		}
		
		return finalString;
	}
	
	
	
	
}
