package loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Reconstructor {
	private String controlled_in_dir = "/Users/watermelon/Dropbox/EECS584/Project/data/controlled_group";
	private String debugger_in_dir = "/Users/watermelon/Dropbox/EECS584/Project/data/debugger_group";
	private String controlled_out_dir = "/Users/watermelon/Dropbox/EECS584/Project/data/controlled_group_mark";
	private String debugger_out_dir = "/Users/watermelon/Dropbox/EECS584/Project/data/debugger_group_mark";
	private String encoding = "UTF-8";
	
	public Reconstructor(){}
	
	public void Process()
	{
		ProcessEach(controlled_in_dir, controlled_out_dir);
		ProcessEach(debugger_in_dir, debugger_out_dir);
	}
	
	private void ProcessEach(String inDir, String outDir)
	{
		File directory = new File(inDir);
		File[] files = directory.listFiles();
		
		String outFile = outDir + "summary.csv";
		PrintWriter writer;
		try {
			writer = new PrintWriter(outFile, encoding);
			for(int i = 0; i < files.length; i++)
			{
				String curFile = files[i].toString();
				System.out.println(curFile);
				System.out.println(outFile);
				BufferedReader br = new BufferedReader(new FileReader(curFile));
				
				String line = "";
				while ((line = br.readLine()) != null) 
				{
					line = i +"," + line;
					writer.println(line);
				} 
				br.close();
			}
			writer.close();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
