/*
 * Copyright (c) 2014. Kirchner
 * web:  http://www.kirchnerei.de
 * mail: mulder3@kirchnerei.de
 */

package kirchnerei.decrypt.plugin;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.plexus.components.cipher.DefaultPlexusCipher;
import org.sonatype.plexus.components.cipher.PlexusCipher;
import org.sonatype.plexus.components.cipher.PlexusCipherException;
import org.sonatype.plexus.components.sec.dispatcher.DefaultSecDispatcher;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcherException;
import org.sonatype.plexus.components.sec.dispatcher.SecUtil;
import org.sonatype.plexus.components.sec.dispatcher.model.SettingsSecurity;

import java.io.File;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Properties;

public class EncryptMojo extends AbstractMojo {

	static final String SETTINGS_SECURITY_FILE = "settings-security.xml";
	static final String MAVEN_HOME = "env.M2_HOME";
	static final String USER_HOME = "user.home";
	static final String M2 = ".m2";


	private static final String PASSWORD_PLACEHOLDER = "\\{.*\\}";

	private static boolean alreadyProcessed = false;
	private static Properties decodedProperties = new Properties();

	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	private MavenProject project;

	@Parameter(defaultValue = "${session}", readonly = true, required = true)
	private MavenSession session;

	private PlexusCipher plexusCipher;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		// if is the first time, load security settings file and process the project properties
		if (!alreadyProcessed) {
			try {
				logInfo("Starting to process project properties...");

				final SettingsSecurity settingsSecurity = getSettingsSecurity();
				final String plainTextMasterPassword =
					getPlexusCipher().decryptDecorated(settingsSecurity.getMaster(),
						DefaultSecDispatcher.SYSTEM_PROPERTY_SEC_LOCATION);

				final Properties projectProperties = this.getProject().getProperties();
				final Enumeration keys = projectProperties.keys();
				while (keys.hasMoreElements()) {
					final String key = (String) keys.nextElement();
					final String value = (String) projectProperties.get(key);
					// if property matches the pattern {.*}, try to decode it
					if (value.matches(PASSWORD_PLACEHOLDER)) {
						logInfo(MessageFormat.format("Processing property with key [{0}]", key));
						try {
							decodedProperties.setProperty(key,
								getPlexusCipher().decryptDecorated(value, plainTextMasterPassword));
						} catch (final PlexusCipherException e) {
							// warn about errors when decrypting passwords, probably the master password is not the key for this one - skip
							logWarn("Error decoding password. It seams you cannot decrypt this one" +
								"with your master password, skipping...");
						}
					}
				}
				logInfo("Finished processing project properties.");
				alreadyProcessed = true;
			} catch (final PlexusCipherException e) {
				logError("Error decoding master password.", e);
				throw new RuntimeException(MessageFormat.format("Failed to decode Master Password from {0}",
					SETTINGS_SECURITY_FILE), e);
			} catch (final SecDispatcherException e) {
				logError(MessageFormat.format("Error loading file {0}", SETTINGS_SECURITY_FILE), e);
				throw new RuntimeException(MessageFormat.format("Failed to load file {0}",
					SETTINGS_SECURITY_FILE), e);
			}
		}
		logInfo("Merging properties...");
		this.getProject().getProperties().putAll(decodedProperties);

	}

	/**
	 * Returns the {@link MavenSession} object
	 *
	 * @return mavenSession
	 */
	protected MavenSession getSession() {
		return session;
	}

	/**
	 * Returns the {@link MavenProject} object
	 *
	 * @return mavenProject
	 */
	protected MavenProject getProject() {
		return project;
	}

	/**
	 * Log info.
	 *
	 * @param message the message to log.
	 */
	protected void logInfo(final String message) {
		if (this.getLog().isInfoEnabled()) {
			this.getLog().info(message);
		}
	}

	/**
	 * Log error.
	 *
	 * @param message the message.
	 * @param e the exception.
	 */
	protected void logError(final String message, final Exception e) {
		if (this.getLog().isErrorEnabled()) {
			this.getLog().error(message, e);
		}
	}

	/**
	 * Log warning.
	 *
	 * @param message the message.
	 */
	protected void logWarn(final String message) {
		if (this.getLog().isWarnEnabled()) {
			this.getLog().warn(message);
		}
	}

	/**
	 * Returns the {@link org.sonatype.plexus.components.sec.dispatcher.model.SettingsSecurity} object.
	 *
	 * @return settingsSecurity
	 * @throws org.sonatype.plexus.components.sec.dispatcher.SecDispatcherException
	 */
	protected SettingsSecurity getSettingsSecurity() throws SecDispatcherException {
		// load execution properties to lookup for settings-security paths
		Properties executionProperties = getSession().getExecutionProperties();
		// try to load security-settings.xml from the system properties
		File securitySettings = getSettingsSecurityFile(
			executionProperties.getProperty(DefaultSecDispatcher.SYSTEM_PROPERTY_SEC_LOCATION));
		if (securitySettings == null) {
			// try to load security-settings.xml from the user home path / .m2
			securitySettings = getSettingsSecurityFile(MessageFormat.format("{0}{1}{2}",
				System.getProperty(USER_HOME), File.separator, M2));
			if (securitySettings == null) {
				//try to load security-settings.xml from the maven home path / .m2
				securitySettings = getSettingsSecurityFile(
					MessageFormat.format("{0}{1}{2}", executionProperties.getProperty(MAVEN_HOME),
						File.separator, M2));
			}
		}
		// if file cannot be found we cannot proceed
		if (securitySettings == null) {
			throw new RuntimeException(MessageFormat.format("Failed to load file {0}",
				SETTINGS_SECURITY_FILE));
		}
		// load SettingsSecurity Object
		return SecUtil.read(securitySettings.getAbsolutePath(), true);
	}

	/**
	 * Returns the {@link File} for the specified path if exists, {@code null} otherwise.
	 *
	 * @param path the path for the file
	 * @return file
	 */
	private File getSettingsSecurityFile(final String path) {
		File file = null;
		if (StringUtils.isNotBlank(path)) {
			// if path contains the file name load the file, otherwise try to load the file by it default name
			if (path.endsWith(".xml")) {
				logInfo(MessageFormat.format("Loading {0}", path));
				file = new File(path);
			} else {
				logInfo(MessageFormat.format("Loading {0}{1}{2}", path,
					File.separator, SETTINGS_SECURITY_FILE));
				final File folder = new File(path);
				file = new File(folder, SETTINGS_SECURITY_FILE);
			}
			if (!file.exists()) {
				file = null;
			}
		}
		return file;
	}


	protected PlexusCipher getPlexusCipher() throws PlexusCipherException {
		if (plexusCipher == null) {
			synchronized (EncryptMojo.class) {
				if (plexusCipher == null) {
					plexusCipher = new DefaultPlexusCipher();
				}
			}
		}
		return plexusCipher;
	}

}
