package org.collection.fm;

import java.io.*;
import java.util.List;

import org.collection.fm.analyses.*;
import org.collection.fm.util.FMUtils;
import org.collection.fm.util.FileUtils;

public class FeatureModelStructureAnalysis {

	AnalysisHandler analysisHandler;
	int timeout;

	private static final int UPDATE_CSV_INTERVALL = 5;

	public FeatureModelStructureAnalysis() {
		initializeAnalyses();
		timeout = 30;
	}

	public static void main(String[] args) {
		FeatureModelStructureAnalysis analysis = new FeatureModelStructureAnalysis();

		FMUtils.installLibraries();
		if (args.length < 1) {
			System.out.println("Mandatory argument([0]): Input path\n" + "Optional Argument([1]): Output path");
			return;
		}

		List<File> files = FileUtils.getFileList(args[0]);

		analysis.handleFiles(files, args[0], args.length == 1 ? "result.csv" : args[1]);
	}

	private void handleFiles(List<File> files, String inputPath, String outputfile) {
		try(PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(outputfile)))) {
			writer.println(analysisHandler.getCsvHeader());
			files.stream().map(f -> handleFile(f, inputPath)).forEachOrdered(writer::print);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
	}

	private String handleFile(File file, String inputPath) {
		System.out.println("Handling " + file.getPath());
		return analysisHandler.evaluateFmFile(file, timeout, inputPath);
	}

	private void initializeAnalyses() {
		analysisHandler = new AnalysisHandler();
		analysisHandler.registerAnalysis(new NumberOfFeatures());
		analysisHandler.registerAnalysis(new NumberOfLeafFeatures());
		analysisHandler.registerAnalysis(new NumberOfTopFeatures());

		analysisHandler.registerAnalysis(new NumberOfConstraints());
		analysisHandler.registerAnalysis(new AverageConstraintSize());
		analysisHandler.registerAnalysis(new CtcDensity());
		analysisHandler.registerAnalysis(new FeaturesInConstraintsDensity());
		
		analysisHandler.registerAnalysis(new TreeDepth());
		analysisHandler.registerAnalysis(new AverageNumberOfChilden());
		
		analysisHandler.registerAnalysis(new NumberOfClauses());
		analysisHandler.registerAnalysis(new NumberOfLiterals());
		analysisHandler.registerAnalysis(new ClauseDensity());
		
		analysisHandler.registerAnalysis(new RatioOfOptionalFeatures());
		analysisHandler.registerAnalysis(new ConnectivityDensity());
		
		analysisHandler.registerAnalysis(new VoidModel());
		analysisHandler.registerAnalysis(new NumberOfCoreFeatures());
		analysisHandler.registerAnalysis(new NumberOfDeadFeatures());

		analysisHandler.registerAnalysis(new NumberOfValidConfigurations());

		analysisHandler.registerAnalysis(new SimpleCyclomaticComplexity());
		analysisHandler.registerAnalysis(new IndependentCyclomaticComplexity());
		analysisHandler.registerAnalysis(new SATZilla());
	}

}
