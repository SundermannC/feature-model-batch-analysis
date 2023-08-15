package org.collection.fm.analyses;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.collection.fm.util.BinaryRunner;
import org.collection.fm.util.FileUtils;
import org.collection.fm.util.BinaryRunner.*;
import org.prop4j.Node;

import de.ovgu.featureide.fm.core.analysis.cnf.formula.FeatureModelFormula;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.io.dimacs.DimacsWriter;

/**
 * 
 */
public class NumberOfValidConfigurations implements IFMAnalysis {


    private static final String LABEL = "NumberOfValidConfigurations";


    private static final String TEMPORARY_DIMACS_PATH = "temp.dimacs";


    private static final String UNSAT_FLAG = "s 0";


    private final static String BINARY_PATH = "solver" + File.separator + "d4";

    @Override
    public String getLabel() {
        return LABEL;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public String getResult(IFeatureModel featureModel, FeatureModelFormula formula, int timeout) {
		Path dir = null;
		try {
			dir = createTemporaryDimacs(formula);
			BinaryResult result = null;
			result = executeSolver(dir.resolve(TEMPORARY_DIMACS_PATH).toString(), timeout);
			cleanUpTemp(dir);
			if (result.status == Status.TIMEOUT) {
				return "?";
			}
			if (result.status == Status.SOLVED) {
				return parseResult(result.stdout);
			}
		} catch (IOException e) {
			return "?";
		}
		return "?";
    }
	
	public static Path createTemporaryDimacs(FeatureModelFormula formula) throws IOException {
		Path tempDir = Files.createTempDirectory("SATfeatPy");
		String cnfPath = tempDir.resolve(TEMPORARY_DIMACS_PATH).toString();
		final DimacsWriter dWriter = new DimacsWriter(formula.getCNF());
		final String dimacsContent = dWriter.write();
		FileUtils.writeContentToFile(cnfPath, dimacsContent);

		return tempDir;
    }

	public static void cleanUpTemp(Path path){
		File[] files = path.toFile().listFiles();
		if (files != null) Arrays.stream(files).forEach(File::delete);
		path.toFile().deleteOnExit();
	}
    
    public BinaryResult executeSolver(String dimacsPath, long timeout) {
		String command = buildCommand(dimacsPath);
		return BinaryRunner.runBinaryStatic(command, timeout);
	}
    
    private String buildCommand(String dimacsPath) {
		return BINARY_PATH + " -i " + dimacsPath + " -m counting";
    }
    

    public String parseResult(String output) {
		if (isUNSAT(output)) {
			return "0";
		}
		final Pattern pattern = Pattern.compile("^s \\d*", Pattern.MULTILINE);
		final Matcher matcher = pattern.matcher(output);
		String result = "";
		if (matcher.find()) {
			result = matcher.group();
		} else {
			return "?";
		}
		final String[] split = result.split(" ");
		return split[split.length - 1];
    }
    

    private boolean isUNSAT(String output) {
		return output.contains(UNSAT_FLAG);
	}

	@Override
	public String getResult(Node node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean supportsFormat(Format format) {
		// TODO Auto-generated method stub
		return false;
	}
}