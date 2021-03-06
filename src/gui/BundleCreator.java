package gui;

import flashsystem.Bundle;
import flashsystem.BundleMetaData;
import gui.models.CategoriesContentProvider;
import gui.models.CategoriesModel;
import gui.models.Category;
import gui.models.SinfilesLabelProvider;
import gui.tools.WidgetsTool;
import gui.tools.createFTFJob;

import java.io.File;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.List;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;

public class BundleCreator extends Dialog {

	protected Object result;
	protected Shell shlBundler;
	private Text sourceFolder;
	private Text device;
	private Text branding;
	private Text version;
	Vector files = new Vector();
	ListViewer listViewerFiles;
	private Label lblSelectSourceFolder;
	private Button btnNewButton;
	private Label lblNewLabel_2;
	private List list;
	private Label lblNewLabel;
	private FormData fd_btnNewButton_1;
	private Button btnNewButton_1;
	private Composite composite_5;
	private Label lblNewLabel_4;
	private BundleMetaData meta = new BundleMetaData();
	private CategoriesModel model = new CategoriesModel(meta);
	TreeViewer treeViewerCategories;
	Button btnNoFinalVerification;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public BundleCreator(Shell parent, int style) {
		super(parent, style);
		setText("SWT Dialog");
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		WidgetsTool.setSize(shlBundler);
		
		shlBundler.open();
		shlBundler.layout();
		Display display = getParent().getDisplay();
		while (!shlBundler.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shlBundler = new Shell(getParent(), getStyle());
		shlBundler.setSize(632, 437);
		shlBundler.setText("Bundler");
		shlBundler.setLayout(new FormLayout());
		
		listViewerFiles = new ListViewer(shlBundler, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
		list = listViewerFiles.getList();
		FormData fd_list = new FormData();
		fd_list.left = new FormAttachment(0, 10);
		fd_list.bottom = new FormAttachment(100, -40);
		list.setLayoutData(fd_list);
	    listViewerFiles.setContentProvider(new IStructuredContentProvider() {
	        public Object[] getElements(Object inputElement) {
	          Vector v = (Vector)inputElement;
	          return v.toArray();
	        }
	        
	        public void dispose() {
	        }
	   
	        public void inputChanged(
	          Viewer viewer,
	          Object oldInput,
	          Object newInput) {
	        }
	      });
	    listViewerFiles.setLabelProvider(new LabelProvider() {
	        public Image getImage(Object element) {
	          return null;
	        }
	   
	        public String getText(Object element) {
	          return ((File)element).getName();
	        }
	      });
	    listViewerFiles.setSorter(new ViewerSorter(){
	        public int compare(Viewer viewer, Object e1, Object e2) {
	          return ((File)e1).getName().compareTo(((File)e2).getName());
	        }

	      });
		Label lblNewLabel_3 = new Label(shlBundler, SWT.NONE);
		fd_list.top = new FormAttachment(lblNewLabel_3, 6);
		FormData fd_lblNewLabel_3 = new FormData();
		fd_lblNewLabel_3.left = new FormAttachment(0, 10);
		lblNewLabel_3.setLayoutData(fd_lblNewLabel_3);
		lblNewLabel_3.setText("folder list :");
		
		composite_5 = new Composite(shlBundler, SWT.NONE);
		composite_5.setLayout(new TreeColumnLayout());
		FormData fd_composite_5 = new FormData();
		fd_composite_5.right = new FormAttachment(100, -10);
		fd_composite_5.top = new FormAttachment(list, 0, SWT.TOP);
		composite_5.setLayoutData(fd_composite_5);
		
		treeViewerCategories = new TreeViewer(composite_5, SWT.BORDER | SWT.MULTI);
		Tree treeCategories = treeViewerCategories.getTree();
		treeCategories.setHeaderVisible(true);
		treeCategories.setLinesVisible(true);
		treeViewerCategories.setContentProvider(new CategoriesContentProvider());
	    treeViewerCategories.setLabelProvider(new SinfilesLabelProvider());
	    treeViewerCategories.setSorter(new ViewerSorter(){
	        public int compare(Viewer viewer, Object e1, Object e2) {
	        	int cat1 = category(e1);
	        	int cat2 = category(e2);
	        	if (cat1 != cat2) return cat1 - cat2;
		    	if ((e1 instanceof Category) && (e2 instanceof Category))
		    		return ((Category)e1).getName().compareTo(((Category)e2).getName());
		    	else
		    		return ((File)e1).getName().compareTo(((File)e2).getName());
	        }
	      });
	    // Expand the tree
	    treeViewerCategories.setAutoExpandLevel(2);
	    // Provide the input to the ContentProvider
	    treeViewerCategories.setInput(new CategoriesModel(meta));
	    treeViewerCategories.refresh();
		
		Button btnCancel = new Button(shlBundler, SWT.NONE);
		fd_composite_5.bottom = new FormAttachment(btnCancel, -5);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shlBundler.dispose();
			}
		});
		FormData fd_btnCancel = new FormData();
		fd_btnCancel.right = new FormAttachment(100, -10);
		fd_btnCancel.bottom = new FormAttachment(100, -10);
		btnCancel.setLayoutData(fd_btnCancel);
		btnCancel.setText("Cancel");
		
