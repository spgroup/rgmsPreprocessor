package pe.edu.rgms.strategies;

public class GroovyStrategy implements ParseStrategy {

	public String parseLine(String line){
		line = line.replaceFirst("//#if", "#if");
		line = line.replaceFirst("//#end", "#end");
		return line;
	}
}
