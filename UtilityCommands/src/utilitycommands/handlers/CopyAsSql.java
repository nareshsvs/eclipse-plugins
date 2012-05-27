package utilitycommands.handlers;

import java.awt.image.TileObserver;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.browser.TitleListener;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.ITextEditor;
/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class CopyAsSql extends AbstractHandler {
	/**
	 * The constructor.
	 */
	public CopyAsSql() {
	}


	public IEditorPart getActiveEditor() {
		if (PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor() instanceof IEditorPart) {
			IEditorPart ed = (IEditorPart)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();


			return ed;
		} else {
			return null;
		}
	}


	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {


		if (PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor() instanceof IEditorPart) {

			IEditorPart ed = (IEditorPart)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();

			if(ed instanceof ITextEditor)
			{
				ITextEditor textEditor = (ITextEditor)ed;
				ISelection sel = textEditor.getSelectionProvider().getSelection();

				String selectedText = "";
				if(sel instanceof TextSelection)
				{
					TextSelection textSelect = (TextSelection)sel;
					selectedText = textSelect.getText();
					String processedText = processMultiLineString(selectedText);


					Display disp = Display.findDisplay(Thread.currentThread());
					Clipboard cl = new Clipboard(disp);
					TextTransfer textTransfer = TextTransfer.getInstance();
					cl.setContents(new Object[]{processedText}, new Transfer[]{textTransfer});
					cl.dispose();

				}
				
			}
		}
		return null;
	}


	private String processMultiLineString(String str)
	{

		String regex = "((String)*(\\s*)(.*?)(\\s*)(=))*(\\s*)(\")(.*?)(\")(\\+*)";

		StringReader reader = new StringReader(str);
		BufferedReader buffReader = new BufferedReader(reader);
		Pattern pattern = Pattern.compile(regex);

		String sqlTitle = "";
		List<String> sqlList = new ArrayList<String>();

		String data;
		try {
			data = buffReader.readLine();
			while(data != null)
			{
				Matcher match = pattern.matcher(data);
				//System.out.println("data "+data);
				if(match.find() && match.groupCount() == 11)
				{
					//System.out.println("Found match ");
					String title = match.group(4);
					String queryString = match.group(9);

					if(title != null && title.length() > 0)
					{
						sqlTitle = title;
					}

					if(queryString != null && queryString.length() > 0)
					{
						sqlList.add(queryString);
					}
				}
				data = buffReader.readLine();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		StringBuffer buffer = new StringBuffer();
		if(sqlTitle != null && sqlTitle.length() > 0)
		{
			buffer.append("--").append(sqlTitle).append("\n");
		}
		for(String sql : sqlList)
		{
			if(sql != null && sql.length() > 0)
			{
				buffer.append(sql).append("\n");
			}
		}
		buffer.append(";").append("\n");


		return buffer.toString();

	}
}
