package pe.edu.rgms.strategies;

public class GspStrategy implements ParseStrategy{

	public String parseLine(String line){
		if (line.startsWith("<!-- #if")) {
			line = line.replaceFirst("<!-- #if", "#if");
			line = line.replaceFirst("-->", "");
		}
		if (line.startsWith("<!-- #end")) {
			line = line.replaceFirst("<!-- #end", "#end");
			line = line.replaceFirst("-->", "");
		}
		line = line.replaceAll("\\{", "\\{ ");
		line = line.replaceAll("\\}", " \\}");
		return line;
	}
}
