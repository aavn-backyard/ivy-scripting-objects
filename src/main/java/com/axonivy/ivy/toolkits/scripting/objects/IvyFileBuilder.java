package com.axonivy.ivy.toolkits.scripting.objects;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import ch.ivyteam.ivy.scripting.objects.Binary;
import ch.ivyteam.ivy.scripting.objects.File;
import ch.ivyteam.ivy.scripting.objects.util.IIvyScriptObjectEnvironment;
import ch.ivyteam.ivy.scripting.objects.util.IvyScriptObjectEnvironment;

/**
 * <p>
 * A more convenience way to create files in Axon.ivy environment. This class
 * provide more useful API to work with Axon.ivy {@code File}.
 * </p>
 * 
 * <h2>Typical Uses</h2>
 * 
 * <h3>Create a session/temporary file</h3>
 * 
 * <p>
 * A session/temporary file is a file created in this session and will be
 * deleted once the session is over
 * </p>
 * 
 * <pre>
 * <code>
 *  File sessionFile = IvyFileBuilder.forName("some file.txt")
 *  	.createWithContent("This is the content".getBytes("UTF-8"));
 * </code>
 * </pre>
 * 
 *
 * <h3>Create a permanent/application file</h3>
 * 
 * <pre>
 * <code>
 *  File permanent = IvyFileBuilder.forName("some file.txt")
 *  	.storePermanently().createWithContent("This is the content".getBytes("UTF-8"));
 * </code>
 * </pre>
 * 
 * 
 * <h3>Create from a java.io.File</h3>
 * 
 * 
 * <pre>
 * <code>
 *  File permanent = IvyFileBuilder.forName("some file.txt")
 *  	.storePermanently().createWithContent("This is the content".getBytes("UTF-8"));
 *  
 *  
 *  java.io.File nowJavaFile = permanent.getJavaFile();
 *  
 *  // suppose you need to pass nowJavaFile around some modules and get it back, now you want to make it an Ivy file.
 *  
 *  File stillSamePermanent = IvyFileBuilder.copyFromJavaFile(nowJavaFile);
 * </code>
 * </pre>
 * 
 */
public class IvyFileBuilder {

	private String desiredName;
	private boolean createSessionTempFile = true;
	private boolean createInUniqueFolder = true;

	/**
	 * Builds a new file with given {@code desiredName}. The file, by default,
	 * is in {@link #thisSessionOnly()} and is {@link #putInUniqueFolder()}.
	 */
	public static IvyFileBuilder forName(String desiredName) {
		if (Objects.toString(desiredName, "").trim().isEmpty()) {
			throw new IllegalArgumentException("File name should not be null or empty");
		}
		return new IvyFileBuilder(desiredName).thisSessionOnly().putInUniqueFolder();
	}

	/**
	 * <p>
	 * Builds a <strong>session</strong> file whose name following the format
	 * {@code tempXXXX.temp} whereas {@code XXXX} is the
	 * {@code System#currentTimeMillis()}. The file, by default, is in
	 * {@linkplain #thisSessionOnly()} and is {@link #putInUniqueFolder()}.
	 * </p>
	 * 
	 * <p>
	 * This is very useful when you want to initialize a {@code null-object} for
	 * a {@linkplain File} instance instead of letting Axon.ivy engine
	 * automatically does it. Because if Axon.ivy engine auto-initializes a
	 * {@code File} instance, it would points directly to the File Area which is
	 * not a very good place.
	 * </p>
	 * 
	 */
	public static IvyFileBuilder empty() {
		String temporaryFileName = "temp" + Long.toString(System.currentTimeMillis()) + ".temp";
		return new IvyFileBuilder(temporaryFileName).thisSessionOnly().putInUniqueFolder();
	}

