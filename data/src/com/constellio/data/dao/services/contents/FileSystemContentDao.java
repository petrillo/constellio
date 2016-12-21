package com.constellio.data.dao.services.contents;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.conf.DigitSeparatorMode;
import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.services.contents.ContentDaoException.ContentDaoException_NoSuchContent;
import com.constellio.data.dao.services.contents.ContentDaoRuntimeException.ContentDaoRuntimeException_CannotDeleteFolder;
import com.constellio.data.dao.services.contents.ContentDaoRuntimeException.ContentDaoRuntimeException_CannotMoveFolderTo;
import com.constellio.data.dao.services.contents.ContentDaoRuntimeException.ContentDaoRuntimeException_NoSuchFolder;
import com.constellio.data.dao.services.contents.FileSystemContentDaoRuntimeException.FileSystemContentDaoRuntimeException_DatastoreFailure;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.streamFactories.CloseableStreamFactory;
import com.constellio.data.utils.ImpossibleRuntimeException;
import org.apache.commons.lang3.StringUtils;

public class FileSystemContentDao implements StatefulService, ContentDao {

	private static final String COPY_RECEIVED_STREAM_TO_FILE = "FileSystemContentDao-CopyReceivedStreamToFile";

	IOServices ioServices;

	File rootFolder;

	DataLayerConfiguration configuration;

	public FileSystemContentDao(File rootFolder, IOServices ioServices, DataLayerConfiguration configuration) {
		this.rootFolder = rootFolder;
		this.ioServices = ioServices;
		this.configuration = configuration;
	}

	@Override
	public void initialize() {

	}

	@Override
	public void moveFileToVault(File file, String newContentId) {
		File content = getFileOf(newContentId);
		if (!content.exists()) {
			try {
				FileUtils.moveFile(file, content);
			} catch (FileExistsException e) {
				//OK

			} catch (IOException e) {
				throw new FileSystemContentDaoRuntimeException_DatastoreFailure(e);
			}
		}
	}

	@Override
	public void add(String newContentId, InputStream newInputStream) {

		File content = getFileOf(newContentId);
		content.getParentFile().mkdirs();

		OutputStream out = null;
		try {
			out = ioServices.newFileOutputStream(content, COPY_RECEIVED_STREAM_TO_FILE);
			IOUtils.copy(newInputStream, out);
		} catch (IOException e) {
			throw new FileSystemContentDaoRuntimeException_DatastoreFailure(e);
		} finally {
			ioServices.closeQuietly(out);
		}

	}

	@Override
	public void delete(List<String> contentIds) {
		for (String contentId : contentIds) {
			File file = getFileOf(contentId);
			file.delete();
		}
	}

	@Override
	public InputStream getContentInputStream(String contentId, String streamName)
			throws ContentDaoException_NoSuchContent {
		try {
			return new BufferedInputStream(ioServices.newFileInputStream(getFileOf(contentId), streamName));
		} catch (IOException e) {
			throw new ContentDaoException_NoSuchContent(contentId);
		}
	}

	@Override
	public CloseableStreamFactory<InputStream> getContentInputStreamFactory(final String id)
			throws ContentDaoException_NoSuchContent {

		final File file = getFileOf(id);

		if (!file.exists()) {
			throw new ContentDaoException_NoSuchContent(id);
		}

		return new CloseableStreamFactory<InputStream>() {
			@Override
			public void close()
					throws IOException {

			}

			@Override
			public long length() {
				return file.length();
			}

			@Override
			public InputStream create(String name)
					throws IOException {
				try {
					return getContentInputStream(id, name);
				} catch (ContentDaoException_NoSuchContent contentDaoException_noSuchContent) {
					throw new ImpossibleRuntimeException(contentDaoException_noSuchContent);
				}
			}
		};
	}

	@Override
	public boolean isFolderExisting(String folderId) {
		File folder = new File(rootFolder, folderId.replace("/", File.separator));
		return folder.exists();
	}

	@Override
	public boolean isDocumentExisting(String documentId) {
		return getFileOf(documentId).exists();
	}

	@Override
	public List<String> getFolderContents(String folderId) {
		File folder = new File(rootFolder, folderId.replace("/", File.separator));
		String[] fileArray = folder.list();
		List<String> files = new ArrayList<>();

		if (fileArray != null) {
			for (String file : fileArray) {
				files.add(folderId + "/" + file);
			}
		}

		return files;
	}

	@Override
	public long getContentLength(String vaultContentId) {
		return getFileOf(vaultContentId).length();
	}

