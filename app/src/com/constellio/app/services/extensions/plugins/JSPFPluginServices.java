package com.constellio.app.services.extensions.plugins;

import static com.constellio.app.services.extensions.plugins.PluginActivationFailureCause.CANNOT_INSTALL_OLDER_VERSION;
import static com.constellio.app.services.extensions.plugins.PluginActivationFailureCause.INVALID_EXISTING_ID;
import static com.constellio.app.services.extensions.plugins.PluginActivationFailureCause.INVALID_VERSION;
import static com.constellio.app.services.extensions.plugins.pluginInfo.ConstellioPluginStatus.ENABLED;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.constellio.app.services.extensions.plugins.InvalidPluginJarException.InvalidPluginJarException_NoCode;
import com.constellio.app.services.extensions.plugins.InvalidPluginJarException.InvalidPluginJarException_NoVersion;
import com.constellio.app.services.extensions.plugins.pluginInfo.ConstellioPluginInfo;
import com.constellio.app.services.migrations.VersionsComparator;

public class JSPFPluginServices implements PluginServices {
	private static final Logger LOGGER = LogManager.getLogger(JSPFPluginServices.class);
	static final String NEW_JAR_EXTENSION = "jar.new";
	private static final String CODE_ATTRIBUTE_NAME = "code";
	private static final String VERSION_ATTRIBUTE_NAME = "version";
	private static final String REQUIRED_CONSTELLIO_VERSION_ATTRIBUTE_NAME = "requiered-Constellio-Version";
	private static final String REQUIRED_CONSTELLIO_VERSION_NULL_VALUE = "null";

	@Override
	public ConstellioPluginInfo extractPluginInfo(File pluginJar)
			throws InvalidPluginJarException {
		InputStream stream = null;
		JarInputStream jarStream = null;
		try {
			stream = new FileInputStream(pluginJar);
			jarStream = new JarInputStream(stream);
			Manifest mf = jarStream.getManifest();
			if (mf == null) {
				throw new InvalidPluginJarException.InvalidPluginJarException_InvalidManifest();
			} else {
				return extractPluginInfoFromManifest(mf.getMainAttributes());
			}
		} catch (IOException e) {
			throw new InvalidPluginJarException.InvalidPluginJarException_InvalidJar(e);
		} finally {
			IOUtils.closeQuietly(stream);
			IOUtils.closeQuietly(jarStream);
		}

	}

	private ConstellioPluginInfo extractPluginInfoFromManifest(Attributes attributes)
			throws InvalidPluginJarException {
		String code = null, requiredConstellioVersion = null, version = null;
		boolean codeFound = false, versionFound = false, requiredConstellioVersionFound = false;

		for (Entry<Object, Object> att : attributes.entrySet()) {
			String key = att.getKey().toString();
			if (key.equalsIgnoreCase(CODE_ATTRIBUTE_NAME)) {
				codeFound = true;
				if (att.getValue() != null && StringUtils.isNotBlank(att.getValue().toString())) {
					code = att.getValue().toString();
				}
			}
			if (key.equalsIgnoreCase(VERSION_ATTRIBUTE_NAME)) {
				versionFound = true;
				if (att.getValue() != null && StringUtils.isNotBlank(att.getValue().toString())) {
					version = att.getValue().toString();
				}
			}
			if (key.equalsIgnoreCase(REQUIRED_CONSTELLIO_VERSION_ATTRIBUTE_NAME)) {
				requiredConstellioVersionFound = true;
				if (att.getValue() != null && StringUtils.isNotBlank(att.getValue().toString())) {
					requiredConstellioVersion = att.getValue().toString();
				}
			}
			if (codeFound && versionFound && requiredConstellioVersionFound) {
				break;
			}
		}

		if (!codeFound) {
			throw new InvalidPluginJarException_NoCode();
		}
		if (!versionFound) {
			throw new InvalidPluginJarException_NoVersion();
		}
		if (StringUtils.isBlank(requiredConstellioVersion)) {
			requiredConstellioVersion = "";
		} else if (requiredConstellioVersion.equalsIgnoreCase(REQUIRED_CONSTELLIO_VERSION_NULL_VALUE)) {
			requiredConstellioVersion = "";
		}
		return new ConstellioPluginInfo().setCode(code).setRequiredConstellioVersion(requiredConstellioVersion)
				.setVersion(version);
	}

	@Override
	public PluginActivationFailureCause validatePlugin(ConstellioPluginInfo newPluginInfo,
			ConstellioPluginInfo previousPluginInfo) {
		if (StringUtils.isBlank(newPluginInfo.getCode())) {
			return INVALID_EXISTING_ID;
		}
		String version = newPluginInfo.getVersion();
		String previousVersion = null;
		if (previousPluginInfo != null && previousPluginInfo.getPluginStatus() == ENABLED) {
			previousVersion = previousPluginInfo.getVersion();
		}

		return validateVersions(version, previousVersion);
	}

	private PluginActivationFailureCause validateVersions(String version, String oldVersion) {
		if (StringUtils.isBlank(version)) {
			return INVALID_VERSION;
		}
		Pattern pattern = Pattern.compile("^(\\d+\\.)?(\\d+\\.)?(\\d+)$");
		Matcher matcher = pattern.matcher(version);
		if (matcher.matches()) {
			if (oldVersion != null &&
					VersionsComparator.isFirstVersionBeforeSecond(version, oldVersion)) {
				return CANNOT_INSTALL_OLDER_VERSION;
			}
		} else {
			return INVALID_VERSION;
		}
		return null;
	}

	public void saveNewPlugin(File pluginsDirectory, File newPluginFile, String pluginCode)
			throws IOException {
		FileUtils.copyFile(newPluginFile, new File(pluginsDirectory, pluginCode + "." + NEW_JAR_EXTENSION));
	}

	@Override
	public void replaceOldPluginVersionsByNewOnes(File pluginsDirectory, File oldVersionsDestinationDirectory)
			throws PluginsReplacementException {
		List<String> pluginsWithReplacementException = new ArrayList<>();
		for (File newJarVersionFile : FileUtils.listFiles(pluginsDirectory, new String[] { NEW_JAR_EXTENSION }, false)) {
			String newVersionFilePath = newJarVersionFile.getPath();
			String previousVersionFilePath = newVersionFilePath.substring(0, newVersionFilePath.length() - 4);
			File previousVersionFile = new File(previousVersionFilePath);
			File oldVersionFile = new File(oldVersionsDestinationDirectory, previousVersionFile.getName());
			FileUtils.deleteQuietly(oldVersionFile);
			try {
				if (previousVersionFile.exists()) {
					FileUtils.moveFile(previousVersionFile, oldVersionFile);
					FileUtils.deleteQuietly(previousVersionFile);
				}
				FileUtils.moveFile(newJarVersionFile, previousVersionFile);
			} catch (IOException e) {
				String pluginId = StringUtils.substringBeforeLast(newJarVersionFile.getName(), "." + NEW_JAR_EXTENSION);
				LOGGER.error("Error when trying to replace old plugin " + pluginId, e);
				pluginsWithReplacementException.add(pluginId);
			}
		}
		if (!pluginsWithReplacementException.isEmpty()) {
			throw new PluginsReplacementException(pluginsWithReplacementException);
		}
	}

	@Override
	public File getPluginJar(File pluginsDirectory, String pluginId) {
		File pluginFie = new File(pluginsDirectory, pluginId + ".jar");
		if (pluginFie.exists()) {
			return pluginFie;
		}
		return null;
	}

}