	/**
	 * <p>
	 * Quick way to convert from an {@linkplain java.io.File} into
	 * {@linkplain File}.
	 * </p>
	 * 
	 * <p>
	 * This method is smart enough to detect whether the given {@code javaFile}
	 * actually references to an Ivy {@code File} and returns the instance
	 * without creating any new file at all.
	 * </p>
	 */
	public static File copyFromJavaFile(java.io.File javaFile) {
		if (javaFile == null || !javaFile.isFile()) {
			throw new IllegalArgumentException("The java file is invalid (not exist or not a file): " + javaFile);
		}

		Path sourcePath = javaFile.toPath();
		java.io.File sessionFileArea = IvyScriptObjectEnvironment.getIvyScriptObjectEnvironment().getTempFileArea();
		if (sourcePath.startsWith(sessionFileArea.toPath())) {
			return IvyFileBuilder.forName(javaFile.getName()).thisSessionOnly().putDirectlyInTargetFolder().getFile();
		}

		java.io.File permanentFileArea = IvyScriptObjectEnvironment.getIvyScriptObjectEnvironment().getFileArea();
		if (sourcePath.startsWith(permanentFileArea.toPath())) {
			return IvyFileBuilder.forName(javaFile.getName()).storePermanently().putDirectlyInTargetFolder().getFile();
		}

		try {
			Binary content = new Binary(Files.readAllBytes(sourcePath));
			return IvyFileBuilder.forName(javaFile.getName()).thisSessionOnly().putInUniqueFolder()
					.createFileWithContent(content);
		} catch (IOException readingFileFailed) {
			throw new RuntimeException("Fail to copy from java file " + javaFile, readingFileFailed);
		}
	}

	private IvyFileBuilder(String desiredName) {
		this.desiredName = desiredName;
	}

	/**
	 * Instructs the builder to create file into <em>temporary/session</em> file
	 * area. <strong>This is the default behavior</strong>.
	 * 
	 * @see IIvyScriptObjectEnvironment#getTempFileArea()
	 */
	public IvyFileBuilder thisSessionOnly() {
		createSessionTempFile = true;
		return this;
	}

	/**
	 * Instructs the builder to create file into <em>persistent</em> file area.
	 * 
	 * @see IIvyScriptObjectEnvironment#getFileArea()
	 */
	public IvyFileBuilder storePermanently() {
		createSessionTempFile = false;
		return this;
	}

	/**
	 * Instructs this builder to generate a unique token and put the file into
	 * it. This was done to avoid name duplication in the directories.
	 * <strong>This is the default behavior</strong>. Neutralize this behavior
	 * by using {@link #putDirectlyInTargetFolder()}.
	 */
	public IvyFileBuilder putInUniqueFolder() {
		this.createInUniqueFolder = true;
		return this;
	}

	/**
	 * Instructs this builder to put the file directly into the folder (either
	 * {@link #thisSessionOnly()} or {@link #storePermanently()}) without
	 * generating a <strong>unique<strong> token directory.
	 * 
	 * @see #putInUniqueFolder()
	 * 
	 */
	public IvyFileBuilder putDirectlyInTargetFolder() {
		this.createInUniqueFolder = false;
		return this;
	}

	/**
	 * Creates a file using the instructed settings and write/replace the given
	 * {@code content} into the file.
	 * 
	 * @throws RuntimeException
	 *             if the file cannot be created or written
	 */
	public File createFileWithContent(Binary content) {
		try {
			File created = createFile();
			created.writeBinary(content);
			return created;
		} catch (IOException writingContentFailed) {
			throw new RuntimeException("Create file with content failed.", writingContentFailed);
		}
	}

	/**
	 * Creates a file using the instructed settings.
	 * 
	 * @throws RuntimeException
	 *             if the file cannot be created
	 */
	public File createFile() {
		try {
			File newFile = tryCreatingFile();
			newFile.createNewFile();
			return newFile;
		} catch (IOException fileCreatedFail) {
			throw new RuntimeException(fileCreatedFail);
		}
	}

	/**
	 * Creates only <strong>an instance</strong> of the {@linkplain File}. There
	 * will be no physical file created in the file area.
	 * 
	 * @throws RuntimeException
	 *             if the file cannot be created
	 */
	public File getFile() {
		try {
			return tryCreatingFile();
		} catch (IOException fileCreatingFailed) {
			throw new RuntimeException(fileCreatingFailed);
		}
	}

	private File tryCreatingFile() throws IOException {
		String desiredLocation = uniqueFolder() + desiredName;
		return new File(desiredLocation, createSessionTempFile);
	}

	private String uniqueFolder() {
		return createInUniqueFolder ? Long.toHexString(System.currentTimeMillis()) + "/" : "";
	}

}
