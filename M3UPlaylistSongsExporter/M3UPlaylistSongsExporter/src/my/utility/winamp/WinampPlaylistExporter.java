package my.utility.winamp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Display;

public class WinampPlaylistExporter {

	private static final LinkOption[] EMPTY_LINK_OPTIONS = new LinkOption[] { LinkOption.NOFOLLOW_LINKS };

	private Path inputFile;
	private Path targetFolder;

	private boolean canReplace;

	private IProgressMonitor monitor;

	private long totalFilesCount;

	private int monitorCount;

	public WinampPlaylistExporter(Path inputFile, Path targetFolder,
			boolean canReplace) {
		this.inputFile = inputFile;
		this.targetFolder = targetFolder;
		this.canReplace = canReplace;
	}

	public void exportFile(IProgressMonitor monitor) {
		this.monitor = monitor;
		monitor.setCanceled(false);
		// monitor.beginTask("Exporting songs:", IProgressMonitor.UNKNOWN);
		if (Files.notExists(inputFile, EMPTY_LINK_OPTIONS)
				|| !Files.isRegularFile(inputFile, EMPTY_LINK_OPTIONS)) {
			System.out.println("Input m3u file doesn't exist");
			return;
		}

		if (Files.notExists(targetFolder, EMPTY_LINK_OPTIONS)
				|| !Files.isDirectory(targetFolder, EMPTY_LINK_OPTIONS)) {
			System.out.println("Output folder doesn't exist");
			return;
		}

		try {
			Stream<String> filteredLineStream = Files.lines(Paths.get(new File(inputFile.toString()).toURI())).map(line -> line.trim()).filter(
					line -> !line.startsWith("#"));
			 List<String> linesList = filteredLineStream.collect(Collectors.toList());
			totalFilesCount = linesList.size();
			monitorCount = 0;
			// monitor.done();
			monitor.beginTask("Exporting songs:", (int) totalFilesCount);

			linesList.stream().onClose(new Runnable() {
				@Override
				public void run() {
					if (!monitor.isCanceled()) {
						Display.getDefault().syncExec(new Runnable() {
							@Override
							public void run() {
								monitor.done();
							}
						});
						System.out.println("Export completed.");
					} else {
						System.out.println("Cancel Requested.");
					}
				}
			}).forEach(this::doExportFile);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}


	private void doExportFile(String filePath) {
		++monitorCount;
		if (!monitor.isCanceled()) {
			Path srcFile = Paths.get(new File(filePath).toURI());
			Path destFile = Paths.get(targetFolder.toString(), new File(
					filePath).getParentFile().getName(), srcFile.getFileName()
					.toString());
			try {
				String msg;
				Files.createDirectories(destFile.getParent());
				boolean exists = destFile.toFile().exists();
				String count = "(" + monitorCount + "/" + totalFilesCount
						+ ") ";
				if (!exists || canReplace) {
					msg = (exists ? "[Replacing] " : "[Copying  ] ") + filePath
							+ " ==> " + destFile.toString();
					System.out.println(count + msg);
					Files.copy(srcFile, destFile,
							StandardCopyOption.COPY_ATTRIBUTES,
							StandardCopyOption.REPLACE_EXISTING);
				} else {
					msg = "[Skipping ] " + filePath;
					System.out.println(count + msg);
				}
				Display.getDefault().syncExec(new Runnable() {
					@Override
					public void run() {
						monitor.subTask(count + msg);
					}
				});
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					monitor.subTask("Cancel requested.");
				}
			});
		}

		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				monitor.worked(monitorCount);
			}
		});
	}

}
