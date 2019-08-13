package de.hhu.stups.codegenerator.cpp;


import de.hhu.stups.codegenerator.CodeGenerator;
import de.hhu.stups.codegenerator.GeneratorMode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * Created by fabian on 30.08.18.
 */

public class TestCpp {

	public static void writeInputToOutput(InputStream inputStream, OutputStream outputStream) throws IOException {
		int size;
		byte[] buffer = new byte[1024];
		while ((size = inputStream.read(buffer)) != -1)
			outputStream.write(buffer, 0, size);
	}

	public static void writeInputToSystem(InputStream inputStream) throws IOException {
		writeInputToOutput(inputStream, System.err);
	}

	private static String streamToString(InputStream inputStream) throws IOException {
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length;
		while ((length = inputStream.read(buffer)) != -1) {
			result.write(buffer, 0, length);
		}
		return result.toString();
	}


	public void testCpp(String machine) throws Exception {
		Path mchPath = Paths.get(CodeGenerator.class.getClassLoader()
				.getResource("de/hhu/stups/codegenerator/" + machine + ".mch").toURI());
		CodeGenerator codeGenerator = new CodeGenerator();
		List<Path> cppFilePaths = codeGenerator.generate(mchPath, GeneratorMode.CPP, false, String.valueOf(Integer.MIN_VALUE), String.valueOf(Integer.MAX_VALUE), "10", true, null);


		cppFilePaths.forEach(path -> {
			try {
				Process process = Runtime.getRuntime()
						.exec("g++ -std=c++14 -O2 -march=native -g -DIMMER_NO_THREAD_SAFETY -c " + path.toFile().getAbsoluteFile().toString());
				writeInputToSystem(process.getErrorStream());
				writeInputToOutput(process.getErrorStream(), process.getOutputStream());
				process.waitFor();
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		});

		Set<File> oFiles = cppFilePaths.stream()
				.map(path -> new File(path.getParent().toFile(), machine + ".o"))
				.collect(Collectors.toSet());

		//javaFilePaths.forEach(path -> cleanUp(path.toString()));
		//classFiles.forEach(path -> cleanUp(path.getAbsolutePath().toString()));
		//cFilePaths.forEach(path -> cleanUp(path.toString()));
	}


	public void testCpp(String machine, String machineName, String addition, boolean execute) throws Exception {
		Path mchPath = Paths.get(CodeGenerator.class.getClassLoader()
				.getResource("de/hhu/stups/codegenerator/" + machine + ".mch").toURI());
		CodeGenerator codeGenerator = new CodeGenerator();
		List<Path> cppFilePaths = codeGenerator.generate(mchPath, GeneratorMode.CPP, false, String.valueOf(Integer.MIN_VALUE), String.valueOf(Integer.MAX_VALUE), "10", true, addition);

		Runtime runtime = Runtime.getRuntime();

		Path mainPath = cppFilePaths.get(cppFilePaths.size() - 1);

		Process compileProcess = runtime
				.exec("g++ -std=c++14 -O2 -flto -march=native -g -DIMMER_NO_THREAD_SAFETY -o " + machineName + ".exec" + " out/test/resources/de/hhu/stups/codegenerator/" + machine + ".cpp" + " out/test/resources/de/hhu/stups/codegenerator/BString.cpp out/test/resources/de/hhu/stups/codegenerator/BStruct.cpp out/test/resources/de/hhu/stups/codegenerator/BInteger.cpp out/test/resources/de/hhu/stups/codegenerator/BBoolean.cpp");
		compileProcess.waitFor();

		String error = streamToString(compileProcess.getErrorStream());
		if(!error.isEmpty()) {
			throw new RuntimeException(error);
		}

		if(!execute) {
			return;
		}

		Process executeProcess = runtime.exec("./" + machineName + ".exec");
		executeProcess.waitFor();

		error = streamToString(executeProcess.getErrorStream());
		if(!error.isEmpty()) {
			throw new RuntimeException(error);
		}

		String result = streamToString(executeProcess.getInputStream()).replaceAll("\n", "");
		String expectedOutput = streamToString(new FileInputStream(mainPath.toFile().getAbsoluteFile().toString().replaceAll(".cpp", ".out"))).replaceAll("\n", "");

		System.out.println("Assert: " + result + " = " + expectedOutput);

		assertEquals(result, expectedOutput);

		Set<File> execFiles = cppFilePaths.stream()
				.map(path -> new File(path.getParent().toFile(), machine + ".exec"))
				.collect(Collectors.toSet());

		//javaFilePaths.forEach(path -> cleanUp(path.toString()));
		//classFiles.forEach(path -> cleanUp(path.getAbsolutePath().toString()));
		//cFilePaths.forEach(path -> cleanUp(path.toString()));
	}

	private void cleanUp(String path) {
		File file = new File(path);
		if (file.exists()) {
			file.delete();
		}
	}

}
