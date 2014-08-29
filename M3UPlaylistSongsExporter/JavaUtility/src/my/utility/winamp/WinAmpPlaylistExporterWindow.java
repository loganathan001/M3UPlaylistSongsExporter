package my.utility.winamp;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

public class WinAmpPlaylistExporterWindow {

	protected Shell shell;
	private Text inputFileText;
	private Button btnInputFileBrowse;
	private Label lblTargetFolder;
	private Text targetFolderText;
	private Button btnTargetFolderBrowse;
	private Composite composite;
	private Composite buttonComposite;
	private Button btnClose;
	private Button btnExport;
	private Label label;
	private Button btnReplaceExistingFiles;
	private Label copyRight;
	private Label lblNewLabel;
	private Composite composite_1;
	private Label lblError;
	private ProgressMonitorPart progressMonitorPart;

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 * 
	 * @wbp.parser.entryPoint
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setSize(610, 357);
		shell.setText("Export songs from Winamp M3U Playlist");
		shell.setLayout(new GridLayout(1, false));

		composite = new Composite(shell, SWT.BORDER);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1,
				1));
		composite.setLayout(new GridLayout(3, false));

		Label lblInputMuFile = new Label(composite, SWT.NONE);
		lblInputMuFile.setText("Input M3U File:");

		inputFileText = new Text(composite, SWT.BORDER);
		inputFileText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				validate();
			}
		});
		inputFileText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));

		btnInputFileBrowse = new Button(composite, SWT.NONE);
		btnInputFileBrowse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog fileDialog = new FileDialog(shell, SWT.OPEN);
				fileDialog
						.setFilterExtensions(new String[] { "*.m3u", "*.M3U" });
				String filePath = fileDialog.open();
				if (filePath != null) {
					inputFileText.setText(filePath);
				}
			}
		});
		btnInputFileBrowse.setText("Browse...");

		lblTargetFolder = new Label(composite, SWT.NONE);
		lblTargetFolder.setText("Target Folder:");

		targetFolderText = new Text(composite, SWT.BORDER);
		targetFolderText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				validate();
			}
		});
		targetFolderText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));

		btnTargetFolderBrowse = new Button(composite, SWT.NONE);
		btnTargetFolderBrowse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog directoryDialog = new DirectoryDialog(shell,
						SWT.OPEN);
				String targetFolderPath = directoryDialog.open();
				if (targetFolderPath != null) {
					targetFolderText.setText(targetFolderPath);
				}
			}
		});
		btnTargetFolderBrowse.setText("Browse...");

		label = new Label(composite, SWT.NONE);

		btnReplaceExistingFiles = new Button(composite, SWT.CHECK);
		btnReplaceExistingFiles.setText("Replace Existing Files");
		new Label(composite, SWT.NONE);

		lblNewLabel = new Label(composite, SWT.NONE);

		btnExport = new Button(composite, SWT.NONE);
		btnExport.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				exportFiles();
			}
		});
		btnExport.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				1, 1));
		btnExport.setText("Export");
		btnExport.setEnabled(false);
		new Label(composite, SWT.NONE);

		composite_1 = new Composite(composite, SWT.NONE);
		composite_1.setLayout(new FillLayout());
		composite_1.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 3, 1));

		lblError = new Label(composite_1, SWT.WRAP);
		lblError.setText("                                                                                                                                                                                          ");
		lblError.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));

		Composite composite_2 = new Composite(composite, SWT.NONE);
		composite_2.setLayout(new FillLayout());
		composite_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 3, 1));

		progressMonitorPart = new ProgressMonitorPart(composite_2, null, false);
		buttonComposite = new Composite(shell, SWT.NONE);
		buttonComposite.setLayout(new GridLayout(2, false));
		buttonComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, true,
				false, 1, 1));

		copyRight = new Label(buttonComposite, SWT.NONE);
		copyRight.setText("(c) 2014 Loganathan.S (GPL)");// href='mailto:loganathan001@gmail.com?Subject=Hello%20Loganathan'

		btnClose = new Button(buttonComposite, SWT.NONE);
		btnClose.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				System.exit(0);
			}
		});
		btnClose.setText("Close");

	}

	protected void exportFiles() {
		Path inputFile = Paths.get(new File(inputFileText.getText().trim())
				.toURI());
		Path targetFolder = Paths.get(new File(targetFolderText.getText()
				.trim()).toURI());
		boolean canReplace = btnReplaceExistingFiles.getSelection();
		WinampPlaylistExporter winampPlaylistExporter = new WinampPlaylistExporter(
				inputFile, targetFolder, canReplace);
		winampPlaylistExporter.exportFile(progressMonitorPart);
	}

	private void validate() {
		String inputFilePath = inputFileText.getText().trim();
		if (inputFilePath.isEmpty()) {
			setErrorMessage("Select an input M3U file.");
			return;
		}
		File inputFile = new File(inputFilePath);
		if (!inputFile.exists() || !inputFile.isFile()
				|| !inputFile.getName().toLowerCase().endsWith(".m3u")) {
			setErrorMessage("Invalid input file!");
			return;
		}

		String targetFolderPath = targetFolderText.getText().trim();
		if (targetFolderPath.isEmpty()) {
			setErrorMessage("Select a target folder.");
			return;
		}
		File targetFolder = new File(targetFolderPath);
		if (!targetFolder.exists() || !targetFolder.isDirectory()) {
			setErrorMessage("Invalid target folder!");
			return;
		}

		setErrorMessage(null);
	}

	private void setErrorMessage(String errMsg) {
		lblError.setText(errMsg == null ? "" : "* " + errMsg);
		btnExport.setEnabled(errMsg == null);
	}
}
