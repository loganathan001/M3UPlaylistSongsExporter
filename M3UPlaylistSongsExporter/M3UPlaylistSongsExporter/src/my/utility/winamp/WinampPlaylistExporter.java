package my.utility.winamp;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
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

		String readFile = readFile(inputFile.toString());
		List<String> lines = Arrays.stream(readFile.split("\n"))
				.map(line -> line.trim()).collect(Collectors.toList());
		Stream<String> filteredListStream = lines.stream().filter(
				l -> !l.startsWith("#"));
		List<String> list = filteredListStream.collect(Collectors.toList());
		totalFilesCount = list.size();
		monitorCount = 0;
		// monitor.done();
		monitor.beginTask("Exporting songs:", (int) totalFilesCount);

		Stream<String> listStream = list.stream();
		listStream.onClose(new Runnable() {
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
		});
		listStream.forEach(this::doExportFile);

	}

	private String readFile(String filePath) {
		StringBuffer stringBuffer = new StringBuffer();
		try {
			RandomAccessFile aFile = new RandomAccessFile(filePath, "r");
			FileChannel inChannel = aFile.getChannel();
			MappedByteBuffer buffer = inChannel.map(
					FileChannel.MapMode.READ_ONLY, 0, inChannel.size());
			buffer.load();
			for (int i = 0; i < buffer.limit(); i++) {
				stringBuffer.append((char) buffer.get());
			}
			buffer.clear(); // do something with the data and clear/compact it.
			inChannel.close();
			aFile.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return stringBuffer.toString();
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
				// TODO Auto-generated catch block
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
