/*
 * Exactly
 * Author: Nouman Tayyab (nouman@avpreserve.com)
 * Author: Rimsha Khalid (rimsha@avpreserve.com)
 * Version: 0.1.5
 * Requires: JDK 1.7 or higher
 * Description: This tool transfers digital files to the UK Exactly
 * Support: info@avpreserve.com
 * License: Apache 2.0
 * Copyright: University of Kentucky (http://www.uky.edu). All Rights Reserved
 *
 */
package uk.sipperfly.ui;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.sipperfly.persistent.Configurations;
import uk.sipperfly.repository.ConfigurationsRepo;
import static uk.sipperfly.ui.Exactly.GACOM;

import uk.sipperfly.utils.CommonUtil;

/**
 * This class implements the visitor used by the FileTransfer class.
 *
 * @author Nouman Tayyab
 */
class CopyDirVisitor extends SimpleFileVisitor<Path> {

	private final Exactly parent;
	private final Path fromPath;
	private final Path toPath;
	private final CopyOption[] COPY_OPTIONS = new CopyOption[]{
	  StandardCopyOption.REPLACE_EXISTING,
	  StandardCopyOption.COPY_ATTRIBUTES
	};
	/**
	 * Constructor for CopyDirVisitor
	 *
	 * @param parent Pointer to the parent GUI for status updates
	 * @param fromPath The source of the copy
	 * @param toPath The destination of the copy
	 */
	public CopyDirVisitor(Exactly parent, Path fromPath, Path toPath) {
		if (parent == null
				|| fromPath == null
				|| toPath == null) {
			throw new IllegalArgumentException();
		}

		this.parent = parent;
		this.fromPath = fromPath;
		this.toPath = toPath;

	}

	/**
	 * Creates the target directories as we walk the tree.
	 *
	 * @param dir The target directory path.
	 * @param attrs Default attributes (unused)
	 * @return CONTINUE in all cases
	 * @throws IOException If the target directory cannot be created
	 */
	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
		if (dir == null
				|| attrs == null) {
			throw new IllegalArgumentException();
		}

		Path targetPath = toPath.resolve(fromPath.relativize(dir));
		if (!Files.exists(targetPath)) {
			Files.createDirectory(targetPath);
			copyFileAttributes(dir, targetPath);
		}
		return FileVisitResult.CONTINUE;
	}

	/**
	 * Performs the actual file copy from the source to the target. Updates the parent GUI progress bar as well.
	 *
	 * @param file The path of the file to copy
	 * @param attrs Default attributes (unused)
	 * @return CONTINUE in all cases
	 * @throws IOException If the file cannot be copied
	 */
	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		if (file == null
				|| attrs == null) {
			throw new IllegalArgumentException();
		}

		if (this.parent.GetBackgroundWorker().isCancelled()) {
			Logger.getLogger(GACOM).log(Level.SEVERE, "TERMINATING file visitor");
			return FileVisitResult.TERMINATE;
		}
		ConfigurationsRepo configRepo = new ConfigurationsRepo();
		Configurations config = configRepo.getOneOrCreateOne();
		config.getFilters();
		CommonUtil commonUtil = new CommonUtil();

		System.out.println(file.getFileName().toString());
		boolean ignore = commonUtil.checkIgnoreFiles(file.getFileName().toString(), config.getFilters());
		if (!ignore) {
			File destinationFile = new File(toPath.resolve(fromPath.relativize(file)).toString());
			try {
				copyFileUsingFileChannels(file.toFile(), destinationFile);
				copyFileAttributes(file, destinationFile.toPath());
			} catch (Exception e) {
				e.printStackTrace();
			}
			this.parent.tranferredFiles = this.parent.tranferredFiles + 1;
			this.parent.UpdateProgressBar(this.parent.tranferredFiles);
		}
		Logger.getLogger(GACOM).log(Level.INFO, "Count of Files: ".concat(Integer.toString(this.parent.tranferredFiles)));

		return FileVisitResult.CONTINUE;
	}

	private static void copyFileUsingFileChannels(File source, File destinationFile)
			throws IOException {
        FileChannel outputChannel = new RandomAccessFile(destinationFile, "rw").getChannel();
        FileLock lock = outputChannel.lock();
        try (FileChannel inputChannel = new FileInputStream(source).getChannel()) {
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
        } finally {
            if (lock != null) {
                lock.release();
            }
            outputChannel.close();
        }
	}

	private static void copyFileAttributes(Path source, Path destination){
		//Copy Basic File Attributes
		try {
			BasicFileAttributes attr = Files.readAttributes(source, BasicFileAttributes.class);
			Files.setAttribute(destination, "basic:creationTime", attr.creationTime());
			Files.setAttribute(destination, "basic:lastAccessTime", attr.lastAccessTime());
			Files.setAttribute(destination, "basic:lastModifiedTime", attr.lastModifiedTime());
		} catch (IOException e) {
			e.printStackTrace();
		}
		//Copy DOS File Attributes
		try {
			DosFileAttributes attr =
					Files.readAttributes(source, DosFileAttributes.class);
			Files.setAttribute(destination, "dos:readonly", attr.isReadOnly());
			Files.setAttribute(destination, "dos:hidden", attr.isHidden());
			Files.setAttribute(destination, "dos:archive", attr.isArchive());
			Files.setAttribute(destination, "dos:system", attr.isSystem());
		} catch (UnsupportedOperationException | IOException e) {
			System.err.println("DOS file" +
					" attributes not supported:" + e);
		}
		//Copy POSIX File Attributes
		try {
			PosixFileAttributes attrs =
					Files.readAttributes(source, PosixFileAttributes.class);
			System.out.println(attrs.group());
			System.out.println(attrs.permissions());
			System.out.println(attrs.owner());
			Files.setAttribute(destination, "posix:permissions", attrs.permissions());
			Files.setAttribute(destination, "posix:group", attrs.group());
		}
		catch (Exception e) {
			System.err.println("POSIX file" +
					" attributes not supported:" + e);
		}
		//Copy ACL File Attributes
		try {
			AclFileAttributeView aclFileAttributes = Files.getFileAttributeView(
					source, AclFileAttributeView.class);
			Files.setAttribute(destination, "acl:acl", aclFileAttributes.getAcl());
			Files.setAttribute(destination, "acl:owner", aclFileAttributes.getOwner());
		}
		catch (Exception e){
			System.err.println("ACL file" +
					" attributes not supported:" + e);
		}
	}
}
