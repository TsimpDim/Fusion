package control;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.utils.PdfMerger;

public class PdfWorkspace {

	public static int totalFiles = 0;
	public static int totalFilesToMerge = 0;
	private ArrayList<PdfFile> allFiles = new ArrayList<PdfFile>();
	
	/***
	 * Merge all the files in the allFiles ArrayList
	 * @return The amount of files merged
	 */
	public int MergePDFs(String destination){
		
		PdfDocument pdf = null;
		int filesMerged = 0;
		
		// Check if the conditions of the Workspace allow for files to be merged
		if(totalFilesToMerge <= 0 || totalFiles <= 0) {
			JOptionPane.showMessageDialog(null, "No files to merge found.", "Warning", JOptionPane.WARNING_MESSAGE);
			return -1;
		}
		
		// Open the new file
		try {
			pdf = new PdfDocument(new PdfWriter(destination));
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(null, "Could not save document to specified destination.", "Warning", JOptionPane.WARNING_MESSAGE);
			e.printStackTrace();
		}
		
		
		PdfMerger merger = new PdfMerger(pdf);
		
		// Parse every file in the workspace and add it to the new file
		for(PdfFile file : allFiles) {
			
			if(file.getToMerge()) {
				PdfDocument sourcePdf = null;
				try {
					sourcePdf = new PdfDocument(new PdfReader(file.getPath()));
				} catch (IOException e) {
					JOptionPane.showMessageDialog(null, "Could not read file at " + file.getPath(), "Warning", JOptionPane.WARNING_MESSAGE);
					e.printStackTrace();
					continue;
				}
				merger.merge(sourcePdf, file.getPages());
			
				filesMerged++;
				sourcePdf.close();
			}
		}

		pdf.close();
		return filesMerged;
	}
	
	/**
	 * Add a new PdfFile into the PdfWorkspace
	 * @param p The PdfFile to add
	 * @return True if the file was added successfully, False if the file is already in the PdfWorkspace
	 */
	public boolean AddPdfToWorkspace(PdfFile p) {
		if(allFiles.contains(p))
			return false;
		else {
			allFiles.add(p);
			totalFiles++;
			totalFilesToMerge++;
			
			return true;
		}
	}
	
	@Override
	public String toString() {
		String finalString = "----- WORKSPACE\n";
		
		for(PdfFile f : allFiles)
			finalString += f.getPath() + ',' + String.valueOf(f.getFileId()) + ',' + String.valueOf(f.getToMerge()) + '\n';
		
		return finalString;
	}

	public ArrayList<PdfFile> getAllFiles() {
		return allFiles;
	}
}
