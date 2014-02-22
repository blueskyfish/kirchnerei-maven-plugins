/*
 * Copyright (c) 2014. Kirchner
 * web:  http://www.kirchnerei.de
 * mail: mulder3@kirchnerei.de
 */
package kirchnerei.buildnumber.plugin;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 * Kirchnerei BuildNumber Plugin (equals to ant buildnumber)
 */
@Mojo(name = "create", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class BuildNumberMojo extends AbstractMojo {

	public static final String PROPERTY_COMMENT = "This is the build number. Do not change directly";

	/**
	 * Location of the file.
	 */
	@Parameter(defaultValue = "${basedir}",
		property = "outputDirectory",
		required = true)
	private File outputDirectory;

	/**
	 * The name of the file.
	 */
	@Parameter(defaultValue = "build.properties",
		property = "buildFile")
	private String buildFile;

	/**
	 * The name of the property value in which the buildnumber will be store.
	 */
	@Parameter(defaultValue = "buildnumber", property = "propertyName")
	private String propertyName;

	@Parameter(defaultValue = "false", property = "increment")
	private boolean increment;

	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	private MavenProject project;

	public void execute() throws MojoExecutionException {
		try {
			File file = new File(outputDirectory, buildFile);

			int buildNumber = readBuildNumberFrom(file);
			if (buildNumber <= 0) {
				logInfo("create new build number with '1'");
				buildNumber = 1;
			} else {
				buildNumber++;
				logInfo("build number '" + buildNumber + "' will be use");
			}
			writeBuildNumberTo(file, buildNumber);
			Properties prop = project.getProperties();
			prop.put(propertyName, String.valueOf(buildNumber));
		} catch (IOException e) {
			logError("could not handle the build number", e);
			throw new MojoExecutionException("could not handle the build number", e);
		}
	}

	int readBuildNumberFrom(File file) throws IOException {
		if (!file.exists()) {
			return -1;
		} else {
			Properties prop = new Properties();
			InputStream input = new FileInputStream(file);
			prop.load(input);
			input.close();
			return NumberUtils.toInt(prop.getProperty(propertyName, "1"), 1);
		}
	}

	void writeBuildNumberTo(File file, int buildNumber) throws IOException {
		if (!increment) {
			logInfo("no increment the buildnumber.");
			return;
		}
		if (!file.exists()) {
			File parent = file.getParentFile();
			parent.mkdirs();
			file.createNewFile();
		}
		logInfo("store the buildnumber");
		OutputStream output = new FileOutputStream(file);
		Properties prop = new Properties();
		prop.setProperty(propertyName, String.valueOf(buildNumber));
		prop.store(output, PROPERTY_COMMENT);
	}

	/**
	 * Log info.
	 *
	 * @param message the message to log.
	 */
	void logInfo(final String message) {
		if (getLog().isInfoEnabled()) {
			getLog().info(message);
		}
	}

	/**
	 * Log error.
	 *
	 * @param message the message.
	 * @param e the exception.
	 */
	protected void logError(final String message, final Exception e) {
		if (getLog().isErrorEnabled()) {
			getLog().error(message, e);
		}
	}
}
