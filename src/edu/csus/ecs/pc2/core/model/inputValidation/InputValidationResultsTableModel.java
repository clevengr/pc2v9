package edu.csus.ecs.pc2.core.model.inputValidation;

import java.util.Arrays;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

/**
 * This class defines a {@link TableModel} for a table holding {@link InputValidationResult}s.
 * 
 * @author pc2@ecs.csus.edu
 *
 */
public class InputValidationResultsTableModel extends DefaultTableModel {

    private static String[] colNames = { "File", "Result", "Validator StdOut", "Validator StdErr" };

    private static Vector<String> columnNames = new Vector<String>(Arrays.asList(colNames));
    
    private Vector<InputValidationResult> results ;

    private static final long serialVersionUID = 1L;
    
    public InputValidationResultsTableModel(InputValidationResult [] results) {
        super(null, columnNames);
        setResults(results);
    }
    
    public InputValidationResultsTableModel() {
        this(null);
    }

    public void setResults(InputValidationResult [] results) {
        this.results = new Vector<InputValidationResult>();
        if (results != null){
            for (int i=0; i<results.length; i++) {
                this.results.add(results[i]);
            }
            setRowCount(results.length);
        } else {
            setRowCount(0);
        }
    }

    @Override
    public Object getValueAt(int row, int column) {

        Object obj = "Unknown";

        switch (column) {
            case 0:
                obj = results.get(row).getFullPathFilename();
                break;
            case 1:
                obj = results.get(row).isPassed();
                break;
            case 2:
                //TODO: need to return a string which can be used as a LINK to the file
                obj = results.get(row).getValidatorStdOut();
                break;
            case 3:
                //TODO: need to return a string which can be used as a LINK to the file
                obj = results.get(row).getValidatorStdErr();
                break;
            default:
                break;

        }
        return obj;
    }

    /**
     * Remove the specified row from the table.  Note that row numbers start with zero!
     * 
     * @param row - the row number to be removed, where the first row is row zero
     */
    @Override
    public void removeRow(int row) {
        results.remove(row);
        super.removeRow(row);
    }
    
    public void addRow (InputValidationResult result) {
        results.add(result);
        Vector<InputValidationResult> newRow = new Vector<InputValidationResult>();
        newRow.add(result);
        super.addRow(newRow);
    }
    
    
    public Iterable<InputValidationResult> getResults() {
        return results;
    }

}