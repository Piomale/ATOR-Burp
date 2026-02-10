package burp;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.TransferHandler;

public class TableRowDnDTransferHandler extends TransferHandler {
	private static final long serialVersionUID = 1L;

	private static final DataFlavor ROW_INDEX_FLAVOR = new DataFlavor(
			DataFlavor.javaJVMLocalObjectMimeType + ";class=java.lang.Integer",
			"Integer Row Index");

	private final RowMoveHandler rowMoveHandler;

	public TableRowDnDTransferHandler(RowMoveHandler rowMoveHandler) {
		this.rowMoveHandler = rowMoveHandler;
	}

	@Override
	protected Transferable createTransferable(JComponent c) {
		JTable table = (JTable) c;
		final Integer selectedRow = table.getSelectedRow();

		return new Transferable() {
			@Override
			public DataFlavor[] getTransferDataFlavors() {
				return new DataFlavor[] { ROW_INDEX_FLAVOR };
			}

			@Override
			public boolean isDataFlavorSupported(DataFlavor flavor) {
				return ROW_INDEX_FLAVOR.equals(flavor);
			}

			@Override
			public Object getTransferData(DataFlavor flavor) {
				return selectedRow;
			}
		};
	}

	@Override
	public int getSourceActions(JComponent c) {
		return MOVE;
	}

	@Override
	public boolean canImport(TransferSupport support) {
		if (!support.isDrop()) {
			return false;
		}
		if (!(support.getComponent() instanceof JTable)) {
			return false;
		}
		return support.isDataFlavorSupported(ROW_INDEX_FLAVOR);
	}

	@Override
	public boolean importData(TransferSupport support) {
		if (!canImport(support)) {
			return false;
		}

		try {
			JTable table = (JTable) support.getComponent();
			JTable.DropLocation dropLocation = (JTable.DropLocation) support.getDropLocation();

			int from = (Integer) support.getTransferable().getTransferData(ROW_INDEX_FLAVOR);
			int to = dropLocation.getRow();

			if (from < 0) {
				return false;
			}

			if (to < 0) {
				to = table.getRowCount();
			}

			if (from == to || from + 1 == to) {
				return false;
			}

			int normalizedTo = to;
			if (from < to) {
				normalizedTo = to - 1;
			}

			rowMoveHandler.moveRow(from, normalizedTo);

			if (normalizedTo >= 0 && normalizedTo < table.getRowCount()) {
				table.getSelectionModel().setSelectionInterval(normalizedTo, normalizedTo);
			}

			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