		Button btnCreate = new Button(shlBundler, SWT.NONE);
		btnCreate.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Bundle b = new Bundle();
				b.setMeta(meta);
				b.setDevice(device.getText());
				b.setVersion(version.getText());
				b.setBranding(branding.getText());
				b.setCmd25(btnNoFinalVerification.getSelection()?"true":"false");
				createFTFJob j = new createFTFJob("Create FTF");
				j.setBundle(b);
				j.schedule();
			}
		});
		FormData fd_btnCreate = new FormData();
		fd_btnCreate.bottom = new FormAttachment(btnCancel, 0, SWT.BOTTOM);
		fd_btnCreate.right = new FormAttachment(btnCancel, -6);
		btnCreate.setLayoutData(fd_btnCreate);
		btnCreate.setText("Create");
		
		btnNewButton_1 = new Button(shlBundler, SWT.NONE);
		fd_composite_5.left = new FormAttachment(btnNewButton_1, 6);
		fd_list.right = new FormAttachment(100, -270);
		btnNewButton_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection)listViewerFiles.getSelection();
				Iterator i = selection.iterator();
				while (i.hasNext()) {
					File f = (File)i.next();
					files.remove(f);
					try {
						meta.process(f.getName(), f.getAbsolutePath());
						model.refresh(meta);
						treeViewerCategories.setInput(model);
					} catch (Exception ex) {}
					treeViewerCategories.setAutoExpandLevel(2);
					treeViewerCategories.refresh();
					listViewerFiles.refresh();
				}
			}
		});
		fd_btnNewButton_1 = new FormData();
		fd_btnNewButton_1.left = new FormAttachment(list, 6);
		fd_btnNewButton_1.right = new FormAttachment(100, -235);
		btnNewButton_1.setLayoutData(fd_btnNewButton_1);
		btnNewButton_1.setText("->");
		
		Button btnNewButton_2 = new Button(shlBundler, SWT.NONE);
		fd_btnNewButton_1.bottom = new FormAttachment(btnNewButton_2, -30);
		btnNewButton_2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection)treeViewerCategories.getSelection();
				Iterator i = selection.iterator();
				while (i.hasNext()) {
					Object o = i.next();
					if (o instanceof Category) {
						Category c = (Category)o;
						Iterator<File> j = c.getSinfiles().iterator();
						while (j.hasNext()) {
							File f=j.next();
							files.add(f);
							meta.remove(f.getName());
							model.refresh(meta);
							treeViewerCategories.setAutoExpandLevel(2);
							treeViewerCategories.refresh();
							listViewerFiles.refresh();
						}
					}
					if (o instanceof File) {
						files.add((File)o);
						meta.remove(((File)o).getName());
						model.refresh(meta);
						treeViewerCategories.setAutoExpandLevel(2);
						treeViewerCategories.refresh();
						listViewerFiles.refresh();
					}
				}
			}
		});
		FormData fd_btnNewButton_2 = new FormData();
		fd_btnNewButton_2.top = new FormAttachment(0, 292);
		fd_btnNewButton_2.right = new FormAttachment(composite_5, -6);
		fd_btnNewButton_2.left = new FormAttachment(list, 6);
		btnNewButton_2.setLayoutData(fd_btnNewButton_2);
		btnNewButton_2.setText("<-");
		
		lblNewLabel_4 = new Label(shlBundler, SWT.NONE);
		fd_lblNewLabel_3.right = new FormAttachment(lblNewLabel_4, -299);
		FormData fd_lblNewLabel_4 = new FormData();
		fd_lblNewLabel_4.left = new FormAttachment(0, 397);
		fd_lblNewLabel_4.right = new FormAttachment(100, -130);
		fd_lblNewLabel_4.top = new FormAttachment(lblNewLabel_3, 0, SWT.TOP);
		lblNewLabel_4.setLayoutData(fd_lblNewLabel_4);
		lblNewLabel_4.setText("Firmware content :");
		Composite composite = new Composite(shlBundler, SWT.NONE);
		composite.setLayout(new GridLayout(3, false));
		FormData fd_composite = new FormData();
		fd_composite.left = new FormAttachment(0, 10);
		fd_composite.right = new FormAttachment(100, -10);
		fd_composite.top = new FormAttachment(0, 10);
		composite.setLayoutData(fd_composite);
		
		lblSelectSourceFolder = new Label(composite, SWT.NONE);
		GridData gd_lblSelectSourceFolder = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblSelectSourceFolder.widthHint = 121;
		lblSelectSourceFolder.setLayoutData(gd_lblSelectSourceFolder);
		lblSelectSourceFolder.setText("Select source folder :");
		
		sourceFolder = new Text(composite, SWT.BORDER);
		sourceFolder.setEditable(false);
		GridData gd_sourceFolder = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_sourceFolder.widthHint = 428;
		sourceFolder.setLayoutData(gd_sourceFolder);
		
		btnNewButton = new Button(composite, SWT.NONE);
		btnNewButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dlg = new DirectoryDialog(shlBundler);

		        // Set the initial filter path according
		        // to anything they've selected or typed in
		        dlg.setFilterPath(sourceFolder.getText());

		        // Change the title bar text
		        dlg.setText("Directory chooser");

		        // Customizable message displayed in the dialog
		        dlg.setMessage("Select a directory");

		        // Calling open() will open and run the dialog.
		        // It will return the selected directory, or
		        // null if user cancels
		        String dir = dlg.open();
		        if (dir != null) {
		          // Set the text box to the new selection
		        	if (!sourceFolder.getText().equals(dir)) {
		        		sourceFolder.setText(dir);
		        		meta.clear();
		        		files = new Vector();
		    			File srcdir = new File(sourceFolder.getText());
		    			File[] chld = srcdir.listFiles();
		    			for(int i = 0; i < chld.length; i++) {
		    				if (chld[i].getName().toUpperCase().endsWith("SIN")) {
		    					try {
		    						meta.process(chld[i].getName(), chld[i].getAbsolutePath());
		    						meta.remove(chld[i].getName());
		    					}
		    					catch (Exception ex) {}
		    					files.add(chld[i]);
		    				}
		    			}
		    			model.refresh(meta);
		    			treeViewerCategories.setInput(model);
		    			listViewerFiles.setInput(files);
		        	}
		        }
			}
		});
		btnNewButton.setText("...");
		
		Composite composite_1 = new Composite(shlBundler, SWT.NONE);
		composite_1.setLayout(new GridLayout(2, false));
		FormData fd_composite_1 = new FormData();
		fd_composite_1.top = new FormAttachment(composite, 2);
		fd_composite_1.left = new FormAttachment(0, 10);
		composite_1.setLayoutData(fd_composite_1);
		
		lblNewLabel = new Label(composite_1, SWT.NONE);
		GridData gd_lblNewLabel = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_lblNewLabel.widthHint = 121;
		lblNewLabel.setLayoutData(gd_lblNewLabel);
		lblNewLabel.setText("Device :");
		
		device = new Text(composite_1, SWT.BORDER);
		GridData gd_device = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_device.widthHint = 270;
		device.setLayoutData(gd_device);
		
		Composite composite_2 = new Composite(shlBundler, SWT.NONE);
		composite_2.setLayout(new GridLayout(2, false));
		FormData fd_composite_2 = new FormData();
		fd_composite_2.right = new FormAttachment(composite_1, 0, SWT.RIGHT);
		fd_composite_2.top = new FormAttachment(composite_1, 6);
		fd_composite_2.left = new FormAttachment(0, 10);
		composite_2.setLayoutData(fd_composite_2);
		
		Label lblNewLabel_1 = new Label(composite_2, SWT.NONE);
		GridData gd_lblNewLabel_1 = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_lblNewLabel_1.widthHint = 121;
		lblNewLabel_1.setLayoutData(gd_lblNewLabel_1);
		lblNewLabel_1.setText("Version :");
		
		version = new Text(composite_2, SWT.BORDER);
		GridData gd_version = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_version.widthHint = 270;
		version.setLayoutData(gd_version);
		
		Composite composite_3 = new Composite(shlBundler, SWT.NONE);
		fd_lblNewLabel_3.top = new FormAttachment(composite_3, 6);
		fd_composite_1.right = new FormAttachment(100, -298);
		composite_3.setLayout(new GridLayout(2, false));
		FormData fd_composite_3 = new FormData();
		fd_composite_3.right = new FormAttachment(100, -298);
		fd_composite_3.left = new FormAttachment(0, 10);
		fd_composite_3.top = new FormAttachment(composite_2, 6);
		composite_3.setLayoutData(fd_composite_3);
		
		lblNewLabel_2 = new Label(composite_3, SWT.NONE);
		GridData gd_lblNewLabel_2 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblNewLabel_2.widthHint = 121;
		lblNewLabel_2.setLayoutData(gd_lblNewLabel_2);
		lblNewLabel_2.setText("Branding :");
		
		branding = new Text(composite_3, SWT.BORDER);
		GridData gd_branding = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_branding.widthHint = 219;
		branding.setLayoutData(gd_branding);
		
		Composite composite_4 = new Composite(shlBundler, SWT.NONE);
		fd_composite_3.bottom = new FormAttachment(100, -256);
		composite_4.setLayout(new GridLayout(1, false));
		FormData fd_composite_4 = new FormData();
		fd_composite_4.left = new FormAttachment(composite_2, 54);
		fd_composite_4.right = new FormAttachment(100, -10);
		fd_composite_4.bottom = new FormAttachment(lblNewLabel_4, -43);
		fd_composite_4.top = new FormAttachment(composite, 39);
		composite_4.setLayoutData(fd_composite_4);
		
		btnNoFinalVerification = new Button(composite_4, SWT.CHECK);
		GridData gd_btnNoFinalVerification = new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1);
		gd_btnNoFinalVerification.heightHint = 24;
		btnNoFinalVerification.setLayoutData(gd_btnNoFinalVerification);
		btnNoFinalVerification.setText("No final verification");

	}
}