	@Override
	public void moveFolder(String folderId, String newFolderId) {
		File folder = getFolder(folderId);
		if (!folder.exists()) {
			throw new ContentDaoRuntimeException_NoSuchFolder(folderId);
		}
		File newfolder = getFolder(newFolderId);
		newfolder.mkdirs();
		newfolder.delete();
		try {
			FileUtils.moveDirectory(folder, newfolder);
		} catch (IOException e) {
			throw new ContentDaoRuntimeException_CannotMoveFolderTo(folderId, newFolderId, e);
		}

	}

	public File getFolder(String folderId) {
		return new File(rootFolder, folderId.replace("/", File.separator));
	}

	@Override
	public void deleteFolder(String folderId) {

		File folder = getFolder(folderId);
		if (folder.exists()) {
			try {
				ioServices.deleteDirectory(getFolder(folderId));
			} catch (IOException e) {
				throw new ContentDaoRuntimeException_CannotDeleteFolder(folderId, e);
			}
		}
	}

	private File getFileOf(String contentId) {
		if (contentId.contains("/")) {
			File file = new File(rootFolder, contentId.replace("/", File.separator));
			if (!file.exists() && !file.getName().endsWith("tlog")) {
				File folder = file.getParentFile();
				if (!folder.exists()) {
					folder.mkdirs();
				}
				AtomicReference path = new AtomicReference();
				returnPath(StringUtils.removeEnd(StringUtils.removeStart(contentId, "/"),"/"), rootFolder, path);
				if (path.get() != null) {
					try {
						FileUtils.moveFile((File) path.get(), file);
						System.err.println("Moved  : " + path.get() +  " to: " + file);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			if (!file.exists() && !file.getName().endsWith("tlog")) {
				System.err.println("/////:	" + contentId + " /Not found : " + file);
			}
			return file;

		} else {
			if (configuration.getContentDaoFileSystemDigitsSeparatorMode() == DigitSeparatorMode.THREE_LEVELS_OF_ONE_DIGITS) {
				StringBuilder name = new StringBuilder();

				String level1 = toCaseInsensitive(contentId.charAt(0));
				name.append(level1).append(File.separator);

				if (contentId.length() > 1) {
					String level2 = toCaseInsensitive(contentId.charAt(1));
					name.append(level1).append(level2).append(File.separator);

					if (contentId.length() > 2) {
						String level3 = toCaseInsensitive(contentId.charAt(2));
						name.append(level1).append(level2).append(level3).append(File.separator);
					}
				}

				name.append(toCaseInsensitive(contentId));
				return new File(rootFolder, name.toString());

			} else {
				String folderName = contentId.substring(0, 2);
				File folder = new File(rootFolder, folderName);
				if (!folder.exists()) {
					folder.mkdir();
					System.err.println("Created  : " + folder);
				}
				File file = new File(folder, contentId);
				if (!file.exists()) {
					String searchFolderName = contentId.substring(0, 2).toLowerCase();
					tryFixLocation(folder, contentId, searchFolderName);

					searchFolderName = contentId.substring(0, 2).toUpperCase();
					tryFixLocation(folder, contentId, searchFolderName);

					searchFolderName = contentId.substring(0, 1).toUpperCase() + contentId.substring(1, 2).toLowerCase();
					tryFixLocation(folder, contentId, searchFolderName);

					searchFolderName = contentId.substring(0, 1).toLowerCase() + contentId.substring(1, 2).toUpperCase();
					tryFixLocation(folder, contentId, searchFolderName);
				}
				if (!file.exists()) {
					System.err.println("#####Not found : " + file);
				}
				return file;
			}
		}
	}

	private void returnPath(String contentId, File searchPath, AtomicReference matchingFiles) {
		contentId = StringUtils.replace(contentId, "//", "/");
		if (!contentId.contains("/") && searchPath.isFile()) {
			matchingFiles.set(searchPath);
		} else if (matchingFiles.get() == null) {
			String currentPathFromContent = StringUtils.substringBefore(contentId, "/");
			for (File subPath : searchPath.listFiles()) {
				if (subPath.getName().equalsIgnoreCase(currentPathFromContent)) {
					returnPath(StringUtils.substringAfter(contentId, "/"), subPath, matchingFiles);
				}
			}
		}
	}

	private void tryFixLocation(File expectedFolder, String contentId, String searchFolderName) {
		File searchFile = new File(new File(rootFolder, searchFolderName), contentId);
		if (searchFile.exists()) {
			File destinationFile = new File(expectedFolder, contentId);
			try {
				FileUtils.moveFile(searchFile, new File(expectedFolder, contentId));
				System.err.println("Moved  : " + searchFile +  " to: " + destinationFile);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private String toCaseInsensitive(char character) {
		String str = "" + character;
		return str;
	}

	private String toCaseInsensitive(String str) {
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < str.length(); i++) {
			stringBuilder.append(toCaseInsensitive(str.charAt(i)));
		}
		return stringBuilder.toString();
	}

	@Override
	public void close() {

	}
}