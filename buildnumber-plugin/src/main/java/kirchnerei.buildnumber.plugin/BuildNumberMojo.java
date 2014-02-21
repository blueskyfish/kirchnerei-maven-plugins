/*
 * Copyright (c) 2014. Kirchner
 * web:  http://www.kirchnerei.de
 * mail: mulder3@kirchnerei.de
 */
package kirchnerei.buildnumber.plugin;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 * kirchnerei buildnumber plugin (equals to ant buildnumber)
 *
 * @author <a href="kirchnerei@gmx.net">Mulder3</a>
 * @goal create
 * @requiresProject
 * @threadsafe true
 * @phase validate
 * @describe Lightweight mojo to create a buildnumber
 */
public class BuildNumberMojo extends AbstractMojo {

	public static final String PROPERTY_COMMENT = "This is the build number. Do not change directly";
	/**
	 * Location of the file.
	 *
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private File outputDirectory;

	/**
	 * The name of the file.
	 *
	 * @parameter default-value="build.properties"
	 */
	private String buildFile;

	/**
	 * The name of the property value in which the buildnumber will be store.
	 *
	 * @parameter default-value="buildnumber"
	 */
	private String propertyName;

	/**
	 * Flag for changing the buildnumber
	 *
	 * @parameter default-value=false
	 */
	private boolean increment;

	/**
	 * The maven project.
	 *
	 * @parameter expression="${project}"
	 * @required
	 * @readonly
	 */
	private MavenProject project;

	public void execute() throws MojoExecutionException {
		try {
			File file = new File(outputDirectory, buildFile);

			int buildNumber = readBuildNumberFrom(file);
			if (buildNumber <= 0) {
				getLog().info("create new build number with '1'");
				buildNumber = 1;
			} else {
				buildNumber++;
				getLog().info("build number '" + buildNumber + "' will be use");
			}
			writeBuildNumberTo(file, buildNumber);
			Properties prop = project.getProperties();
			prop.put(propertyName, String.valueOf(buildNumber));
		} catch (IOException e) {
			getLog().error(e);
			throw new MojoExecutionException("could not handle the build number", e);
		}
	}

	public void setProject(MavenProject project) {
		this.project = project;
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
			getLog().info("no increment the buildnumber.");
			return;
		}
		if (!file.exists()) {
			File parent = file.getParentFile();
			parent.mkdirs();
			file.createNewFile();
		}
		getLog().info("store the buildnumber");
		OutputStream output = new FileOutputStream(file);
		Properties prop = new Properties();
		prop.setProperty(propertyName, String.valueOf(buildNumber));
		prop.store(output, PROPERTY_COMMENT);
	}
}
